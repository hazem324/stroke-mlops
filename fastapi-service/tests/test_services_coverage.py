from pathlib import Path

import numpy as np
import pytest
import SimpleITK as sitk
import torch
from fastapi import HTTPException

from app.ml import model_loader
from app.routes.download import download_output
from app.routes.prediction import get_output_file
from app.services import inference, lesion_analysis, visualization


def test_lesion_analysis_empty_and_populated():
    image = sitk.Image(5, 5, 5, sitk.sitkFloat32)
    image.SetSpacing((2.0, 3.0, 4.0))

    empty = lesion_analysis.analyze_lesion(np.zeros((3, 3, 3), dtype=np.uint8), image)
    assert empty["detected"] is False
    assert empty["centroid"] is None

    mask = np.zeros((3, 3, 3), dtype=np.uint8)
    mask[1, 1, 1] = 1
    mask[2, 1, 1] = 1
    result = lesion_analysis.analyze_lesion(mask, image)
    assert result["detected"] is True
    assert result["voxel_count"] == 2
    assert result["volume_mm3"] == 48.0
    assert result["bounding_box"]["max_x"] == 2
    assert result["centroid"]["physical"]["x"] == 4.0


def test_visualization_helpers_and_preview(tmp_path):
    empty = np.zeros((4, 4, 6), dtype=np.float32)
    assert visualization.find_best_slice(empty) == 3
    assert np.all(visualization.normalize_for_display(empty) == 0)

    constant = np.ones((4, 4), dtype=np.float32)
    assert np.all(visualization.normalize_for_display(constant) == 0)

    mask = np.zeros_like(empty, dtype=np.uint8)
    mask[:, :, 1] = 1
    assert visualization.find_best_slice(mask) == 1
    output = tmp_path / "preview.png"
    assert visualization.create_prediction_preview(empty + 1, mask, output) == 1
    assert output.exists()


def test_inference_array_helpers_and_nifti_writes(tmp_path):
    volume = np.zeros((4, 5, 6), dtype=np.float32)
    volume[1:3, 1:4, 2:5] = np.arange(18).reshape(2, 3, 3) + 1
    cropped, slices = inference.crop_to_foreground(volume, margin=1)
    assert cropped.shape == (4, 5, 5)
    assert slices[0].start == 0
    assert inference.crop_to_foreground(np.zeros((2, 2, 2)))[1][0] == slice(0, 2)

    normalized = inference.normalize_volume(volume)
    assert np.isclose(normalized[volume > 0].mean(), 0, atol=1e-6)
    assert np.array_equal(inference.normalize_volume(np.zeros((2, 2, 2))), np.zeros((2, 2, 2)))
    assert np.array_equal(inference.normalize_volume(np.ones((2, 2, 2))), np.ones((2, 2, 2)))

    resized = inference.resize_volume(np.ones((2, 2, 2), dtype=np.float32))
    assert resized.shape == inference.TARGET_SHAPE
    prediction = np.ones((4, 4, 4), dtype=np.uint8)
    restored = inference.restore_mask_to_original(prediction, volume.shape, slices)
    assert restored.shape == volume.shape
    assert set(np.unique(restored)).issubset({0, 1})
    assert inference.numpy_to_tensor(np.zeros((2, 3, 4))).shape == (1, 1, 2, 3, 4)

    reference = sitk.Image(6, 5, 4, sitk.sitkFloat32)
    prediction_path = tmp_path / "prediction.nii.gz"
    overlay_path = tmp_path / "overlay.nii.gz"
    prediction_data = np.zeros((6, 5, 4), dtype=np.uint8)
    prediction_data[1, 1, 1] = 1
    inference.save_prediction_as_nifti(prediction_data, reference, prediction_path)
    inference.save_overlay_as_nifti(np.ones_like(prediction_data, dtype=np.float32), prediction_data, reference, overlay_path)
    assert prediction_path.exists()
    assert overlay_path.exists()


def test_inference_pipeline_with_small_reference_image(monkeypatch, tmp_path):
    class FakeModel(torch.nn.Module):
        def __init__(self):
            super().__init__()
            self.device_marker = torch.nn.Parameter(torch.zeros(1))

        def forward(self, tensor):
            return torch.ones((1, 1, 128, 128, 64), device=tensor.device)

    image = sitk.Image(4, 4, 4, sitk.sitkFloat32)
    image_array = np.zeros((4, 4, 4), dtype=np.float32)
    image_array[1:3, 1:3, 1:3] = 2
    image = sitk.GetImageFromArray(np.transpose(image_array, (2, 1, 0)))
    source = tmp_path / "input.nii.gz"
    sitk.WriteImage(image, str(source))

    monkeypatch.setattr(inference, "get_model", lambda: FakeModel())
    result = inference.predict(source, tmp_path / "prediction.nii.gz", tmp_path / "overlay.nii.gz")
    assert result["prediction"].shape == image_array.shape
    assert (tmp_path / "prediction.nii.gz").exists()
    assert (tmp_path / "overlay.nii.gz").exists()


def test_model_architecture_factory(monkeypatch):
    class FakeUNet:
        def __init__(self, **kwargs):
            self.kwargs = kwargs

    from app.ml import model
    monkeypatch.setattr(model, "UNet", FakeUNet)
    created = model.create_model()
    assert created.kwargs["spatial_dims"] == 3
    assert created.kwargs["out_channels"] == 1


def test_model_loader_missing_and_cached(monkeypatch, tmp_path):
    monkeypatch.setattr(model_loader, "_MODEL", None)
    settings = model_loader.get_settings()
    monkeypatch.setattr(settings, "model_path", str(tmp_path / "missing.pth"))
    with pytest.raises(FileNotFoundError):
        model_loader.load_model()

    cached = torch.nn.Identity()
    monkeypatch.setattr(model_loader, "_MODEL", cached)
    assert model_loader.get_model() is cached


@pytest.mark.asyncio
async def test_file_routes_reject_missing_and_traversal(tmp_path, monkeypatch):
    with pytest.raises(HTTPException) as missing:
        await get_output_file("missing.nii.gz", str(tmp_path))
    assert missing.value.status_code == 404

    with pytest.raises(HTTPException) as traversal:
        await get_output_file("../secret.nii.gz", str(tmp_path))
    assert traversal.value.status_code == 400

    import app.routes.download as download_module
    monkeypatch.setattr(download_module, "OUTPUT_DIR", tmp_path)
    with pytest.raises(HTTPException) as download_missing:
        await download_output("missing.png")
    assert download_missing.value.status_code == 404
    output = tmp_path / "result.png"
    output.write_bytes(b"png")
    response = await download_output("result.png")
    assert response.media_type == "image/png"


def test_prediction_openapi_documents_internal_error():
    import app.routes.prediction as prediction_module
    route = next(route for route in prediction_module.router.routes if route.endpoint.__name__ == "predict_stroke")
    assert 500 in route.responses
    assert "Internal server error" in route.responses[500]["description"]
