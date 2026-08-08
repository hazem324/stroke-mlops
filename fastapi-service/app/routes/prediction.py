from pathlib import Path
import shutil
import tempfile
import uuid

from fastapi import (
    APIRouter,
    File,
    HTTPException,
    UploadFile,
    status,
)

from app.schemas.prediction import PredictionResponse
from app.services.inference import predict


router = APIRouter(
    prefix="/predict",
    tags=["Prediction"],
)


OUTPUT_DIR = Path("outputs")
OUTPUT_DIR.mkdir(
    parents=True,
    exist_ok=True,
)


@router.post(
    "/",
    response_model=PredictionResponse,
    status_code=status.HTTP_200_OK,
    summary="Predict Stroke Lesion",
    description=(
        "Upload a DWI MRI (.nii.gz) and "
        "generate a stroke lesion segmentation."
    ),
)
async def predict_stroke(
    file: UploadFile = File(
        ...,
        description="DWI MRI (.nii.gz)",
    )
):

    # ======================================================
    # Validate file extension
    # ======================================================

    if not file.filename.endswith(".nii.gz"):

        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only .nii.gz files are supported.",
        )

    # ======================================================
    # Save uploaded file temporarily
    # ======================================================

    with tempfile.TemporaryDirectory() as temp_dir:

        temp_path = (
            Path(temp_dir) / file.filename
        )

        with open(temp_path, "wb") as buffer:

            shutil.copyfileobj(
                file.file,
                buffer,
            )

        try:

            # ==================================================
            # Create unique prediction filename
            # ==================================================

            prediction_filename = (
                f"prediction_{uuid.uuid4().hex}.nii.gz"
            )

            prediction_path = (
                OUTPUT_DIR / prediction_filename
            )

            # ==================================================
            # Run inference
            # ==================================================

            prediction = predict(
                temp_path,
                prediction_path,
            )

            # ==================================================
            # Return metadata
            # ==================================================

            return PredictionResponse(
                status="success",
                filename=file.filename,
                prediction_file=prediction_filename,
                prediction_shape=list(
                    prediction.shape
                ),
            )

        except Exception as e:

            raise HTTPException(
                status_code=(
                    status.HTTP_500_INTERNAL_SERVER_ERROR
                ),
                detail=str(e),
            )