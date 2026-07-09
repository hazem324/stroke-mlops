from pathlib import Path
from dataclasses import dataclass
from pathlib import Path
from dataclasses import dataclass
from typing import Optional, List, Dict
import nibabel as nib
from nibabel.filebasedimages import ImageFileError


@dataclass
class PatientData:
    """
    Store all information related to one patient.
    """

    patient_id: str

    flair_path: Optional[Path]
    adc_path: Optional[Path]
    dwi_path: Optional[Path]
    mask_path: Optional[Path]

    metadata: Dict

    is_valid: bool

class ISLESDatasetLoader:

    def __init__(self, dataset_root: str):
        self.dataset_root = Path(dataset_root)
        self.rawdata_dir = self.dataset_root / "rawdata"
        self.derivatives_dir = self.dataset_root / "derivatives"
        self.patients: List[PatientData] = []

    def validate_dataset_path(self) -> None:
        print("\nChecking dataset structure...\n")

        if not self.dataset_root.exists():
            raise FileNotFoundError(
                f"Dataset not found : {self.dataset_root}"
            )

        if not self.rawdata_dir.exists():
            raise FileNotFoundError("rawdata folder not found.")

        if not self.derivatives_dir.exists():
            raise FileNotFoundError("derivatives folder not found.")

        print("Dataset found.")
        print(f"Root        : {self.dataset_root}")
        print(f"Raw data    : {self.rawdata_dir}")
        print(f"Derivatives : {self.derivatives_dir}")

    def discover_patients(self) -> List[str]:
        patient_ids = []

        for patient_dir in sorted(self.rawdata_dir.iterdir()):
            if patient_dir.is_dir():
                patient_ids.append(patient_dir.name)

        print(f"\nPatients discovered : {len(patient_ids)}")
        return patient_ids


def build_patient_paths(self, patient_id: str) -> Dict[str, Path]:
    """
    Build all expected file paths for one patient.

    Parameters
    ----------
    patient_id : str
        Example: sub-strokecase0001

    Returns
    -------
    Dict[str, Path]
        Dictionary containing all expected paths.
    """

    session = "ses-0001"

    raw_patient = self.rawdata_dir / patient_id / session

    derivatives_patient = self.derivatives_dir / patient_id / session

    paths = {
        "flair": raw_patient / "anat" / f"{patient_id}_{session}_FLAIR.nii.gz",

        "adc": raw_patient / "dwi" / f"{patient_id}_{session}_adc.nii.gz",

        "dwi": raw_patient / "dwi" / f"{patient_id}_{session}_dwi.nii.gz",

        "mask": derivatives_patient / f"{patient_id}_{session}_msk.nii.gz"
    }

    return paths

def validate_modalities(self, paths: Dict[str, Path]) -> bool:
    """
    Check that all expected modalities are present.

    Returns
    -------
    bool
        True if every modality exists.
    """

    required_modalities = [
        "flair",
        "adc",
        "dwi",
        "mask"
    ]

    valid = True

    for modality in required_modalities:

        if modality not in paths:

            print(f"[ERROR] Missing modality entry : {modality}")

            valid = False

    return valid

def validate_files_exist(self, paths: Dict[str, Path]) -> bool:
    """
    Verify that every expected file exists.

    Parameters
    ----------
    paths : Dict[str, Path]

    Returns
    -------
    bool
    """

    valid = True

    for modality, file_path in paths.items():

        if file_path.exists():

            print(f"[OK] {modality.upper()}")

        else:

            print(f"[MISSING] {modality.upper()}")

            print(f"          {file_path}")

            valid = False

    return valid


def validate_nifti_files(self, paths: Dict[str, Path]) -> bool:
    """
    Validate that every NIfTI file can be opened.

    Parameters
    ----------
    paths : Dict[str, Path]

    Returns
    -------
    bool
    """

    valid = True

    for modality, file_path in paths.items():

        if modality == "snapshot":
            continue

        try:

            nib.load(str(file_path))

            print(f"[READABLE] {modality.upper()}")

        except ImageFileError as e:

            print(f"[INVALID NIFTI] {modality.upper()}")

            print(e)

            valid = False

        except Exception as e:

            print(f"[ERROR] {modality.upper()}")

            print(e)

            valid = False

    return valid

def extract_metadata(self, paths: Dict[str, Path]) -> Dict:
    """
    Extract metadata from every modality.

    Parameters
    ----------
    paths : Dict[str, Path]

    Returns
    -------
    Dict
    """

    metadata = {}

    for modality, file_path in paths.items():

        if modality == "snapshot":
            continue

        image = nib.load(str(file_path))

        header = image.header

        metadata[modality] = {

            "shape": image.shape,

            "dtype": str(header.get_data_dtype()),

            "voxel_spacing": tuple(header.get_zooms()),

            "affine": image.affine

        }

    return metadata

def print_metadata(self, metadata: Dict) -> None:
    """
    Display metadata.
    """

    print("\n========== METADATA ==========\n")

    for modality, info in metadata.items():

        print(f"{modality.upper()}")

        print(f"Shape          : {info['shape']}")

        print(f"Voxel spacing  : {info['voxel_spacing']}")

        print(f"Data type      : {info['dtype']}")

        print()

        

# # Dataset root
# DATASET_ROOT = Path("data/ISLES-2022")

# DERIVATIVES = DATASET_ROOT / "derivatives"


# def print_separator():
#     print("=" * 10)

# #Verify that the dataset exists.
# def check_dataset():

#     print_separator()
#     print("Checking dataset...")
#     print_separator()

#     if not DATASET_ROOT.exists():
#         raise FileNotFoundError(f"Dataset not found: {DATASET_ROOT}")

#     print(f"Dataset found : {DATASET_ROOT.resolve()}")
#     print()


# def get_patients():

#     patients = sorted(
#         [
#             folder
#             for folder in DERIVATIVES.iterdir()
#             if folder.is_dir()
#         ]
#     )

#     return patients


# def dataset_summary(patients):

#     print_separator()
#     print("DATASET SUMMARY")
#     print_separator()

#     print(f"Total patients : {len(patients)}")
#     print()


# def explore_patient(patient_folder):

#     session = patient_folder / "ses-0001"

#     print(f"Patient : {patient_folder.name}")

#     if not session.exists():
#         print("   Session not found\n")
#         return

#     files = sorted(session.iterdir())

#     for file in files:
#         print(f"   - {file.name}")

#     print()


# def count_files(patients):

#     total_masks = 0
#     total_png = 0

#     for patient in patients:

#         session = patient / "ses-0001"

#         if not session.exists():
#             continue

#         for file in session.iterdir():

#             if file.suffix == ".png":
#                 total_png += 1

#             if file.name.endswith("_msk.nii.gz"):
#                 total_masks += 1

#     print_separator()
#     print("FILES SUMMARY")
#     print_separator()

#     print(f"Masks      : {total_masks}")
#     print(f"Snapshots  : {total_png}")
#     print()


# def main():

#     check_dataset()

#     patients = get_patients()

#     dataset_summary(patients)

#     print_separator()
#     print("FIRST 5 PATIENTS")
#     print_separator()

#     for patient in patients[:5]:
#         explore_patient(patient)

#     count_files(patients)


# if __name__ == "__main__":
#     main()

if __name__ == "__main__":
    dataset_path = "data/ISLES-2022"   # adapte ce chemin si nécessaire

    loader = ISLESDatasetLoader(dataset_path)

    loader.validate_dataset_path()

    patients = loader.discover_patients()

    print(patients)