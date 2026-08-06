from fastapi import FastAPI


app = FastAPI(
    title="Stroke MRI Segmentation API",
    description="API REST for ischemic stroke lesion segmentation.",
    version="1.0.0"
)


@app.get("/")
def root():
    return {
        "message": "Stroke MRI Segmentation API is running"
    }