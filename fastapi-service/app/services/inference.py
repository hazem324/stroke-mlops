from pathlib import Path

import numpy as np
import SimpleITK as sitk
import torch

from app.ml.model_loader import get_model


TARGET_SHAPE = (128, 128, 64)


# Read MRI

def load_dwi_image(file_path: Path) -> sitk.Image:

    return sitk.ReadImage(str(file_path))


# Convert SimpleITK -> NumPy

def image_to_numpy(image: sitk.Image) -> np.ndarray:

    volume = sitk.GetArrayFromImage(image)

    volume = np.transpose(
        volume,
        (2, 1, 0),
    )

    return volume.astype(np.float32)

# Normalize

def normalize_volume(volume: np.ndarray) -> np.ndarray:

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


# Resize

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


# NumPy -> Tensor

def numpy_to_tensor(volume: np.ndarray) -> torch.Tensor:

    tensor = torch.from_numpy(volume).float()

    tensor = tensor.unsqueeze(0)
    tensor = tensor.unsqueeze(0)

    return tensor


# Save segmentation as NIfTI

def save_prediction_as_nifti( prediction: np.ndarray, output_path: Path,) -> None:

    prediction_image = sitk.GetImageFromArray(
        prediction.astype(np.uint8)
    )

    sitk.WriteImage(
        prediction_image,
        str(output_path),
    )


# Prediction

def predict( file_path: Path, output_path: Path, ) -> np.ndarray:


    # Get cached model
    model = get_model()

    # Read MRI
    image = load_dwi_image(file_path)

    # Convert to NumPy
    volume = image_to_numpy(image)

    # Normalize
    volume = normalize_volume(volume)

    # Resize
    volume = resize_volume(volume)

    # NumPy -> Tensor
    tensor = numpy_to_tensor(volume)

    # Model inference
    with torch.no_grad():

        prediction = model(tensor)

        # Logits -> probabilities
        prediction = torch.sigmoid(prediction)

        # Probability -> binary mask
        prediction = ( prediction > 0.5).to(torch.uint8)

        # Remove batch/channel dimensions
        prediction = prediction.squeeze()

    # Tensor -> NumPy
    prediction = prediction.cpu().numpy()

    # Save NIfTI
    save_prediction_as_nifti(prediction, output_path,)

    return prediction