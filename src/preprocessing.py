from pathlib import Path
from typing import Dict, Optional

import numpy as np
import nibabel as nib
import SimpleITK as sitk

from load_data import ISLESDatasetLoader


TARGET_SHAPE = (128, 128, 64)

OUTPUT_DIR = Path("data/preprocessed")


# 1. Charge tous les volumes d'un patient.

def load_volumes(
    paths: Dict[str, Optional[Path]]
) -> Dict[str, sitk.Image]:
    

    volumes = {}

    for modality, path in paths.items():

        if path is None:
            continue

        if not path.exists():
            continue

        volumes[modality] = sitk.ReadImage(str(path))

    return volumes


# 2. Resampling

def resample_to_reference( moving: sitk.Image, reference: sitk.Image,is_mask: bool = False,) -> sitk.Image:
    

    resampler = sitk.ResampleImageFilter()

    resampler.SetReferenceImage(reference)

    if is_mask:

        # Conserver uniquement les labels 0 / 1
        resampler.SetInterpolator(
            sitk.sitkNearestNeighbor
        )

    else:

        # Images IRM
        resampler.SetInterpolator(
            sitk.sitkLinear
        )

    return resampler.Execute(moving)


# 3. Normalisation Cela permet d'avoir toutes les IRM dans la même échelle

def normalize_volume(volume: np.ndarray) -> np.ndarray:
    
    volume = volume.astype(np.float32)

    mean = volume.mean()

    std = volume.std()

    if std < 1e-8:
        return volume

    volume = (volume - mean) / std

    return volume


from scipy.ndimage import zoom


# 4. Resize des volumes (Redimensionne un volume vers TARGET_SHAPE.)

def resize_volume(  volume: np.ndarray, target_shape=TARGET_SHAPE, is_mask: bool = False,) -> np.ndarray:

    factors = [
        target_shape[i] / volume.shape[i]
        for i in range(3)
    ]

    interpolation = 0 if is_mask else 1

    resized = zoom(
        volume,
        zoom=factors,
        order=interpolation,
    )

    return resized.astype(np.float32)


# 5. Sauvegarde les volumes prétraités.

def save_patient( patient_id: str, adc: np.ndarray, dwi: np.ndarray, flair: np.ndarray,mask: np.ndarray,):
    
    patient_dir = OUTPUT_DIR / patient_id

    patient_dir.mkdir(
        parents=True,
        exist_ok=True,
    )

    np.save(patient_dir / "adc.npy", adc)
    np.save(patient_dir / "dwi.npy", dwi)
    np.save(patient_dir / "flair.npy", flair)
    np.save(patient_dir / "mask.npy", mask)

    print(f"[SAVED] {patient_dir}")


# 6. Prétraitement complet d'un patient

def preprocess_patient( patient_id: str, loader: ISLESDatasetLoader,):

    paths = loader.build_patient_paths(patient_id)

    volumes = load_volumes(paths)

    required = ["adc", "dwi", "flair", "mask"]

    for modality in required:

        if modality not in volumes:

            print(
                f"[SKIP] {patient_id} : {modality} manquant"
            )

            return False
    # ADC = image de référence

    adc_img = volumes["adc"]

    dwi_img = volumes["dwi"]

    flair_img = volumes["flair"]

    mask_img = volumes["mask"]

    # Resampling

    flair_img = resample_to_reference(
        flair_img,
        adc_img,
        is_mask=False,
    )

    mask_img = resample_to_reference(
        mask_img,
        adc_img,
        is_mask=True,
    )

    # Conversion numpy

    adc = sitk.GetArrayFromImage(adc_img)

    dwi = sitk.GetArrayFromImage(dwi_img)

    flair = sitk.GetArrayFromImage(flair_img)

    mask = sitk.GetArrayFromImage(mask_img)

    # SimpleITK retourne (z,y,x)

    adc = np.transpose(adc, (2, 1, 0))

    dwi = np.transpose(dwi, (2, 1, 0))

    flair = np.transpose(flair, (2, 1, 0))

    mask = np.transpose(mask, (2, 1, 0))

    # Normalisation

    adc = normalize_volume(adc)

    dwi = normalize_volume(dwi)

    flair = normalize_volume(flair)

    # Resize

    adc = resize_volume(adc)

    dwi = resize_volume(dwi)

    flair = resize_volume(flair)

    mask = resize_volume(
        mask,
        is_mask=True,
    )

    # Sauvegarde

    save_patient(
        patient_id,
        adc,
        dwi,
        flair,
        mask,
    )

    return True

# 7. Prétraitement de tout le dataset

def run_preprocessing( loader: ISLESDatasetLoader, patient_ids: list[str],):

    OUTPUT_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    processed = 0
    skipped = 0

    print("\n========== PREPROCESSING ==========\n")

    for i, patient_id in enumerate(patient_ids, start=1):

        print(
            f"\n[{i}/{len(patient_ids)}] {patient_id}"
        )

        success = preprocess_patient(
            patient_id,
            loader,
        )

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


# 8. Résumé

def print_summary(summary):
    """
    Affiche un résumé du prétraitement.
    """

    print("\n")
    print("=" * 60)
    print("PREPROCESSING TERMINÉ")
    print("=" * 60)

    print(f"Patients totaux      : {summary['total']}")
    print(f"Patients traités     : {summary['processed']}")
    print(f"Patients ignorés     : {summary['skipped']}")

    print(f"\nDonnées sauvegardées dans :")

    print(summary["output"])

    print("\nStructure obtenue :")

    print("=" * 60)

def main():

    dataset_path = "data/ISLES-2022"

    loader = ISLESDatasetLoader(
        dataset_path
    )

    loader.validate_dataset_path()

    patient_ids = loader.discover_patients()

    summary = run_preprocessing(

        loader,

        patient_ids,

    )

    print_summary(summary)


if __name__ == "__main__":
    main()