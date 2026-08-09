# Stroke MRI Segmentation API

FastAPI-based inference service for ischemic stroke lesion segmentation from
3D DWI MRI volumes using a trained 3D U-Net model.

This service is part of the `stroke-mlops` project and provides a REST API for
uploading DWI NIfTI volumes (`.nii.gz`), running the trained segmentation model,
analyzing the detected lesion, and generating visualization outputs.

---

## Project Overview

The service receives a DWI MRI volume in NIfTI format:

```text
DWI MRI (.nii.gz)
       |
       v
FastAPI /predict/
       |
       v
Preprocessing
  - Load NIfTI
  - Convert to NumPy
  - Crop foreground
  - Normalize intensity
  - Resize
       |
       v
PyTorch Tensor
       |
       v
  3D U-Net
       |
       v
Segmentation Mask
       |
       +--------------------+
       |                    |
       v                    v
Binary NIfTI Mask       Visualization PNG
       |
       v
Lesion Analysis
  - Detection
  - Voxel count
  - Lesion volume
  - Centroid
  - Bounding box
       |
       v
   JSON Response
```

---

## Features

* REST API built with FastAPI
* 3D U-Net inference using PyTorch and MONAI
* DWI-only stroke lesion segmentation
* NIfTI (`.nii.gz`) input support
* Automatic MRI preprocessing
* Model loading and caching
* Binary segmentation output
* DWI + lesion overlay generation
* PNG visualization
* Lesion volume calculation
* Lesion centroid calculation
* Lesion bounding box calculation
* Health check endpoint
* Output file download endpoint
* Automatic OpenAPI / Swagger documentation
* Pydantic response validation

---

## Model

The service uses a trained 3D U-Net model.

### Input

The model expects a single DWI modality:

```text
in_channels = 1
```

The preprocessing pipeline converts the input volume to:

```text
128 x 128 x 64
```

The tensor sent to the model has the shape:

```text
[batch, channel, x, y, z]
```

Example:

```text
[1, 1, 128, 128, 64]
```

### Output

The model produces a single-channel segmentation:

```text
out_channels = 1
```

The output is converted into probabilities using a sigmoid activation and
then thresholded to obtain a binary segmentation mask.

```text
probability > 0.5
    |
    +---- 0 = background
    |
    +---- 1 = predicted lesion
```

---

## Project Structure

```text
fastapi-service/
│
├── app/
│   │
│   ├── core/
│   │   └── config.py
│   │
│   ├── main.py
│   │
│   ├── ml/
│   │   ├── model.py
│   │   └── model_loader.py
│   │
│   ├── routes/
│   │   ├── download.py
│   │   ├── health.py
│   │   └── prediction.py
│   │
│   ├── schemas/
│   │   ├── health.py
│   │   └── prediction.py
│   │
│   └── services/
│       ├── inference.py
│       ├── lesion_analysis.py
│       └── visualization.py
│
├── models/
│   └── unet3d_dwi_best.pth
│
├── outputs/
│
├── tests/
│   └── test_health.py
│
├── requirements.txt
├── run.py
└── README.md
```

---

# Installation

## 1. Clone the Repository

Clone the main `stroke-mlops` repository:

```bash
git clone https://github.com/hazem324/stroke-mlops.git
```

Move to the FastAPI service:

```bash
cd stroke-mlops/fastapi-service
```

---

## 2. Create a Virtual Environment

Create a Python virtual environment:

```bash
python3 -m venv .venv
```

Activate the environment.

### Linux / macOS

```bash
source .venv/bin/activate
```

### Windows

```powershell
.venv\Scripts\activate
```

After activation, the terminal should display:

```text
(.venv)
```

---

## 3. Install Dependencies

Install all required Python libraries:

```bash
pip install -r requirements.txt
```

The main dependencies include:

* FastAPI
* Uvicorn
* PyTorch
* MONAI
* SimpleITK
* NumPy
* SciPy
* Pydantic
* Matplotlib

---

# Model Setup

The trained model must be available at:

```text
models/unet3d_dwi_best.pth
```

The expected structure is:

```text
fastapi-service/
└── models/
    └── unet3d_dwi_best.pth
```

The model is the trained DWI-only 3D U-Net used for ischemic stroke lesion
segmentation.

---

# Configuration

The application configuration is managed through:

```text
app/core/config.py
```

The configuration includes values such as:

```text
MODEL_PATH
DEVICE
APP_NAME
APP_VERSION
ENVIRONMENT
```

The model path must point to:

```text
models/unet3d_dwi_best.pth
```

The inference device can be configured as:

```text
cpu
```

or, when a compatible CUDA environment is available:

```text
cuda
```

---

# Start the FastAPI Service

After activating the virtual environment and installing the dependencies,
start the application with:

```bash
python run.py
```

The API will be available at:

```text
http://localhost:8000
```

Expected output:

```text
INFO:     Uvicorn running on http://0.0.0.0:8000
INFO:     Application startup complete.
```

---

# Swagger Documentation

FastAPI automatically generates interactive API documentation using Swagger
UI.

Once the service is running, open:

```text
http://localhost:8000/docs
```

The OpenAPI specification is also available at:

```text
http://localhost:8000/openapi.json
```

---

# Testing the API with Swagger

## 1. Open Swagger

Open your browser and navigate to:

```text
http://localhost:8000/docs
```

The available endpoints should include:

```text
GET  /health
POST /predict/
GET  /outputs/{filename}
```

---

## 2. Test the Health Endpoint

Find:

```text
GET /health
```

Click:

```text
Try it out
```

Then click:

```text
Execute
```

A successful response should look like:

```json
{
  "status": "healthy",
  "service": "Stroke MRI Segmentation API",
  "version": "1.0.0",
  "environment": "development"
}
```

This confirms that the FastAPI service is running correctly.

---

# Test Stroke Prediction

## 1. Open the Prediction Endpoint

In Swagger, find:

```text
POST /predict/
```

Click:

```text
Try it out
```

---

## 2. Upload a DWI MRI

The endpoint accepts a DWI MRI volume in NIfTI format:

```text
.nii.gz
```

Example:

```text
sub-strokecase0004_ses-0001_dwi.nii.gz
```

Click:

```text
Choose File
```

and select the DWI NIfTI file.

---

## 3. Execute the Prediction

Click:

```text
Execute
```

The API performs the complete inference pipeline:

```text
DWI .nii.gz
      |
      v
Load NIfTI
      |
      v
Convert to NumPy
      |
      v
Crop foreground
      |
      v
Normalize
      |
      v
Resize
      |
      v
Convert to PyTorch Tensor
      |
      v
3D U-Net
      |
      v
Sigmoid
      |
      v
Threshold
      |
      v
Binary Segmentation
      |
      v
Restore Original Space
      |
      +----------------------+
      |                      |
      v                      v
Lesion Analysis         Visualization
      |                      |
      +----------+-----------+
                 |
                 v
           JSON Response
```

---

# Example Prediction Response

A successful prediction returns a response similar to:

```json
{
  "status": "success",
  "filename": "sub-strokecase0004_ses-0001_dwi.nii.gz",
  "prediction_file": "prediction_74246290f71445b9b3ce22582ae1ee15.nii.gz",
  "overlay_file": "prediction_overlay_74246290f71445b9b3ce22582ae1ee15.nii.gz",
  "preview_file": "prediction_74246290f71445b9b3ce22582ae1ee15.png",
  "prediction_shape": [
    112,
    112,
    72
  ],
  "preview_slice": 39,
  "lesion": {
    "detected": true,
    "voxel_count": 233,
    "volume_mm3": 1864,
    "centroid": {
      "index": {
        "x": 37.23,
        "y": 53.46,
        "z": 38.09
      },
      "physical": {
        "x": -35.44,
        "y": -8.61,
        "z": 36.46
      }
    },
    "bounding_box": {
      "min_x": 34,
      "max_x": 48,
      "min_y": 40,
      "max_y": 78,
      "min_z": 31,
      "max_z": 61
    }
  },
  "execution_time_seconds": 2.126
}
```

---

# Prediction Outputs

After a successful prediction, generated files are stored in:

```text
outputs/
```

## 1. Binary Segmentation Mask

```text
prediction_<id>.nii.gz
```

This file contains the predicted binary segmentation:

```text
0 = background
1 = predicted lesion
```

---

## 2. DWI + Lesion Overlay

```text
prediction_overlay_<id>.nii.gz
```

This output contains the DWI information with the predicted lesion highlighted
for visualization in a medical imaging viewer.

---

## 3. PNG Preview

```text
prediction_<id>.png
```

This is a 2D visualization of the DWI and predicted lesion.

The PNG is intended to be easily displayed by the web application.

---

# Download Generated Files

Generated prediction files can be accessed through:

```text
GET /outputs/{filename}
```

For example:

```text
GET /outputs/prediction_74246290f71445b9b3ce22582ae1ee15.nii.gz
```

The exact filename is returned by the `/predict/` endpoint.

---

# Testing

Automated tests are located in:

```text
tests/
└── test_health.py
```

Run the tests with:

```bash
pytest
```

For detailed output:

```bash
pytest -v
```

---

# Complete Local Setup

From a fresh clone:

```bash
# Clone repository
git clone https://github.com/hazem324/stroke-mlops.git

# Enter FastAPI service
cd stroke-mlops/fastapi-service

# Create virtual environment
python3 -m venv .venv

# Activate virtual environment
source .venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Verify model
ls models/

# Start FastAPI
python run.py
```

Then open:

```text
http://localhost:8000/docs
```

Test:

```text
GET  /health
POST /predict/
GET  /outputs/{filename}
```

---

# Inference Pipeline

The complete machine learning inference pipeline is:

```text
DWI NIfTI
    |
    v
SimpleITK
    |
    v
NumPy
    |
    v
Crop Foreground
    |
    v
Z-score Normalization
    |
    v
Resize to 128 x 128 x 64
    |
    v
PyTorch Tensor
    |
    v
3D U-Net
    |
    v
Sigmoid
    |
    v
Threshold 0.5
    |
    v
Binary Segmentation
    |
    v
Restore Original Image Space
    |
    +-------------------------+
    |                         |
    v                         v
Segmentation Mask        DWI + Lesion
.nii.gz                  Overlay
    |                         |
    v                         v
Lesion Analysis           PNG Preview
    |
    v
JSON API Response
```

---

# MLOps Context

This FastAPI service is the model serving component of the larger
`stroke-mlops` project.

The overall project pipeline is:

```text
ISLES 2022 Dataset
        |
        v
Data Preparation
        |
        v
Preprocessed .npy Data
        |
        v
3D U-Net Training
        |
        v
Model Evaluation
        |
        v
Best Model
        |
        v
FastAPI Inference Service
        |
        v
Docker
        |
        v
Kubernetes
        |
        v
CI/CD
        |
        v
Cloud Deployment
```

---

# Technologies

* Python
* FastAPI
* Uvicorn
* PyTorch
* MONAI
* SimpleITK
* NumPy
* SciPy
* Pydantic
* Matplotlib