from pathlib import Path
from typing import Dict, Optional, Tuple, List

import numpy as np
import SimpleITK as sitk
from scipy.ndimage import zoom

from load_data import ISLESDatasetLoader


TARGET_SHAPE = (128, 128, 64)
OUTPUT_DIR = Path("data/preprocessed")
CROP_MARGIN = 5  # voxels de marge autour du cerveau detecte


# 1. Chargement des volumes d'un patient
def load_volumes(paths: Dict[str, Optional[Path]]) -> Dict[str, sitk.Image]:
    """
    Charge tous les volumes disponibles d'un patient avec SimpleITK.
    """

    volumes = {}

    for modality, path in paths.items():

        if path is None or not path.exists():
            continue

        volumes[modality] = sitk.ReadImage(str(path))

    return volumes


# 2. Resampling vers une image de reference
def resample_to_reference(
    moving: sitk.Image, reference: sitk.Image, is_mask: bool = False
) -> sitk.Image:

    resampler = sitk.ResampleImageFilter()
    resampler.SetReferenceImage(reference)
    resampler.SetInterpolator(
        sitk.sitkNearestNeighbor if is_mask else sitk.sitkLinear
    )

    return resampler.Execute(moving)


# 3. Recadrage sur le cerveau (crop foreground)
def crop_to_foreground(
    volumes: Dict[str, np.ndarray], reference_key: str = "adc", margin: int = CROP_MARGIN
) -> Dict[str, np.ndarray]:
    
    ref = volumes[reference_key]
    coords = np.argwhere(ref > 0)

    if coords.size == 0:
        return volumes

    mins = np.maximum(coords.min(axis=0) - margin, 0)
    maxs = np.minimum(coords.max(axis=0) + margin, ref.shape)

    slices = tuple(slice(mn, mx) for mn, mx in zip(mins, maxs))

    return {key: vol[slices] for key, vol in volumes.items()}


# 4. Normalisation d'intensite (z-score sur voxels non nuls)
def normalize_volume(volume: np.ndarray) -> np.ndarray:
    
    volume = volume.astype(np.float32)
    foreground_mask = volume > 0
    nonzero = volume[foreground_mask]

    if nonzero.size == 0:
        return volume

    mean = nonzero.mean()
    std = nonzero.std()

    if std < 1e-8:
        return volume

    normalized = np.zeros_like(volume)
    normalized[foreground_mask] = (volume[foreground_mask] - mean) / std

    return normalized

# 5. Redimensionnement vers une taille fixe
def resize_volume(
    volume: np.ndarray, target_shape: Tuple[int, int, int] = TARGET_SHAPE, is_mask: bool = False
) -> np.ndarray:

    factors = [target_shape[i] / volume.shape[i] for i in range(3)]
    interpolation_order = 0 if is_mask else 1

    resized = zoom(volume, zoom=factors, order=interpolation_order)

    if is_mask:
        resized = np.round(resized).astype(np.float32)
        resized = np.clip(resized, 0, 1)
    else:
        resized = resized.astype(np.float32)

    return resized

# 6. Sauvegarde des volumes pretraites
def save_patient(
    patient_id: str,
    adc: np.ndarray,
    dwi: np.ndarray,
    flair: np.ndarray,
    mask: np.ndarray,
) -> None:

    patient_dir = OUTPUT_DIR / patient_id
    patient_dir.mkdir(parents=True, exist_ok=True)

    np.save(patient_dir / "adc.npy", adc)
    np.save(patient_dir / "dwi.npy", dwi)
    np.save(patient_dir / "flair.npy", flair)
    np.save(patient_dir / "mask.npy", mask)

    print(f"[SAVED] {patient_dir}")


# 7. Pretraitement complet d'un patient
def preprocess_patient(patient_id: str, loader: ISLESDatasetLoader) -> bool:

    paths = loader.build_patient_paths(patient_id)
    volumes = load_volumes(paths)

    required = ["adc", "dwi", "flair", "mask"]
    for modality in required:
        if modality not in volumes:
            print(f"[SKIP] {patient_id} : {modality} manquant")
            return False

    adc_img = volumes["adc"]
    dwi_img = volumes["dwi"]
    flair_img = volumes["flair"]
    mask_img = volumes["mask"]

    flair_img = resample_to_reference(flair_img, adc_img, is_mask=False)
    dwi_img = resample_to_reference(dwi_img, adc_img, is_mask=False)
    mask_img = resample_to_reference(mask_img, adc_img, is_mask=True)

    adc = np.transpose(sitk.GetArrayFromImage(adc_img), (2, 1, 0))
    dwi = np.transpose(sitk.GetArrayFromImage(dwi_img), (2, 1, 0))
    flair = np.transpose(sitk.GetArrayFromImage(flair_img), (2, 1, 0))
    mask = np.transpose(sitk.GetArrayFromImage(mask_img), (2, 1, 0))

    if not (adc.shape == dwi.shape == flair.shape == mask.shape):
        print(
            f"[SKIP] {patient_id} : shapes incoherentes apres resampling "
            f"(adc={adc.shape}, dwi={dwi.shape}, flair={flair.shape}, mask={mask.shape})"
        )
        return False

    cropped = crop_to_foreground(
        {"adc": adc, "dwi": dwi, "flair": flair, "mask": mask},
        reference_key="adc",
    )
    adc, dwi, flair, mask = cropped["adc"], cropped["dwi"], cropped["flair"], cropped["mask"]

    adc = normalize_volume(adc)
    dwi = normalize_volume(dwi)
    flair = normalize_volume(flair)

    adc = resize_volume(adc)
    dwi = resize_volume(dwi)
    flair = resize_volume(flair)
    mask = resize_volume(mask, is_mask=True)

    save_patient(patient_id, adc, dwi, flair, mask)

    return True


# 8. Pretraitement de tout le dataset
def run_preprocessing(loader: ISLESDatasetLoader, patient_ids: List[str]) -> Dict:

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    processed = 0
    skipped = 0

    print("\n========== PREPROCESSING ==========\n")

    for i, patient_id in enumerate(patient_ids, start=1):
        print(f"\n[{i}/{len(patient_ids)}] {patient_id}")

        success = preprocess_patient(patient_id, loader)

        if success:
            processed += 1
        else:
            skipped += 1

    return {
        "total": len(patient_ids),
        "processed": processed,
        "skipped": skipped,
        "output": OUTPUT_DIR,
    }


# 9. Resume
def print_summary(summary: Dict) -> None:
    """
    Affiche un resume final du pretraitement.
    """

    print("\n")
    print("=" * 60)
    print("PREPROCESSING TERMINE")
    print("=" * 60)

    print(f"Patients totaux      : {summary['total']}")
    print(f"Patients traites     : {summary['processed']}")
    print(f"Patients ignores     : {summary['skipped']}")
    print(f"\nDonnees sauvegardees dans : {summary['output']}")
    print("=" * 60)