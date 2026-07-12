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
        self.rawdata_dir = self.dataset_root
        self.derivatives_dir = self.dataset_root / "derivatives"
        self.patients: List[PatientData] = []

    def validate_dataset_path(self) -> None:
        print("\nChecking dataset structure...\n")

        if not self.dataset_root.exists():
            raise FileNotFoundError(
                f"Dataset not found: {self.dataset_root}"
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

            if (
                patient_dir.is_dir()
                and patient_dir.name.startswith("sub-")
            ):
                patient_ids.append(patient_dir.name)

        print(f"\nPatients discovered : {len(patient_ids)}")

        return patient_ids

    def find_file(self, folder: Path, pattern: str) -> Optional[Path]:

        files = list(folder.glob(pattern))

        return files[0] if files else None

    def build_patient_paths(self, patient_id: str) -> Dict[str, Optional[Path]]:

        session = "ses-0001"

        raw_patient = self.rawdata_dir / patient_id / session

        derivatives_patient = (
            self.derivatives_dir
            / patient_id
            / session
        )

        paths = {

            "flair":
                self.find_file(
                    raw_patient / "anat",
                    "*FLAIR.nii.gz"
                ),

            "adc":
                self.find_file(
                    raw_patient / "dwi",
                    "*adc.nii.gz"
                ),

            "dwi":
                self.find_file(
                    raw_patient / "dwi",
                    "*dwi.nii.gz"
                ),

            "mask":
                self.find_file(
                    derivatives_patient,
                    "*msk.nii.gz"
                )

        }

        return paths

    # Check that all expected modalities are present
    def validate_modalities(self, paths):

        valid = True

        for modality, path in paths.items():

            if path is None:

                print(f"[MISSING] {modality.upper()}")

                valid = False

        return valid

    def validate_files_exist(self, paths: Dict[str, Path]) -> bool:
        """
        Verify that every expected file exists.
        """

        valid = True

        for modality, file_path in paths.items():

            if file_path is None:
                print(f"[MISSING] {modality.upper()} (fichier non trouvé)")
                valid = False
                continue

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
        """

        valid = True

        for modality, file_path in paths.items():

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

    def load_patient(self, patient_id: str) -> PatientData:

        paths = self.build_patient_paths(patient_id)

        valid = (
            self.validate_modalities(paths)
            and self.validate_files_exist(paths)
            and self.validate_nifti_files(paths)
        )

        metadata = {}

        if valid:
            metadata = self.extract_metadata(paths)

        return PatientData(
            patient_id=patient_id,
            flair_path=paths["flair"],
            adc_path=paths["adc"],
            dwi_path=paths["dwi"],
            mask_path=paths["mask"],
            metadata={},
            is_valid=valid
        )


# if __name__ == "__main__":
#     dataset_path = "data/ISLES-2022"
#     loader = ISLESDatasetLoader(dataset_path)
#     loader.validate_dataset_path()
#     patients = loader.discover_patients()

#     for patient in patients:
#         print("=" * 60)
#         print(patient)
#         patient_data = loader.load_patient(patient)
#         loader.patients.append(patient_data)