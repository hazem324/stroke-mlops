from pathlib import Path
from typing import Dict, List

import numpy as np
import SimpleITK as sitk

from load_data import ISLESDatasetLoader

from preprocessing.constants import OUTPUT_DIR

from preprocessing.loader import (
    load_volumes,
    resample_to_reference,
)

from preprocessing.transforms import (
    crop_to_foreground,
    normalize_volume,
    resize_volume,
)



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


    