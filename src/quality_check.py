"""
quality_check.py

Rôle : contrôle qualité automatisé, exécuté sur l'ensemble du dataset
(pas un échantillon), pour détecter les anomalies exploitables par
du code : NaN/Inf, incohérences dimensionnelles, masques vides,
valeurs d'intensité suspectes, désalignement image/masque.

Dépend de load_data.py pour :
- la validation de la structure du dataset
- la découverte des patients
- la construction des chemins de fichiers

Sortie : reports/qc_report.csv (une ligne par patient, colonne
"problemes" listant les anomalies détectées).
"""

from pathlib import Path
from typing import Dict, List, Optional

import numpy as np
import nibabel as nib
import pandas as pd

from load_data import ISLESDatasetLoader



# 1. Vérification des valeurs manquantes / infinies

def check_nan_inf(volume: np.ndarray, label: str) -> List[str]:
    """
    Détecte la présence de NaN ou Inf dans un volume.

    Rôle :
    - Un NaN/Inf dans les données d'entraînement fait planter ou
      corrompt silencieusement l'apprentissage (loss = NaN).
    """

    problems = []

    if np.isnan(volume).any():
        problems.append(f"{label}: NaN présent")

    if np.isinf(volume).any():
        problems.append(f"{label}: Inf présent")

    return problems



# 2. Vérification de la cohérence des shapes image/masque

def check_shape_consistency(
    image: np.ndarray, mask: np.ndarray, label: str
) -> List[str]:
    """
    Vérifie que l'image et son masque ont exactement la même shape.

    Rôle :
    - Un mismatch de shape rend impossible tout calcul de loss
      voxel-à-voxel, et signale souvent un mauvais fichier associé.
    """

    problems = []

    if image.shape != mask.shape:
        problems.append(
            f"{label}: shape mismatch image {image.shape} vs mask {mask.shape}"
        )

    return problems



# 3. Vérification que le masque n'est pas vide

def check_empty_mask(mask: np.ndarray) -> List[str]:
    """
    Vérifie si le masque ne contient aucune lésion annotée.

    Rôle :
    - Ce n'est pas forcément une erreur (certains patients n'ont pas
      de lésion visible), mais c'est une info à connaître : ces cas
      servent d'exemples négatifs et doivent être répartis avec soin
      entre train/val/test (pas tous dans le même split).
    """

    problems = []

    if mask.sum() == 0:
        problems.append("masque entièrement vide (0 lésion annotée)")

    return problems



# 4. Vérification de la plage de valeurs d'intensité

def check_intensity_range(volume: np.ndarray, label: str) -> List[str]:
    """
    Vérifie uniquement qu'un volume n'est pas constant.
    """

    problems = []

    vmin = volume.min()
    vmax = volume.max()

    if np.isclose(vmin, vmax):
        problems.append(
            f"{label}: volume constant (min={vmin:.6f}, max={vmax:.6f})"
        )

    return problems



# 5. Vérification de la cohérence de l'affine (orientation spatiale)

def check_affine_consistency(
    image_affine: np.ndarray, mask_affine: np.ndarray, label: str, tol: float = 1e-3
) -> List[str]:
    """
    Vérifie que l'image et son masque partagent la même matrice affine
    (même orientation, origine, spacing dans l'espace réel).

    Rôle :
    - Deux volumes de même shape mais d'affine différente peuvent
      quand même être mal alignés dans l'espace réel (recalage raté).
      C'est un bug silencieux que check_shape_consistency ne détecte
      pas seul.
    """

    problems = []

    if not np.allclose(image_affine, mask_affine, atol=tol):
        problems.append(f"{label}: affine image/mask différente (désalignement possible)")

    return problems



# 6. Contrôle qualité complet pour un patient

def check_patient(patient_id: str, paths: Dict[str, Optional[Path]]) -> Dict:
    """
    Exécute tous les contrôles qualité pour un patient.

    Contrôles effectués :
    - Présence du masque
    - NaN / Inf
    - Masque vide
    - Intensités
    - Shape et affine uniquement pour ADC et DWI
    """

    problems = []

   
    # Chargement du masque
   
    mask_path = paths.get("mask")

    if mask_path is None or not mask_path.exists():
        return {
            "patient_id": patient_id,
            "n_problemes": 1,
            "problemes": "Masque manquant"
        }

    mask_img = nib.load(str(mask_path))
    mask = mask_img.get_fdata()

    # Vérification du masque
    problems += check_nan_inf(mask, "mask")
    problems += check_volume_dimension(mask, "mask")
    problems += check_mask_labels(mask)
    problems += check_empty_mask(mask)

   
    # Vérification des modalités
   
    for modality in ["flair", "adc", "dwi"]:

        image_path = paths.get(modality)

        if image_path is None or not image_path.exists():
            problems.append(f"{modality}: fichier manquant")
            continue

        image_img = nib.load(str(image_path))
        image = image_img.get_fdata()

        # Contrôles valables pour toutes les modalités
        problems += check_nan_inf(image, modality)
        problems += check_volume_dimension(image, modality)
        problems += check_intensity_range(image, modality)

        # Contrôles géométriques uniquement pour ADC et DWI
        if modality in ["adc", "dwi"]:

            problems += check_shape_consistency(
                image,
                mask,
                modality,
            )

            problems += check_affine_consistency(
                image_img.affine,
                mask_img.affine,
                modality,
            )


    return {
        "patient_id": patient_id,
        "n_problemes": len(problems),
        "problemes": "; ".join(problems) if problems else "aucun",
    }



# 7. Exécution du contrôle qualité sur tout le dataset

def run_quality_check(
    loader: ISLESDatasetLoader, patient_ids: List[str]
) -> pd.DataFrame:
    """
    Applique check_patient() à tous les patients du dataset et
    agrège les résultats dans un DataFrame.

    Rôle :
    - Contrairement à visualize.py (échantillon), ici on passe
      systématiquement TOUS les patients, car les checks sont
      automatisables et peu coûteux comparé à une inspection visuelle.
    """

    rows = []

    for patient_id in patient_ids:
        paths = loader.build_patient_paths(patient_id)
        result = check_patient(patient_id, paths)
        rows.append(result)

        status = "OK" if result["n_problemes"] == 0 else f"{result['n_problemes']} problème(s)"
        print(f"[{status}] {patient_id}")

    return pd.DataFrame(rows)



# 8. Export du rapport

def export_qc_report(df: pd.DataFrame, output_dir: str) -> None:
    """
    Sauvegarde le rapport de contrôle qualité en CSV.

    Rôle :
    - Livrable final : liste exhaustive des sujets à exclure ou
      à corriger avant preprocessing.py / entraînement.
    """

    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    report_path = output_path / "qc_report.csv"
    df.to_csv(report_path, index=False)

    print(f"\n[SAVED] {report_path}")

    n_ok = (df["n_problemes"] == 0).sum()
    n_ko = (df["n_problemes"] > 0).sum()

    print(f"\nRésumé : {n_ok} patients OK, {n_ko} patients avec au moins un problème")

def check_mask_labels(mask: np.ndarray) -> List[str]:
    """
    Vérifie que le masque contient uniquement les labels 0 et 1.
    """

    problems = []

    labels = np.unique(mask)

    invalid_labels = []

    for value in labels:

        if not (np.isclose(value, 0.0) or np.isclose(value, 1.0)):
            invalid_labels.append(value)

    if invalid_labels:
        problems.append(
            f"labels inattendus dans le masque : {invalid_labels}"
        )

    return problems

def check_volume_dimension(volume: np.ndarray, label: str) -> List[str]:
    """
    Vérifie que le volume est en 3D.

    Rôle :
    - Les modèles de segmentation 3D (U-Net, nnU-Net)
      attendent des volumes 3D.
    """

    problems = []

    if volume.ndim != 3:
        problems.append(
            f"{label}: volume {volume.ndim}D au lieu de 3D"
        )

    return problems


def analyze_lesions(
    loader: ISLESDatasetLoader,
    patient_ids: List[str]
) -> pd.DataFrame:
    """
    Analyse les masques de segmentation.

    Pour chaque patient :
    - calcule le nombre de voxels appartenant à la lésion
    - indique si une lésion est présente

    Retourne un DataFrame contenant les résultats.
    """

    rows = []

    for patient_id in patient_ids:

        paths = loader.build_patient_paths(patient_id)

        mask_path = paths.get("mask")

        if mask_path is None or not mask_path.exists():
            continue

        mask = nib.load(str(mask_path)).get_fdata()

        lesion_size = int(np.sum(mask > 0))

        rows.append({
            "patient_id": patient_id,
            "lesion_voxels": lesion_size,
            "has_lesion": lesion_size > 0
        })

    df = pd.DataFrame(rows)

    print("\n========== ANALYSE DES LÉSIONS ==========\n")

    n_patients = len(df)
    n_positive = df["has_lesion"].sum()
    n_negative = n_patients - n_positive

    print(f"Nombre total de patients : {n_patients}")
    print(f"Patients avec lésion     : {n_positive}")
    print(f"Patients sans lésion     : {n_negative}")

    if n_positive > 0:

        lesions = df[df["has_lesion"]]["lesion_voxels"]

        print("\nTaille des lésions (voxels)")

        print(f"Minimum : {lesions.min()}")
        print(f"Maximum : {lesions.max()}")
        print(f"Moyenne : {lesions.mean():.2f}")
        print(f"Médiane : {lesions.median():.2f}")

    return df 

# 9. Point d'entrée

# def main():
#     dataset_path = "data/ISLES-2022"
#     output_dir = "reports"

#     loader = ISLESDatasetLoader(dataset_path)
#     loader.validate_dataset_path()
#     patient_ids = loader.discover_patients()

#     df = run_quality_check(loader, patient_ids)
#     lesion_df = analyze_lesions(loader, patient_ids)
#     lesion_df.to_csv("reports/lesion_statistics.csv", index=False)
#     export_qc_report(df, output_dir)


# if __name__ == "__main__":
#     main()