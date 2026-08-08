from pathlib import Path
import shutil
import tempfile
import time
import uuid

from fastapi import (
    APIRouter,
    File,
    HTTPException,
    UploadFile,
    status,
)

from app.schemas.prediction import (
    PredictionResponse,
)

from app.services.inference import predict

from app.services.lesion_analysis import (
    analyze_lesion,
)

from app.services.visualization import (
    create_prediction_preview,
)


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
)
async def predict_stroke(
    file: UploadFile = File(
        ...,
        description="DWI MRI (.nii.gz)",
    ),
):

    start_time = time.perf_counter()

    # ======================================================
    # Validate filename
    # ======================================================

    if not file.filename:

        raise HTTPException(
            status_code=400,
            detail="Filename is required.",
        )

    if not file.filename.lower().endswith(
        ".nii.gz"
    ):

        raise HTTPException(
            status_code=400,
            detail=(
                "Only .nii.gz NIfTI files "
                "are supported."
            ),
        )

    # Temporary uploaded file

    with tempfile.TemporaryDirectory() as temp_dir:

        temp_path = (
            Path(temp_dir)
            / file.filename
        )

        try:

            with open(
                temp_path,
                "wb",
            ) as buffer:

                shutil.copyfileobj(
                    file.file,
                    buffer,
                )

            # Unique output names

            prediction_id = (
                uuid.uuid4().hex
            )

            prediction_filename = (
                f"prediction_{prediction_id}.nii.gz"
            )

            preview_filename = (
                f"prediction_{prediction_id}.png"
            )

            prediction_path = (
                OUTPUT_DIR
                / prediction_filename
            )

            preview_path = (
                OUTPUT_DIR
                / preview_filename
            )

            # AI inference

            result = predict(
                temp_path,
                prediction_path,
            )

            prediction = result[
                "prediction"
            ]

            original_volume = result[
                "original_volume"
            ]

            original_image = result[
                "original_image"
            ]

            # ==================================================
            # Lesion analysis
            # ==================================================

            lesion = analyze_lesion(
                prediction,
                original_image,
            )

            # ==================================================
            # Visualization
            # ==================================================

            preview_slice = (
                create_prediction_preview(
                    original_volume,
                    prediction,
                    preview_path,
                )
            )

            # ==================================================
            # Execution time
            # ==================================================

            execution_time = (
                time.perf_counter()
                - start_time
            )

            # ==================================================
            # Response
            # ==================================================

            return PredictionResponse(
                status="success",
                filename=file.filename,
                prediction_file=(
                    prediction_filename
                ),
                preview_file=(
                    preview_filename
                ),
                prediction_shape=list(
                    prediction.shape
                ),
                preview_slice=preview_slice,
                lesion=lesion,
                execution_time_seconds=round(
                    execution_time,
                    3,
                ),
            )

        except HTTPException:
            raise

        except Exception as exc:

            raise HTTPException(
                status_code=500,
                detail=(
                    f"Inference failed: {str(exc)}"
                ),
            ) from exc