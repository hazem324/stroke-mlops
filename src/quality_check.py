from pathlib import Path
from typing import Dict, List, Optional

import numpy as np
import nibabel as nib
import pandas as pd

from load_data import ISLESDatasetLoader


# 1. Verification des valeurs manquantes / infinies
def check_nan_inf(volume: np.ndarray, label: str) -> List[str]:

    problems = []

    if np.isnan(volume).any():
        problems.append(f"{label}: NaN present")

    if np.isinf(volume).any():
        problems.append(f"{label}: Inf present")

    return problems


# 2. Verification de la dimensionnalite du volume
def check_volume_dimension(volume: np.ndarray, label: str) -> List[str]:
    problems = []

    if volume.ndim != 3:
        problems.append(f"{label}: volume {volume.ndim}D au lieu de 3D")

    return problems

# 3. Verification de la coherence des shapes image/masque
def check_shape_consistency(
    image: np.ndarray, mask: np.ndarray, label: str
) -> List[str]:

    problems = []

    if image.shape != mask.shape:
        problems.append(
            f"{label}: shape mismatch image {image.shape} vs mask {mask.shape}"
        )

    return problems


# 4. Verification des labels du masque
def check_mask_labels(mask: np.ndarray) -> List[str]:
    
    problems = []

    labels = np.unique(mask)
    invalid_labels = [v for v in labels if not (np.isclose(v, 0.0) or np.isclose(v, 1.0))]

    if invalid_labels:
        problems.append(f"labels inattendus dans le masque : {invalid_labels}")

    return problems

# 5. Verification que le masque n'est pas vide
def check_empty_mask(mask: np.ndarray) -> List[str]:
    problems = []

    if mask.sum() == 0:
        problems.append("masque entierement vide (0 lesion annotee)")

    return problems

# 6. Verification de la plage de valeurs d'intensite
def check_intensity_range(volume: np.ndarray, label: str) -> List[str]:

    problems = []

    vmin = volume.min()
    vmax = volume.max()

    if np.isclose(vmin, vmax):
        problems.append(f"{label}: volume constant (min={vmin:.6f}, max={vmax:.6f})")

    return problems


# 7. Verification de la coherence de l'affine
def check_affine_consistency(
    image_affine: np.ndarray, mask_affine: np.ndarray, label: str, tol: float = 1e-3
) -> List[str]:
    
    problems = []

    if not np.allclose(image_affine, mask_affine, atol=tol):
        problems.append(f"{label}: affine image/mask differente (desalignement possible)")

    return problems

# 8. Controle qualite complet pour un patient
def check_patient(patient_id: str, paths: Dict[str, Optional[Path]]) -> Dict:

    problems = []

    mask_path = paths.get("mask")

    if mask_path is None or not mask_path.exists():
        return {
            "patient_id": patient_id,
            "n_problemes": 1,
            "problemes": "Masque manquant",
        }

    mask_img = nib.load(str(mask_path))
    mask = mask_img.get_fdata()

    problems += check_nan_inf(mask, "mask")
    problems += check_volume_dimension(mask, "mask")
    problems += check_mask_labels(mask)
    problems += check_empty_mask(mask)

    for modality in ["flair", "adc", "dwi"]:

        image_path = paths.get(modality)

        if image_path is None or not image_path.exists():
            problems.append(f"{modality}: fichier manquant")
            continue

        image_img = nib.load(str(image_path))
        image = image_img.get_fdata()

        problems += check_nan_inf(image, modality)
        problems += check_volume_dimension(image, modality)
        problems += check_intensity_range(image, modality)

        if modality in ["adc", "dwi"]:
            problems += check_shape_consistency(image, mask, modality)
            problems += check_affine_consistency(image_img.affine, mask_img.affine, modality)

    return {
        "patient_id": patient_id,
        "n_problemes": len(problems),
        "problemes": "; ".join(problems) if problems else "aucun",
    }


# 9. Execution du controle qualite sur tout le dataset
def run_quality_check(
    loader: ISLESDatasetLoader, patient_ids: List[str]
) -> pd.DataFrame:

    rows = []

    for patient_id in patient_ids:
        paths = loader.build_patient_paths(patient_id)
        result = check_patient(patient_id, paths)
        rows.append(result)

        status = "OK" if result["n_problemes"] == 0 else f"{result['n_problemes']} probleme(s)"
        print(f"[{status}] {patient_id}")

    return pd.DataFrame(rows)


# 10. Analyse des lesions
def analyze_lesions(
    loader: ISLESDatasetLoader, patient_ids: List[str]
) -> pd.DataFrame:
    
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
            "has_lesion": lesion_size > 0,
        })

    df = pd.DataFrame(rows)

    print("\n========== ANALYSE DES LESIONS ==========\n")

    n_patients = len(df)
    n_positive = df["has_lesion"].sum()
    n_negative = n_patients - n_positive

    print(f"Nombre total de patients : {n_patients}")
    print(f"Patients avec lesion     : {n_positive}")
    print(f"Patients sans lesion     : {n_negative}")

    if n_positive > 0:
        lesions = df[df["has_lesion"]]["lesion_voxels"]
        print("\nTaille des lesions (voxels)")
        print(f"Minimum : {lesions.min()}")
        print(f"Maximum : {lesions.max()}")
        print(f"Moyenne : {lesions.mean():.2f}")
        print(f"Mediane : {lesions.median():.2f}")

    return df


# 11. Export du rapport
def export_qc_report(df: pd.DataFrame, output_dir: str) -> None:
    
    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    report_path = output_path / "qc_report.csv"
    df.to_csv(report_path, index=False)

    print(f"\n[SAVED] {report_path}")

    n_ok = (df["n_problemes"] == 0).sum()
    n_ko = (df["n_problemes"] > 0).sum()

    print(f"\nResume : {n_ok} patients OK, {n_ko} patients avec au moins un probleme")