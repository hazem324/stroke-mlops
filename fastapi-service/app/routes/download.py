from pathlib import Path

from fastapi import (
    APIRouter,
    HTTPException,
)
from fastapi.responses import FileResponse


router = APIRouter(
    prefix="/outputs",
    tags=["Outputs"],
)


OUTPUT_DIR = Path("outputs")


@router.get(
    "/{filename}",
)
async def download_output(
    filename: str,
):

    # Security: don't allow paths such as ../../
    safe_filename = Path(
        filename
    ).name

    file_path = (
        OUTPUT_DIR
        / safe_filename
    )

    if not file_path.exists():
        raise HTTPException(
            status_code=404,
            detail="Output file not found.",
        )

    if not file_path.is_file():
        raise HTTPException(
            status_code=404,
            detail="Output file not found.",
        )

    if safe_filename.endswith(
        ".nii.gz"
    ):

        media_type = (
            "application/gzip"
        )

    elif safe_filename.endswith(
        ".png"
    ):

        media_type = "image/png"

    else:

        media_type = (
            "application/octet-stream"
        )

    return FileResponse(
        path=file_path,
        media_type=media_type,
        filename=safe_filename,
    )