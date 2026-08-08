from pathlib import Path
from typing import Tuple

import numpy as np
import SimpleITK as sitk
import torch
from scipy.ndimage import zoom

from app.ml.model_loader import get_model


TARGET_SHAPE = (128, 128, 64)
CROP_MARGIN = 5
THRESHOLD = 0.5


# Read DWI

def load_dwi_image(file_path: Path) -> sitk.Image:
    """
    Read the original DWI NIfTI image.
    """

    return sitk.ReadImage(str(file_path))


# SimpleITK -> NumPy

def image_to_numpy(image: sitk.Image) -> np.ndarray:

    volume = sitk.GetArrayFromImage(image)

    volume = np.transpose(
        volume,
        (2, 1, 0),
    )

    return volume.astype(np.float32)


# Crop foreground

def crop_to_foreground( volume: np.ndarray, margin: int = CROP_MARGIN, ) -> Tuple[np.ndarray, Tuple[slice, slice, slice]]:

    coords = np.argwhere(volume > 0)

    if coords.size == 0:
        return volume, (
            slice(0, volume.shape[0]),
            slice(0, volume.shape[1]),
            slice(0, volume.shape[2]),
        )

    mins = np.maximum(
        coords.min(axis=0) - margin,
        0,
    )

    maxs = np.minimum(
        coords.max(axis=0) + margin + 1,
        volume.shape,
    )

    slices = tuple(
        slice(int(mn), int(mx))
        for mn, mx in zip(mins, maxs)
    )

    cropped = volume[slices]

    return cropped, slices


# Normalize

def normalize_volume( volume: np.ndarray, ) -> np.ndarray:

    volume = volume.astype(np.float32)

    foreground = volume > 0

    non_zero = volume[foreground]

    if non_zero.size == 0:
        return volume

    mean = non_zero.mean()
    std = non_zero.std()

    if std < 1e-8:
        return volume

    normalized = np.zeros_like(volume)

    normalized[foreground] = (
        volume[foreground] - mean
    ) / std

    return normalized


# Resize for model

def resize_volume( volume: np.ndarray, ) -> np.ndarray:

    factors = [
        TARGET_SHAPE[i] / volume.shape[i]
        for i in range(3)
    ]

    resized = zoom(
        volume,
        zoom=factors,
        order=1,
    )

    return resized.astype(np.float32)


# Restore mask to cropped space

def restore_mask_to_crop( prediction: np.ndarray, crop_shape: Tuple[int, int, int], ) -> np.ndarray:

    factors = [
        crop_shape[i] / prediction.shape[i]
        for i in range(3)
    ]

    restored = zoom(
        prediction.astype(np.uint8),
        zoom=factors,
        order=0,
    )

    restored = np.rint(restored).astype(np.uint8)

    restored = np.clip(
        restored,
        0,
        1,
    )

    return restored


# Restore mask to original space

def restore_mask_to_original( prediction: np.ndarray, original_shape: Tuple[int, int, int], crop_slices: Tuple[slice, slice, slice],) -> np.ndarray:

    crop_shape = tuple(
        sl.stop - sl.start
        for sl in crop_slices
    )

    restored_crop = restore_mask_to_crop(
        prediction,
        crop_shape,
    )

    original_mask = np.zeros(
        original_shape,
        dtype=np.uint8,
    )

    original_mask[crop_slices] = restored_crop

    return original_mask


# NumPy -> Tensor

def numpy_to_tensor( volume: np.ndarray, ) -> torch.Tensor:

    tensor = torch.from_numpy(
        volume
    ).float()

    tensor = tensor.unsqueeze(0)
    tensor = tensor.unsqueeze(0)

    return tensor


# Save NIfTI

def save_prediction_as_nifti( prediction: np.ndarray, reference_image: sitk.Image, output_path: Path, ) -> None:
    """
    Save prediction as NIfTI using the original DWI
    geometry.
    """

    prediction_sitk = sitk.GetImageFromArray(
        np.transpose(
            prediction,
            (2, 1, 0),
        ).astype(np.uint8)
    )

    prediction_sitk.CopyInformation(
        reference_image
    )

    sitk.WriteImage(
        prediction_sitk,
        str(output_path),
    )


# Complete inference

def predict( file_path: Path, output_path: Path, overlay_path: Path, ) -> dict:
    
    # Load cached model

    model = get_model()

    # Read original DWI

    image = load_dwi_image(
        file_path
    )

    # Convert to NumPy

    original_volume = image_to_numpy(
        image
    )

    original_shape = (
        original_volume.shape
    )

    # Crop

    cropped_volume, crop_slices = (
        crop_to_foreground(
            original_volume
        )
    )

    crop_shape = cropped_volume.shape

    # Normalize

    normalized = normalize_volume(
        cropped_volume
    )

    # Resize

    resized = resize_volume(
        normalized
    )

    # NumPy -> Tensor

    tensor = numpy_to_tensor(
        resized
    )

    # Device

    device = next(
        model.parameters()
    ).device

    tensor = tensor.to(device)

    # Inference

    with torch.no_grad():

        output = model(
            tensor
        )

        probability = torch.sigmoid(
            output
        )

        prediction = (
            probability > THRESHOLD
        ).to(torch.uint8)

        prediction = prediction.squeeze()

    # Tensor -> NumPy

    prediction = (
        prediction
        .cpu()
        .numpy()
    )

    # Restore to original DWI space

    prediction_original = (
        restore_mask_to_original(prediction, original_shape, crop_slices, )
    )

    save_overlay_as_nifti(     original_volume,     prediction_original,     image,     overlay_path, ) 
    # Save NIfTI

    save_prediction_as_nifti( prediction_original, image,output_path, )

    return {
        "prediction": prediction_original,
        "original_image": image,
        "original_volume": original_volume,
    }


def save_overlay_as_nifti( dwi_volume: np.ndarray, prediction: np.ndarray, reference_image: sitk.Image, output_path: Path, ) -> None:

    # Copy original DWI
    overlay = dwi_volume.copy().astype(np.float32)

    # Normalize DWI intensity for visualization
    non_zero = overlay[overlay > 0]

    if non_zero.size > 0:
        min_value = non_zero.min()
        max_value = non_zero.max()

        if max_value > min_value:
            overlay = (
                (overlay - min_value)
                / (max_value - min_value)
            )

    # Highlight predicted lesion
    lesion = prediction > 0

    if lesion.any():
        overlay[lesion] = 1.5

    # NumPy (x,y,z) -> SimpleITK (z,y,x)
    overlay_sitk = sitk.GetImageFromArray(
        np.transpose(
            overlay,
            (2, 1, 0),
        ).astype(np.float32)
    )

    # Keep original DWI geometry
    overlay_sitk.CopyInformation(
        reference_image
    )

    # Save
    sitk.WriteImage(
        overlay_sitk,
        str(output_path),
    )