from pathlib import Path

import numpy as np
import SimpleITK as sitk
import torch

from preprocessing.loader import resample_to_reference
from preprocessing.transforms import (
    crop_to_foreground,
    normalize_volume,
    resize_volume,
)


def preprocess_dwi_for_inference( dwi_path: Path, ) -> torch.Tensor:
    """
    Preprocess a DWI MRI volume for inference.

    Pipeline
    --------
    DWI (.nii.gz)
        ↓
    Read image
        ↓
    Convert to NumPy
        ↓
    Crop foreground
        ↓
    Normalize
        ↓
    Resize
        ↓
    NumPy → Tensor
        ↓
    Add Channel dimension
        ↓
    Add Batch dimension
        ↓
    Return Tensor
    """

    # Read MRI

    dwi_img = sitk.ReadImage(str(dwi_path))

    # Convert to NumPy

    dwi = np.transpose(
        sitk.GetArrayFromImage(dwi_img),
        (2, 1, 0),
    )

    # Crop foreground

    volumes = {"adc": dwi}

    volumes = crop_to_foreground(
        volumes,
        reference_key="adc",
    )

    dwi = volumes["adc"]

    # Normalize

    dwi = normalize_volume(dwi)

    # Resize

    dwi = resize_volume(dwi)

    # NumPy -> Tensor

    tensor = torch.from_numpy(dwi).float()

    tensor = tensor.unsqueeze(0)

    tensor = tensor.unsqueeze(0)

    return tensor