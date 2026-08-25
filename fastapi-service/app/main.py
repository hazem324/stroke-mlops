from fastapi import FastAPI, Request

from app.core.config import get_settings
from app.routes.health import router as health_router
from app.routes.prediction import router as prediction_router
from app.routes.download import router as download_router


settings = get_settings()


app = FastAPI(
    title=settings.app_name,
    description=settings.app_description,
    version=settings.app_version,
    debug=settings.debug,
)


# ============================================================
# DEBUG HTTP
# ============================================================

@app.middleware("http")
async def debug_request(
    request: Request,
    call_next
):

    print()
    print("=" * 80)
    print("FASTAPI RECEIVED REQUEST")
    print("=" * 80)

    print("METHOD:")
    print(request.method)

    print()

    print("URL:")
    print(request.url)

    print()

    print("HEADERS:")

    for name, value in request.headers.items():

        print(
            f"{name}: {value}"
        )

    print()

    print("CONTENT-TYPE:")

    print(
        request.headers.get(
            "content-type"
        )
    )

    print()

    print("CONTENT-LENGTH:")

    print(
        request.headers.get(
            "content-length"
        )
    )

    print("=" * 80)

    response = await call_next(request)

    print()
    print("=" * 80)

    print("FASTAPI RESPONSE")

    print("=" * 80)

    print(
        "STATUS:",
        response.status_code
    )

    print("=" * 80)
    print()

    return response


# ============================================================
# ROUTERS
# ============================================================

app.include_router(
    health_router
)

app.include_router(
    prediction_router
)

# app.include_router(download_router)