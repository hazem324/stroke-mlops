from pathlib import Path
import shutil
import tempfile
import time
import uuid
import traceback

from fastapi import (
    APIRouter,
    File,
    HTTPException,
    UploadFile,
    status,
)

from app.schemas.prediction import PredictionResponse
from app.services.inference import predict
from app.services.lesion_analysis import analyze_lesion
from app.services.visualization import create_prediction_preview


router = APIRouter(
    prefix="/predict",
    tags=["Prediction"],
)


OUTPUT_DIR = Path("outputs")

OUTPUT_DIR.mkdir(
    parents=True,
    exist_ok=True,
)


# ============================================================
# TEST CONNECTION
# ============================================================

@router.post("/test")
async def test_prediction_connection(
    file: UploadFile = File(...)
):

    print()
    print("============================================================")
    print("🔵 FASTAPI /predict/test")
    print("============================================================")

    print(f"Filename       : {file.filename}")
    print(f"Content-Type   : {file.content_type}")

    content = await file.read()

    print(f"Received size  : {len(content)} bytes")

    print("============================================================")
    print("🟢 /predict/test SUCCESS")
    print("============================================================")
    print()

    return {
        "status": "success",
        "message": "Spring Boot reached FastAPI successfully",
        "filename": file.filename,
        "content_type": file.content_type,
        "size_bytes": len(content),
    }


# ============================================================
# REAL PREDICTION
# ============================================================

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

    print()
    print("============================================================")
    print("🚀 FASTAPI /predict/")
    print("============================================================")

    start_time = time.perf_counter()

    # --------------------------------------------------------
    # 1. FILE INFORMATION
    # --------------------------------------------------------

    print()
    print("[1] FILE RECEIVED")

    print(f"Filename       : {file.filename}")
    print(f"Content-Type   : {file.content_type}")

    # --------------------------------------------------------
    # 2. VALIDATE FILENAME
    # --------------------------------------------------------

    print()
    print("[2] VALIDATING FILE")

    if not file.filename:

        print("❌ ERROR: Filename is missing")

        raise HTTPException(
            status_code=400,
            detail="Filename is required.",
        )

    print(f"Filename       : {file.filename}")

    if not file.filename.lower().endswith(".nii.gz"):

        print("❌ ERROR: Invalid extension")
        print(f"Received       : {file.filename}")

        raise HTTPException(
            status_code=400,
            detail="Only .nii.gz NIfTI files are supported.",
        )

    print("✅ Extension OK")

    # --------------------------------------------------------
    # 3. TEMPORARY DIRECTORY
    # --------------------------------------------------------

    with tempfile.TemporaryDirectory() as temp_dir:

        print()
        print("[3] TEMPORARY DIRECTORY")

        print(f"Temp directory : {temp_dir}")

        temp_path = Path(temp_dir) / file.filename

        print(f"Temp file      : {temp_path}")

        try:

            # ------------------------------------------------
            # 4. SAVE UPLOADED FILE
            # ------------------------------------------------

            print()
            print("[4] SAVING UPLOADED FILE")

            with open(temp_path, "wb") as buffer:

                shutil.copyfileobj(
                    file.file,
                    buffer,
                )

            print("✅ File written")

            print(f"Exists         : {temp_path.exists()}")

            if temp_path.exists():

                print(
                    f"Size           : {temp_path.stat().st_size} bytes"
                )

            # ------------------------------------------------
            # 5. CREATE PREDICTION ID
            # ------------------------------------------------

            print()
            print("[5] CREATING OUTPUT FILES")

            prediction_id = uuid.uuid4().hex

            print(f"Prediction ID  : {prediction_id}")

            prediction_filename = (
                f"prediction_{prediction_id}.nii.gz"
            )

            overlay_filename = (
                f"prediction_overlay_{prediction_id}.nii.gz"
            )

            preview_filename = (
                f"prediction_{prediction_id}.png"
            )

            prediction_path = (
                OUTPUT_DIR / prediction_filename
            )

            overlay_path = (
                OUTPUT_DIR / overlay_filename
            )

            preview_path = (
                OUTPUT_DIR / preview_filename
            )

            print(f"Prediction     : {prediction_path}")
            print(f"Overlay        : {overlay_path}")
            print(f"Preview        : {preview_path}")

            print(f"Output dir     : {OUTPUT_DIR.absolute()}")

            # ------------------------------------------------
            # 6. CALL MODEL
            # ------------------------------------------------

            print()
            print("============================================================")
            print("🧠 [6] CALLING INFERENCE MODEL")
            print("============================================================")

            print(f"Input          : {temp_path}")
            print(f"Prediction out : {prediction_path}")
            print(f"Overlay out    : {overlay_path}")

            inference_start = time.perf_counter()

            result = predict(
                temp_path,
                prediction_path,
                overlay_path,
            )

            inference_time = (
                time.perf_counter()
                - inference_start
            )

            print()
            print("✅ INFERENCE FINISHED")

            print(
                f"Inference time : {inference_time:.3f} seconds"
            )

            print(
                f"Result type    : {type(result)}"
            )

            print(
                f"Result keys    : {result.keys()}"
            )

            # ------------------------------------------------
            # 7. EXTRACT PREDICTION
            # ------------------------------------------------

            print()
            print("[7] EXTRACTING MODEL RESULT")

            prediction = result["prediction"]

            original_volume = result["original_volume"]

            original_image = result["original_image"]

            print(
                f"Prediction type  : {type(prediction)}"
            )

            print(
                f"Prediction shape : {prediction.shape}"
            )

            print(
                f"Original volume  : {type(original_volume)}"
            )

            print(
                f"Original image   : {type(original_image)}"
            )

            # ------------------------------------------------
            # 8. LESION ANALYSIS
            # ------------------------------------------------

            print()
            print("============================================================")
            print("🔬 [8] ANALYZING LESION")
            print("============================================================")

            lesion_start = time.perf_counter()

            lesion = analyze_lesion(
                prediction,
                original_image,
            )

            lesion_time = (
                time.perf_counter()
                - lesion_start
            )

            print("✅ LESION ANALYSIS FINISHED")

            print(
                f"Lesion result : {lesion}"
            )

            print(
                f"Lesion time   : {lesion_time:.3f} seconds"
            )

            # ------------------------------------------------
            # 9. CREATE PREVIEW
            # ------------------------------------------------

            print()
            print("============================================================")
            print("🖼️ [9] CREATING PREVIEW")
            print("============================================================")

            preview_start = time.perf_counter()

            preview_slice = create_prediction_preview(
                original_volume,
                prediction,
                preview_path,
            )

            preview_time = (
                time.perf_counter()
                - preview_start
            )

            print("✅ PREVIEW CREATED")

            print(
                f"Preview slice : {preview_slice}"
            )

            print(
                f"Preview path  : {preview_path}"
            )

            print(
                f"Preview exists: {preview_path.exists()}"
            )

            print(
                f"Preview time  : {preview_time:.3f} seconds"
            )

            # ------------------------------------------------
            # 10. EXECUTION TIME
            # ------------------------------------------------

            execution_time = (
                time.perf_counter()
                - start_time
            )

            print()
            print("============================================================")
            print("🎉 PREDICTION SUCCESS")
            print("============================================================")

            print(
                f"Total time    : {execution_time:.3f} seconds"
            )

            print(
                f"Prediction    : {prediction_filename}"
            )

            print(
                f"Overlay       : {overlay_filename}"
            )

            print(
                f"Preview       : {preview_filename}"
            )

            print("============================================================")
            print()

            # ------------------------------------------------
            # 11. RETURN RESPONSE
            # ------------------------------------------------

            return PredictionResponse(
                status="success",
                filename=file.filename,
                overlay_file=overlay_filename,
                prediction_file=prediction_filename,
                preview_file=preview_filename,
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

        except HTTPException as exc:

            print()
            print("============================================================")
            print("⚠️ HTTP EXCEPTION")
            print("============================================================")

            print(f"Status code : {exc.status_code}")
            print(f"Detail      : {exc.detail}")

            print("============================================================")
            print()

            raise

        except Exception as exc:

            print()
            print("============================================================")
            print("🔥 REAL PREDICTION ERROR")
            print("============================================================")

            print(f"Exception type : {type(exc).__name__}")
            print(f"Exception      : {exc}")

            print()
            print("---------- TRACEBACK ----------")

            traceback.print_exc()

            print("---------- END TRACEBACK ----------")

            print()
            print("============================================================")
            print()

            raise HTTPException(
                status_code=500,
                detail=f"Inference failed: {str(exc)}",
            ) from exc