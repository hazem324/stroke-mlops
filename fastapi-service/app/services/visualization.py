from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np


def find_best_slice(
    mask: np.ndarray,
) -> int:

    lesion_per_slice = (
        mask > 0
    ).sum(axis=(0, 1))

    if lesion_per_slice.max() == 0:
        return mask.shape[2] // 2

    return int(
        lesion_per_slice.argmax()
    )

# Normalize an image for visualization 
def normalize_for_display( image: np.ndarray, ) -> np.ndarray:

    non_zero = image[
        image > 0
    ]

    if non_zero.size == 0:
        return np.zeros_like(
            image,
            dtype=np.float32,
        )

    low = np.percentile(
        non_zero,
        1,
    )

    high = np.percentile(
        non_zero,
        99,
    )

    if high <= low:
        return np.zeros_like(
            image,
            dtype=np.float32,
        )

    result = (
        image - low
    ) / (
        high - low
    )

    return np.clip(
        result,
        0,
        1,
    )

# Generate a PNG containing:  DWI + predicted lesion overlay
def create_prediction_preview( dwi: np.ndarray, mask: np.ndarray, output_path: Path, ) -> int:

    slice_index = find_best_slice(
        mask
    )

    dwi_slice = dwi[
        :, :,
        slice_index
    ]

    mask_slice = mask[
        :, :,
        slice_index
    ]

    dwi_display = normalize_for_display(
        dwi_slice
    )

    fig, axes = plt.subplots(
        1,
        2,
        figsize=(10, 5),
    )

    # ------------------------------------------------------
    # DWI
    # ------------------------------------------------------

    axes[0].imshow(
        dwi_display.T,
        cmap="gray",
        origin="lower",
        interpolation="nearest",
    )

    axes[0].set_title(
        "DWI"
    )

    axes[0].axis("off")

    # ------------------------------------------------------
    # Prediction
    # ------------------------------------------------------

    axes[1].imshow(
        dwi_display.T,
        cmap="gray",
        origin="lower",
        interpolation="nearest",
    )

    # Lesion overlay
    overlay = np.ma.masked_where(
        mask_slice.T == 0,
        mask_slice.T,
    )

    axes[1].imshow(
        overlay,
        cmap="autumn",
        alpha=0.65,
        origin="lower",
        interpolation="nearest",
    )

    axes[1].set_title(
        "Prediction"
    )

    axes[1].axis("off")

    fig.suptitle(
        "DWI Stroke Lesion Segmentation"
    )

    plt.tight_layout()

    fig.savefig(
        output_path,
        dpi=150,
        bbox_inches="tight",
    )

    plt.close(fig)

    return slice_index