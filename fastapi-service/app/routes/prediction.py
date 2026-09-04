from pathlib import Path
import shutil
import tempfile
import time
import uuid
import traceback

import numpy as np
from fastapi.responses import FileResponse

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


@router.get("/files/{filename}")
async def get_output_file(filename: str, output_dir: str | None = None):
    """
    Serves a generated output file (prediction/overlay/preview) so
    Spring Boot can download it right after /predict/ returns.
    """
    base_dir = Path(output_dir) if output_dir else Path(OUTPUT_DIR)
    file_path = base_dir / filename

    if not file_path.resolve().is_relative_to(base_dir.resolve()):
        raise HTTPException(status_code=400, detail="Invalid filename")

    if not file_path.exists():
        raise HTTPException(status_code=404, detail="File not found")

    return FileResponse(
        path=file_path,
        filename=filename,
        media_type="application/octet-stream",
    )

# TEST CONNECTION
@router.post("/test")
async def test_prediction_connection(
    file: UploadFile = File(...)
):

    print()
    print("============================================================")
    print("FASTAPI /predict/test")
    print("============================================================")

    print(f"Filename       : {file.filename}")
    print(f"Content-Type   : {file.content_type}")

    content = await file.read()

    print(f"Received size  : {len(content)} bytes")

    print("============================================================")
    print("/predict/test SUCCESS")
    print("============================================================")
    print()

    return {
        "status": "success",
        "message": "Spring Boot reached FastAPI successfully",
        "filename": file.filename,
        "content_type": file.content_type,
        "size_bytes": len(content),
    }


# REAL PREDICTION
@router.post( "/", response_model=PredictionResponse, status_code=status.HTTP_200_OK,)
async def predict_stroke(
    file: UploadFile = File(
        ...,
        description="DWI MRI (.nii.gz)",
    ),
):

    print()
    print("============================================================")
    print(" FASTAPI /predict/")
    print("============================================================")

    start_time = time.perf_counter()

    # 1. FILE INFORMATION
    print()
    print(" FILE RECEIVED")

    print(f"Filename       : {file.filename}")
    print(f"Content-Type   : {file.content_type}")

    # 2. VALIDATE FILENAME
    print()
    print("[2] VALIDATING FILE")

    if not file.filename:

        print("ERROR: Filename is missing")

        raise HTTPException(
            status_code=400,
            detail="Filename is required.",
        )

    print(f"Filename       : {file.filename}")

    if not file.filename.lower().endswith(".nii.gz"):

        print("ERROR: Invalid extension")
        print(f"Received       : {file.filename}")

        raise HTTPException(
            status_code=400,
            detail="Only .nii.gz files are supported",
        )

    print(" Extension OK")

    # 3. TEMPORARY DIRECTORY
    with tempfile.TemporaryDirectory() as temp_dir:

        print()
        print("[3] TEMPORARY DIRECTORY")

        print(f"Temp directory : {temp_dir}")

        temp_path = Path(temp_dir) / file.filename

        print(f"Temp file      : {temp_path}")

        try:

            # 4. SAVE UPLOADED FILE
            print()
            print("[4] SAVING UPLOADED FILE")

            with open(temp_path, "wb") as buffer:

                shutil.copyfileobj(
                    file.file,
                    buffer,
                )

            print("File written")

            print(f"Exists         : {temp_path.exists()}")

            if temp_path.exists():

                print(f"Size           : {temp_path.stat().st_size} bytes")

            # 5. CREATE PREDICTION ID
            print()
            print("CREATING OUTPUT FILES")

            prediction_id = uuid.uuid4().hex

            print(f"Prediction ID  : {prediction_id}")
            output_dir = Path(OUTPUT_DIR)
            prediction_filename = (f"prediction_{prediction_id}.nii.gz")
            overlay_filename = ( f"prediction_overlay_{prediction_id}.nii.gz")
            preview_filename = (f"prediction_{prediction_id}.png")
            prediction_path = (output_dir / prediction_filename)
            overlay_path = (output_dir / overlay_filename)
            preview_path = (output_dir / preview_filename )

            print(f"Prediction     : {prediction_path}")
            print(f"Overlay        : {overlay_path}")
            print(f"Preview        : {preview_path}")
            print(f"Output dir     : {output_dir.absolute()}")

            # 6. CALL MODEL
            print()
            print("============================================================")
            print("CALLING INFERENCE MODEL")
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

            inference_time = (time.perf_counter() - inference_start)
            print()
            print(" INFERENCE FINISHED")
            print( f"Inference time : {inference_time:.3f} seconds")
            print(f"Result type    : {type(result)}")

            # 7. EXTRACT PREDICTION
            print()
            print(" XTRACTING MODEL RESULT")

            if isinstance(result, dict):
                prediction = result.get("prediction")
                original_volume = result.get("original_volume")
                original_image = result.get("original_image")
            else:
                prediction = np.asarray(result)
                original_volume = prediction.astype(np.float32)
                original_image = original_volume

            if prediction is None:
                raise HTTPException(status_code=500, detail="Inference returned no prediction payload")

            print(f"Prediction type  : {type(prediction)}")
            print(f"Prediction shape : {prediction.shape}")
            print(f"Original volume  : {type(original_volume)}" )
            print( f"Original image   : {type(original_image)}")

            # 8. LESION ANALYSIS
            print()
            print("============================================================")
            print(" ANALYZING LESION")
            print("============================================================")

            lesion_start = time.perf_counter()

            lesion = analyze_lesion(
                prediction,
                original_image,
            )

            lesion_time = (time.perf_counter() - lesion_start)

            print("LESION ANALYSIS FINISHED")
            print( f"Lesion result : {lesion}" )
            print(f"Lesion time   : {lesion_time:.3f} seconds" )

            # 9. CREATE PREVIEW
            print()
            print("============================================================")
            print(" CREATING PREVIEW")
            print("============================================================")

            preview_start = time.perf_counter()

            preview_slice = create_prediction_preview(
                original_volume,
                prediction,
                preview_path,
            )

            if isinstance(preview_slice, str):
                preview_slice = 1

            preview_time = ( time.perf_counter() - preview_start)

            print(" PREVIEW CREATED")
            print( f"Preview slice : {preview_slice}" )
            print(f"Preview path  : {preview_path}" )
            print(f"Preview exists: {preview_path.exists()}" )
            print( f"Preview time  : {preview_time:.3f} seconds")

            # 10. EXECUTION TIME
            execution_time = (
                time.perf_counter()
                - start_time
            )

            print()
            print("============================================================")
            print(" PREDICTION SUCCESS")
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

            if isinstance(lesion, dict):
                centroid = lesion.get("centroid")
                if isinstance(centroid, dict) and {"x", "y", "z"}.issubset(centroid.keys()):
                    lesion = lesion.copy()
                    lesion["centroid"] = {
                        "index": centroid,
                        "physical": centroid,
                    }

            lesion_count = lesion.get("voxel_count", 0) if isinstance(lesion, dict) else getattr(lesion, "voxel_count", 0)

            # 11. RETURN RESPONSE
            return PredictionResponse(
                status="success",
                filename=file.filename,
                overlay_file=overlay_filename,
                prediction_file=prediction_filename,
                preview_file=preview_filename,
                prediction_shape=list(
                    prediction.shape
                ),
                preview_slice=int(preview_slice),
                lesion=lesion,
                lesion_count=int(lesion_count),
                execution_time_seconds=round(
                    execution_time,
                    3,
                ),
            )

        except HTTPException as exc:

            print()
            print("============================================================")
            print("HTTP EXCEPTION")
            print("============================================================")

            print(f"Status code : {exc.status_code}")
            print(f"Detail      : {exc.detail}")

            print("============================================================")
            print()

            raise

        except Exception as exc:

            print()
            print("============================================================")
            print(" REAL PREDICTION ERROR")
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