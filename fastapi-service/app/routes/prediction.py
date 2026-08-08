from pathlib import Path
import shutil
import tempfile
import traceback

from fastapi import APIRouter, File, HTTPException, UploadFile, status

from app.schemas.prediction import PredictionResponse
from app.services.inference import predict


router = APIRouter(
    prefix="/predict",
    tags=["Prediction"],
)


@router.post(
    "/",
    response_model=PredictionResponse,
    status_code=status.HTTP_200_OK,
    summary="Predict Stroke Lesion",
    description="""
Upload a DWI MRI (.nii.gz) and return the predicted
ischemic stroke lesion segmentation.
""",
)
async def predict_stroke(
    file: UploadFile = File(
        ...,
        description="DWI MRI (.nii.gz)",
    )
):
    """
    Predict ischemic stroke lesion from a DWI MRI.
    """

    # ---------------------------------------------------------
    # Validate uploaded file
    # ---------------------------------------------------------

    if not file.filename.endswith(".nii.gz"):

        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only .nii.gz files are supported.",
        )

    # ---------------------------------------------------------
    # Save temporary file
    # ---------------------------------------------------------

    with tempfile.TemporaryDirectory() as temp_dir:

        temp_path = Path(temp_dir) / file.filename

        with open(temp_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        try:

            prediction = predict(temp_path)

            return PredictionResponse(
                status="success",
                filename=file.filename,
                prediction_shape=list(prediction.shape),
            )

        except Exception as e:
            traceback.print_exc()
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=str(e),
            )