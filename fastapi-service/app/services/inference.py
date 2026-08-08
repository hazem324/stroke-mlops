from pathlib import Path

import numpy as np
import SimpleITK as sitk
import torch

from app.ml.model_loader import get_model


TARGET_SHAPE = (128, 128, 64)


# ==========================================================
# Read MRI
# ==========================================================

def load_dwi_image(file_path: Path) -> sitk.Image:
    """
    Load a DWI MRI from a NIfTI file.
    """

    return sitk.ReadImage(str(file_path))


# ==========================================================
# Convert SimpleITK -> NumPy
# ==========================================================

def image_to_numpy(image: sitk.Image) -> np.ndarray:
    """
    Convert a SimpleITK image to a NumPy array.
    """

    volume = sitk.GetArrayFromImage(image)

    volume = np.transpose(
        volume,
        (2, 1, 0),
    )

    return volume.astype(np.float32)


# ==========================================================
# Normalize
# ==========================================================

def normalize_volume(volume: np.ndarray) -> np.ndarray:
    """
    Apply Z-score normalization.
    """

    foreground = volume > 0

    non_zero = volume[foreground]

    if non_zero.size == 0:
        return volume

    mean = non_zero.mean()

    std = non_zero.std()

    normalized = np.zeros_like(volume)

    normalized[foreground] = (
        volume[foreground] - mean
    ) / max(std, 1e-8)

    return normalized


# ==========================================================
# Resize
# ==========================================================

def resize_volume(volume: np.ndarray) -> np.ndarray:

    from scipy.ndimage import zoom

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


# ==========================================================
# NumPy -> Tensor
# ==========================================================

def numpy_to_tensor(volume: np.ndarray) -> torch.Tensor:
    """
    Convert NumPy volume to PyTorch tensor.

    Output shape:

    (1,1,H,W,D)
    """

    tensor = torch.from_numpy(volume)

    tensor = tensor.float()

    tensor = tensor.unsqueeze(0)

    tensor = tensor.unsqueeze(0)

    return tensor


# ==========================================================
# Prediction
# ==========================================================

def predict(file_path: Path) -> np.ndarray:
    """
    Complete inference pipeline.
    """

    model = get_model()

    image = load_dwi_image(file_path)

    volume = image_to_numpy(image)

    volume = normalize_volume(volume)

    volume = resize_volume(volume)

    tensor = numpy_to_tensor(volume)

    with torch.no_grad():

        prediction = model(tensor)
        prediction = torch.sigmoid(prediction)
        prediction = (prediction > 0.5).to(torch.uint8)
        prediction = prediction.squeeze()

    return prediction.cpu().numpy()