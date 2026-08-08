from pydantic import BaseModel, Field


class PredictionResponse(BaseModel):
    status: str = Field(
        ...,
        example="success",
        description="Prediction status",
    )

    filename: str = Field(
        ...,
        example="patient001_dwi.nii.gz",
        description="Uploaded MRI filename",
    )

    prediction_shape: list[int] = Field(
        ...,
        example=[128, 128, 64],
        description="Shape of the predicted segmentation",
    )