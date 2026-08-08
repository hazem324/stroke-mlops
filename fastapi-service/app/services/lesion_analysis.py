from typing import Dict, Any

import numpy as np
import SimpleITK as sitk


#     Analyze the predicted stroke lesion.

def analyze_lesion( mask: np.ndarray, image: sitk.Image, ) -> Dict[str, Any]:

    lesion_voxels = np.argwhere(
        mask > 0
    )

    # No lesion

    if lesion_voxels.size == 0:

        return {
            "detected": False,
            "voxel_count": 0,
            "volume_mm3": 0.0,
            "centroid": None,
            "bounding_box": None,
        }

    # Number of voxels

    voxel_count = int(
        lesion_voxels.shape[0]
    )

    # Voxel spacing

    spacing = image.GetSpacing()

    voxel_volume = (
        spacing[0]
        * spacing[1]
        * spacing[2]
    )

    volume_mm3 = (
        voxel_count
        * voxel_volume
    )

    # Centroid in NumPy coordinates
    centroid_numpy = (
        lesion_voxels.mean(
            axis=0
        )
    )

    # Our NumPy volume is (x, y, z)
    centroid_index = {
        "x": float(
            centroid_numpy[0]
        ),
        "y": float(
            centroid_numpy[1]
        ),
        "z": float(
            centroid_numpy[2]
        ),
    }

    # Convert centroid to physical coordinates

    centroid_physical = image.TransformIndexToPhysicalPoint(
        (
            int(round(centroid_numpy[0])),
            int(round(centroid_numpy[1])),
            int(round(centroid_numpy[2])),
        )
    )

    # Bounding box

    minimum = lesion_voxels.min(
        axis=0
    )

    maximum = lesion_voxels.max(
        axis=0
    )

    bounding_box = {
        "min_x": int(minimum[0]),
        "max_x": int(maximum[0]),
        "min_y": int(minimum[1]),
        "max_y": int(maximum[1]),
        "min_z": int(minimum[2]),
        "max_z": int(maximum[2]),
    }

    return {
        "detected": True,
        "voxel_count": voxel_count,
        "volume_mm3": round(
            float(volume_mm3),
            2,
        ),
        "centroid": {
            "index": centroid_index,
            "physical": {
                "x": round(
                    float(
                        centroid_physical[0]
                    ),
                    2,
                ),
                "y": round(
                    float(
                        centroid_physical[1]
                    ),
                    2,
                ),
                "z": round(
                    float(
                        centroid_physical[2]
                    ),
                    2,
                ),
            },
        },
        "bounding_box": bounding_box,
    }