"""
visualize.py

Rôle : contrôle visuel des coupes IRM et de leurs masques de
segmentation, pour détecter les anomalies invisibles aux métriques
numériques (mismatch image/masque, mauvaise séquence, artefact majeur).

Dépend de load_data.py pour :
- la validation de la structure du dataset
- la découverte des patients
- la construction des chemins de fichiers

Sortie : reports/visualizations/{patient_id}_overlay.png
"""

import random
from pathlib import Path
from typing import Dict, List, Optional

import numpy as np
import nibabel as nib
import matplotlib
matplotlib.use("Agg")  # pas d'affichage interactif, on sauvegarde direct
import matplotlib.pyplot as plt

from load_data import ISLESDatasetLoader


# ----------------------------------------------------------------------
# 1. Sélection d'un échantillon de patients à contrôler
# ----------------------------------------------------------------------
def select_sample_patients(
    patient_ids: List[str], n_samples: int = 10, seed: int = 42
) -> List[str]:
    """
    Sélectionne un échantillon aléatoire de patients à visualiser.

    Rôle :
    - Éviter de générer des centaines d'images (coûteux en temps/disque).
    - random_state fixe pour reproductibilité (toujours le même échantillon
      d'une exécution à l'autre, facilite la comparaison entre runs).
    """

    random.seed(seed)
    n_samples = min(n_samples, len(patient_ids))

    return random.sample(patient_ids, n_samples)


# ----------------------------------------------------------------------
# 2. Chargement des volumes image + masque d'un patient
# ----------------------------------------------------------------------
def load_volumes(paths: Dict[str, Optional[Path]]) -> Dict[str, np.ndarray]:
    """
    Charge toutes les modalités disponibles d'un patient.
    """

    volumes = {}

    for modality, path in paths.items():

        if path is None or not path.exists():
            print(f"[MISSING] {modality.upper()}")
            continue

        volumes[modality] = nib.load(str(path)).get_fdata()

    return volumes


# ----------------------------------------------------------------------
# 3. Trouver la coupe contenant le plus de lésion, par plan
# ----------------------------------------------------------------------
def find_best_slice(mask: np.ndarray, axis: int) -> int:
    """
    Trouve l'indice de coupe contenant le plus de voxels de lésion
    sur l'axe donné (0=sagittal, 1=coronal, 2=axial).

    Rôle :
    - Si la lésion est petite, une coupe centrale au hasard risque de
      ne rien montrer. On choisit la coupe la plus représentative.
    - Si le masque est vide (pas de lésion), on retombe sur la coupe
      centrale du volume.
    """

    lesion_counts = mask.sum(axis=tuple(i for i in range(3) if i != axis))

    if lesion_counts.max() == 0:
        return mask.shape[axis] // 2

    return int(np.argmax(lesion_counts))


# ----------------------------------------------------------------------
# 4. Extraction d'une coupe 2D selon l'axe
# ----------------------------------------------------------------------
def get_slice(volume: np.ndarray, axis: int, index: int) -> np.ndarray:
    """
    Extrait une coupe 2D d'un volume 3D selon l'axe demandé.
    """

    if axis == 0:
        return volume[index, :, :]
    elif axis == 1:
        return volume[:, index, :]
    else:
        return volume[:, :, index]


# ----------------------------------------------------------------------
# 5. Génération de l'image overlay (3 plans) pour un patient
# ----------------------------------------------------------------------
def plot_patient_modalities(
    patient_id: str,
    volumes: Dict[str, np.ndarray],
    output_dir: Path,
) -> None:
    """
    Affiche FLAIR, ADC, DWI et MASK.
    Si le masque contient une lésion, affiche une coupe avec la lésion.
    Sinon affiche la coupe centrale.
    """

    modalities = [
        ("adc", "ADC"),
        ("dwi", "DWI"),
        ("mask", "Ground Truth Mask"),
    ]

    # -------------------------------------------------
    # Choix de la coupe à afficher
    # -------------------------------------------------

    if "mask" in volumes:

        mask = volumes["mask"]

        # Chercher toutes les coupes contenant une lésion
        lesion_slices = []

        for i in range(mask.shape[2]):
            if np.sum(mask[:, :, i]) > 0:
                lesion_slices.append(i)

        if len(lesion_slices) > 0:
            # Choisir la coupe centrale parmi celles contenant la lésion
            slice_idx = lesion_slices[len(lesion_slices) // 2]

            print(f"{patient_id} -> lésion trouvée (slice {slice_idx})")

        else:
            # Aucun voxel annoté
            slice_idx = mask.shape[2] // 2

            print(f"{patient_id} -> masque vide, coupe centrale ({slice_idx})")

    else:
        # Si le masque est absent
        first_volume = next(iter(volumes.values()))
        slice_idx = first_volume.shape[2] // 2

    # -------------------------------------------------
    # Affichage
    # -------------------------------------------------

    fig, axes = plt.subplots(1, 3, figsize=(10, 10))
    axes = axes.flatten()

    for ax, (key, title) in zip(axes, modalities):

        if key not in volumes:
            ax.set_title(f"{title}\nMissing")
            ax.axis("off")
            continue

        volume = volumes[key]

        # Si les profondeurs sont différentes (ex: FLAIR)
        idx = min(slice_idx, volume.shape[2] - 1)

        ax.imshow(
            volume[:, :, idx].T,
            cmap="gray",
            origin="lower",
        )

        ax.set_title(f"{title}\nSlice {idx}")
        ax.axis("off")

    fig.suptitle(patient_id)

    plt.tight_layout()

    output_file = output_dir / f"{patient_id}.png"

    fig.savefig(output_file, dpi=150)

    plt.close(fig)

    print(f"[SAVED] {output_file}")


# ----------------------------------------------------------------------
# 6. Pipeline complet de visualisation
# ----------------------------------------------------------------------
def run_visualization(
    loader: ISLESDatasetLoader,
    patient_ids: List[str],
    output_dir: str = "reports/visualizations",
    n_samples: int = 10,
    modality: str = "flair",
) -> None:
    """
    Orchestre tout le processus :
    - sélectionne un échantillon de patients
    - charge image + masque
    - génère et sauvegarde l'overlay 3 plans pour chacun
    """

    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    sample_ids = select_sample_patients(patient_ids, n_samples=n_samples)
    print(f"\nÉchantillon sélectionné ({len(sample_ids)} patients) : {sample_ids}\n")

    for patient_id in sample_ids:
        paths = loader.build_patient_paths(patient_id)

        volumes = load_volumes(paths)

        if len(volumes) == 0:
            print(f"[SKIP] {patient_id}")
            continue

        plot_patient_modalities(patient_id, volumes, output_path,)
