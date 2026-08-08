from typing import Any

from pydantic import BaseModel, Field


class CentroidIndex(BaseModel):

    x: float
    y: float
    z: float


class CentroidPhysical(BaseModel):

    x: float
    y: float
    z: float


class Centroid(BaseModel):

    index: CentroidIndex
    physical: CentroidPhysical


class BoundingBox(BaseModel):

    min_x: int
    max_x: int

    min_y: int
    max_y: int

    min_z: int
    max_z: int


class LesionAnalysis(BaseModel):

    detected: bool

    voxel_count: int

    volume_mm3: float

    centroid: Centroid | None = None

    bounding_box: BoundingBox | None = None


class PredictionResponse(BaseModel):

    status: str = Field(
        ...,
        example="success",
    )

    filename: str = Field(
        ...,
        example="patient001_dwi.nii.gz",
    )

    prediction_file: str = Field(
        ...,
        example="prediction_123456.nii.gz",
    )

    preview_file: str = Field(
        ...,
        example="prediction_123456.png",
    )

    prediction_shape: list[int] = Field(
        ...,
        example=[128, 128, 64],
    )

    preview_slice: int = Field(
        ...,
        example=32,
    )

    lesion: LesionAnalysis

    execution_time_seconds: float = Field(
        ...,
        example=2.31,
    )