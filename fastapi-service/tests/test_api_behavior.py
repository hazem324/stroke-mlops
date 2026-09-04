from io import BytesIO

import numpy as np

from app.core.config import get_settings
from app.main import app
from app.routes import prediction as prediction_module
from fastapi.testclient import TestClient

client = TestClient(app)
settings = get_settings()


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "healthy"
    assert payload["service"] == settings.app_name


def test_prediction_rejects_non_nii_input():
    response = client.post(
        "/predict/",
        files={"file": ("not-a-volume.txt", b"not a nifti image", "text/plain")},
    )

    assert response.status_code == 400
    assert "Only .nii.gz files are supported" in response.json()["detail"]


def test_prediction_route_returns_success_for_mocked_inference(monkeypatch, tmp_path):
    monkeypatch.setattr(prediction_module, "OUTPUT_DIR", str(tmp_path))

    def fake_predict(original_volume, prediction_path, overlay_path):
        prediction = np.zeros((2, 2, 2), dtype=np.uint8)
        prediction[0, 0, 0] = 1
        return prediction

    def fake_analyze_lesion(prediction_mask, original_volume):
        return {
            "detected": True,
            "voxel_count": 1,
            "volume_mm3": 1.0,
            "centroid": {"x": 0.0, "y": 0.0, "z": 0.0},
            "bounding_box": {"min_x": 0, "max_x": 0, "min_y": 0, "max_y": 0, "min_z": 0, "max_z": 0},
        }

    monkeypatch.setattr(prediction_module, "predict", fake_predict)
    monkeypatch.setattr(prediction_module, "analyze_lesion", fake_analyze_lesion)
    monkeypatch.setattr(prediction_module, "create_prediction_preview", lambda *args, **kwargs: str(tmp_path / "preview.png"))

    payload = BytesIO(b"fake nii contents")
    response = client.post(
        "/predict/",
        files={"file": ("sample.nii.gz", payload, "application/octet-stream")},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "success"
    assert body["filename"] == "sample.nii.gz"
    assert body["prediction_shape"] == [2, 2, 2]
    assert body["lesion_count"] == 1


def test_prediction_file_download_serves_generated_file(tmp_path):
    target = tmp_path / "generated.nii.gz"
    target.write_bytes(b"generated-content")

    response = client.get(f"/predict/files/{target.name}", params={"output_dir": str(tmp_path)})

    assert response.status_code == 200
    assert response.content == b"generated-content"
