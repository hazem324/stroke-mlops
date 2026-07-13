"""
metadata.py

Role : extraire, afficher et agreger les metadonnees des fichiers NIfTI
de chaque patient (shape, spacing, dtype, affine), puis produire un
rapport CSV global du dataset.

Depend de load_data.py pour :
- la construction des chemins de fichiers
- la validation d'existence des fichiers

Pas de bloc main() ici : ce module est appele depuis main.py.
"""

from pathlib import Path
from typing import Dict, List
import pandas as pd
import nibabel as nib

from load_data import ISLESDatasetLoader

# 1. Extraction des metadonnees d'un patient
def extract_metadata(paths: Dict) -> Dict:
    """
    Extrait les metadonnees (shape, dtype, spacing, affine) de chaque
    fichier NIfTI d'un patient.
    """

    metadata = {}

    for modality, file_path in paths.items():

        if file_path is None or not file_path.exists():
            continue

        image = nib.load(str(file_path))
        header = image.header

        metadata[modality] = {
            "shape": image.shape,
            "dtype": str(header.get_data_dtype()),
            "voxel_spacing": tuple(header.get_zooms()),
            "affine": image.affine,
        }

    return metadata

# 2. Affichage des metadonnees d'un patient
def print_metadata(metadata: Dict) -> None:
    """
    Affiche dans la console les metadonnees d'un patient.
    """

    print("\n========== METADATA ==========\n")

    for modality, info in metadata.items():
        print(f"{modality.upper()}")
        print(f"Shape          : {info['shape']}")
        print(f"Voxel spacing  : {info['voxel_spacing']}")
        print(f"Data type      : {info['dtype']}")
        print()

# 3. Construction du tableau de metadonnees pour tout le dataset
def build_metadata_dataframe(
    loader: ISLESDatasetLoader, patient_ids: List[str]
) -> pd.DataFrame:
    """
    Parcourt tous les patients, extrait leurs metadonnees et construit
    un DataFrame plat (une ligne = un patient + une modalite).
    """

    rows = []

    for patient_id in patient_ids:
        paths = loader.build_patient_paths(patient_id)

        print(f"\n========== {patient_id} ==========")

        if not loader.validate_files_exist(paths):
            print(f"[SKIP] {patient_id}")
            continue

        metadata = extract_metadata(paths)

        for modality, info in metadata.items():
            shape = info["shape"]
            spacing = info["voxel_spacing"]

            rows.append({
                "patient_id": patient_id,
                "modality": modality,
                "shape_x": shape[0],
                "shape_y": shape[1],
                "shape_z": shape[2] if len(shape) > 2 else None,
                "spacing_x": spacing[0],
                "spacing_y": spacing[1],
                "spacing_z": spacing[2] if len(spacing) > 2 else None,
                "dtype": info["dtype"],
            })

    return pd.DataFrame(rows)

# 4. Resume statistique du dataset
def summarize_dataset(df: pd.DataFrame) -> None:
    """
    Affiche un resume synthetique : shapes/spacing les plus frequents
    par modalite, dtypes rencontres.
    """

    print("\n========== RESUME DU DATASET ==========\n")
    print(f"Nombre de lignes (patient x modalite) : {len(df)}")
    print(f"Nombre de patients uniques            : {df['patient_id'].nunique()}")
    print(f"Modalites presentes                    : {sorted(df['modality'].unique())}")

    df["shape"] = list(zip(df["shape_x"], df["shape_y"], df["shape_z"]))

    print("\n--- Shapes les plus frequentes par modalite ---")
    print(df.groupby("modality")["shape"].apply(lambda s: s.value_counts().head(3)))

    print("\n--- Spacing (min / max / moyenne) par modalite ---")
    for axis in ["spacing_x", "spacing_y", "spacing_z"]:
        print(f"\n{axis}")
        print(df.groupby("modality")[axis].agg(["min", "max", "mean"]))

    print("\n--- Dtypes rencontres ---")
    print(df.groupby("modality")["dtype"].value_counts())

# 5. Distribution des shapes par modalite
def show_shape_distribution(df: pd.DataFrame) -> None:
    """
    Affiche la distribution des shapes pour chaque modalite.
    """

    print("\n========== DISTRIBUTION DES SHAPES ==========\n")

    for modality in sorted(df["modality"].unique()):

        print(f"\n{modality.upper()}")

        sub = df[df["modality"] == modality]

        shape_counts = sub["shape"].value_counts()

        for shape, count in shape_counts.items():
            print(f"{shape} : {count} patients")

# 6. Export des rapports CSV
def export_reports(df: pd.DataFrame, output_dir: str) -> None:
    """
    Sauvegarde le detail complet en CSV.
    """

    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    df.drop(columns=["shape"], errors="ignore").to_csv(
        output_path / "exploration_dataset.csv",
        index=False
    )

    print(f"\n[SAVED] {output_path / 'exploration_dataset.csv'}")