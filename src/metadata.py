"""
metadata.py

Rôle : extraire, afficher et agréger les métadonnées des fichiers NIfTI
de chaque patient (shape, spacing, dtype, affine), puis produire un
rapport CSV global du dataset.

Dépend de load_data.py uniquement pour :
- la validation de la structure du dataset
- la découverte des patients
- la construction des chemins de fichiers
- la validation d'existence des fichiers

Tout ce qui concerne les métadonnées (extraction, affichage, agrégation,
statistiques) vit ici.
"""

from pathlib import Path
from typing import Dict, List, Optional
import pandas as pd
import nibabel as nib

from load_data import ISLESDatasetLoader


#  Extraction des métadonnées d'un patient

def extract_metadata(paths):
    """
    Extrait les métadonnées (shape, dtype, spacing, affine) de chaque
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


#  Affichage des métadonnées d'un patient
def print_metadata(metadata: Dict) -> None:
    """
    Affiche dans la console les métadonnées d'un patient.
    """
    print("\n========== METADATA ==========\n")

    for modality, info in metadata.items():
        print(f"{modality.upper()}")
        print(f"Shape          : {info['shape']}")
        print(f"Voxel spacing  : {info['voxel_spacing']}")
        print(f"Data type      : {info['dtype']}")
        print()

#  Construction du tableau de métadonnées pour tout le dataset

def build_metadata_dataframe(
    loader: ISLESDatasetLoader, patient_ids: List[str]
) -> pd.DataFrame:
    """
    Parcourt tous les patients, extrait leurs métadonnées et construit
    un DataFrame plat (une ligne = un patient + une modalité).
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


#  Résumé statistique du dataset

def summarize_dataset(df: pd.DataFrame) -> None:
    """
    Affiche un résumé synthétique : shapes/spacing les plus fréquents
    par modalité, dtypes rencontrés.
    """
    print("\n========== RÉSUMÉ DU DATASET ==========\n")
    print(f"Nombre de lignes (patient x modalité) : {len(df)}")
    print(f"Nombre de patients uniques            : {df['patient_id'].nunique()}")
    print(f"Modalités présentes                    : {sorted(df['modality'].unique())}")

    df["shape"] = list(zip(df["shape_x"], df["shape_y"], df["shape_z"]))

    print("\n--- Shapes les plus fréquentes par modalité ---")
    print(df.groupby("modality")["shape"].apply(lambda s: s.value_counts().head(3)))

    print("\n--- Spacing (min / max / moyenne) par modalité ---")
    for axis in ["spacing_x", "spacing_y", "spacing_z"]:
        print(f"\n{axis}")
        print(df.groupby("modality")[axis].agg(["min", "max", "mean"]))

    print("\n--- Dtypes rencontrés ---")
    print(df.groupby("modality")["dtype"].value_counts())


#  Détection des patients atypiques

# def detect_outliers(df: pd.DataFrame) -> pd.DataFrame:
#     """
#     Compare chaque patient au shape le plus fréquent (mode) de sa
#     modalité et signale les écarts.
#     """
#     outlier_rows = []

#     for modality in df["modality"].unique():
#         sub = df[df["modality"] == modality]
#         mode_shape = sub["shape"].mode().iloc[0]

#         for _, row in sub[sub["shape"] != mode_shape].iterrows():
#             outlier_rows.append({
#                 "patient_id": row["patient_id"],
#                 "modality": modality,
#                 "shape": row["shape"],
#                 "expected_shape": mode_shape,
#             })

#     outliers_df = pd.DataFrame(outlier_rows)

#     print(f"\n--- Patients avec shape atypique : {len(outliers_df)} ---")
#     if not outliers_df.empty:
#         print(outliers_df)

#     return outliers_df


def show_shape_distribution(df: pd.DataFrame) -> None:
    """
    Affiche la distribution des shapes pour chaque modalité.
    """

    print("\n========== DISTRIBUTION DES SHAPES ==========\n")

    for modality in sorted(df["modality"].unique()):

        print(f"\n{modality.upper()}")

        sub = df[df["modality"] == modality]

        shape_counts = sub["shape"].value_counts()

        for shape, count in shape_counts.items():
            print(f"{shape} : {count} patients")


#  Export des rapports CSV

def export_reports(df: pd.DataFrame, output_dir: str):

    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    df.drop(columns=["shape"], errors="ignore").to_csv(
        output_path / "exploration_dataset.csv",
        index=False
    )

    print(f"\n[SAVED] {output_path / 'exploration_dataset.csv'}")


#  Point d'entrée

def main():
    dataset_path = "data/ISLES-2022"
    output_dir = "reports"

    loader = ISLESDatasetLoader(dataset_path)
    loader.validate_dataset_path()
    patient_ids = loader.discover_patients()

    df = build_metadata_dataframe(loader, patient_ids)

    summarize_dataset(df)
    show_shape_distribution(df)
    export_reports(df, output_dir)


if __name__ == "__main__":
    main()