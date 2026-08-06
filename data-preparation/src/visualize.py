import random
from pathlib import Path
from typing import Dict, List, Optional

import numpy as np
import nibabel as nib
import matplotlib
matplotlib.use("Agg")  # pas d'affichage interactif, on sauvegarde direct
import matplotlib.pyplot as plt

from load_data import ISLESDatasetLoader


# 1. Selection d'un echantillon de patients a controler
def select_sample_patients(
    patient_ids: List[str], n_samples: int = 10, seed: int = 42
) -> List[str]:

    random.seed(seed)
    n_samples = min(n_samples, len(patient_ids))

    return random.sample(patient_ids, n_samples)


# 2. Chargement des volumes d'un patient
def load_volumes(paths: Dict[str, Optional[Path]]) -> Dict[str, np.ndarray]:
    """
    Charge toutes les modalites disponibles d'un patient.
    """

    volumes = {}

    for modality, path in paths.items():

        if path is None or not path.exists():
            print(f"[MISSING] {modality.upper()}")
            continue

        volumes[modality] = nib.load(str(path)).get_fdata()

    return volumes


# 3. Generation de la figure (ADC / DWI / MASK) pour un patient
def plot_patient_modalities(
    patient_id: str,
    volumes: Dict[str, np.ndarray],
    output_dir: Path,
) -> None:
    modalities = [
        ("adc", "ADC"),
        ("dwi", "DWI"),
        ("mask", "Ground Truth Mask"),
    ]

    if "mask" in volumes:

        mask = volumes["mask"]
        lesion_slices = [i for i in range(mask.shape[2]) if np.sum(mask[:, :, i]) > 0]

        if len(lesion_slices) > 0:
            slice_idx = lesion_slices[len(lesion_slices) // 2]
            print(f"{patient_id} -> lesion trouvee (slice {slice_idx})")
        else:
            slice_idx = mask.shape[2] // 2
            print(f"{patient_id} -> masque vide, coupe centrale ({slice_idx})")

    else:
        first_volume = next(iter(volumes.values()))
        slice_idx = first_volume.shape[2] // 2

    fig, axes = plt.subplots(1, 3, figsize=(10, 10))
    axes = axes.flatten()

    for ax, (key, title) in zip(axes, modalities):

        if key not in volumes:
            ax.set_title(f"{title}\nMissing")
            ax.axis("off")
            continue

        volume = volumes[key]
        idx = min(slice_idx, volume.shape[2] - 1)

        ax.imshow(volume[:, :, idx].T, cmap="gray", origin="lower")
        ax.set_title(f"{title}\nSlice {idx}")
        ax.axis("off")

    fig.suptitle(patient_id)
    plt.tight_layout()

    output_file = output_dir / f"{patient_id}.png"
    fig.savefig(output_file, dpi=150)
    plt.close(fig)

    print(f"[SAVED] {output_file}")


# 4. Pipeline complet de visualisation
def run_visualization(
    loader: ISLESDatasetLoader,
    patient_ids: List[str],
    output_dir: str = "reports/visualizations",
    n_samples: int = 10,
) -> None:

    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    sample_ids = select_sample_patients(patient_ids, n_samples=n_samples)
    print(f"\nEchantillon selectionne ({len(sample_ids)} patients) : {sample_ids}\n")

    for patient_id in sample_ids:
        paths = loader.build_patient_paths(patient_id)
        volumes = load_volumes(paths)

        if len(volumes) == 0:
            print(f"[SKIP] {patient_id}")
            continue

        plot_patient_modalities(patient_id, volumes, output_path)