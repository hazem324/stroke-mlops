from pathlib import Path
from typing import Dict, Optional

import SimpleITK as sitk


def load_volumes(paths: Dict[str, Optional[Path]]) -> Dict[str, sitk.Image]:
   
    volumes = {}

    for modality, path in paths.items():

        if path is None or not path.exists():
            continue

        volumes[modality] = sitk.ReadImage(str(path))

    return volumes


def resample_to_reference(
    moving: sitk.Image,
    reference: sitk.Image,
    is_mask: bool = False,
) -> sitk.Image:

    resampler = sitk.ResampleImageFilter()

    resampler.SetReferenceImage(reference)

    resampler.SetInterpolator(
        sitk.sitkNearestNeighbor
        if is_mask
        else sitk.sitkLinear
    )

    return resampler.Execute(moving)