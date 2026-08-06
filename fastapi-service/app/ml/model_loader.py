from pathlib import Path

import torch
from torch import nn

from app.core.config import get_settings
from app.ml.model import create_model


def load_model() -> nn.Module:
    """
    Load the trained 3D U-Net model for inference.
    """

    settings = get_settings()

    # Select inference device
    device = torch.device(settings.device)

    # Check that the model file exists
    model_path = Path(settings.model_path)

    if not model_path.exists():
        raise FileNotFoundError(
            f"Model file not found: {model_path}"
        )

    # Recreate the same U-Net architecture used during training
    model = create_model()

    # Load the state_dict saved during training
    state_dict = torch.load(
        model_path,
        map_location=device,
        weights_only=True,
    )

    # Inject trained weights into the U-Net
    model.load_state_dict(state_dict)

    # Move model to inference device
    model.to(device)

    # Switch from training mode to inference mode
    model.eval()

    return model