from pathlib import Path

import torch
from torch import nn

from app.core.config import get_settings
from app.ml.model import create_model


# Global model cache
_MODEL: nn.Module | None = None


# Load Model

def load_model() -> nn.Module:

    settings = get_settings()

    device = torch.device(settings.device)

    model_path = Path(settings.model_path)

    print("=" * 60)
    print("Loading model...")
    print(f"Model Path : {model_path}")
    print(f"Device     : {device}")
    print("=" * 60)

    if not model_path.exists():
        raise FileNotFoundError(
            f"Model file not found: {model_path}"
        )

    # Create model architecture
    model = create_model()

    # Load trained weights
    state_dict = torch.load(
        model_path,
        map_location=device,
        weights_only=True,
    )

    model.load_state_dict(state_dict)

    model.to(device)

    model.eval()

    print("Model loaded successfully.")

    return model


# Get Cached Model

def get_model() -> nn.Module:

    global _MODEL

    if _MODEL is None:

        _MODEL = load_model()

    return _MODEL