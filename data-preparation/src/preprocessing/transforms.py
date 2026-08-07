from typing import Dict, Tuple

import numpy as np
from scipy.ndimage import zoom

from preprocessing.constants import TARGET_SHAPE, CROP_MARGIN


def crop_to_foreground(
    volumes: Dict[str, np.ndarray],
    reference_key: str = "adc",
    margin: int = CROP_MARGIN,
) -> Dict[str, np.ndarray]:
    
    ref = volumes[reference_key]

    coords = np.argwhere(ref > 0)

    if coords.size == 0:
        return volumes

    mins = np.maximum(coords.min(axis=0) - margin, 0)
    maxs = np.minimum(coords.max(axis=0) + margin, ref.shape)

    slices = tuple(
        slice(mn, mx)
        for mn, mx in zip(mins, maxs)
    )

    return {
        key: volume[slices]
        for key, volume in volumes.items()
    }


def normalize_volume(
    volume: np.ndarray,
) -> np.ndarray:
    
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

    normalized[foreground_mask] = (
        volume[foreground_mask] - mean
    ) / std

    return normalized


def resize_volume(
    volume: np.ndarray,
    target_shape: Tuple[int, int, int] = TARGET_SHAPE,
    is_mask: bool = False,
) -> np.ndarray:
    

    factors = [
        target_shape[i] / volume.shape[i]
        for i in range(3)
    ]

    interpolation_order = 0 if is_mask else 1

    resized = zoom(
        volume,
        zoom=factors,
        order=interpolation_order,
    )

    if is_mask:

        resized = np.round(resized)

        resized = np.clip(
            resized,
            0,
            1,
        )

    return resized.astype(np.float32)