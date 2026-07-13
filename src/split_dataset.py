"""
split_dataset.py

Rôle :
Créer une séparation reproductible du dataset en
Train / Validation / Test.

Entrée :
    data/preprocessed/
        sub-strokecaseXXXX/
            adc.npy
            dwi.npy
            flair.npy
            mask.npy

Sortie :
    data/splits/
        train.csv
        validation.csv
        test.csv
"""

from pathlib import Path

import numpy as np
import pandas as pd

from sklearn.model_selection import train_test_split


PREPROCESSED_DIR = Path("data/preprocessed")
OUTPUT_DIR = Path("data/splits")

RANDOM_STATE = 42

TRAIN_SIZE = 0.70
VALID_SIZE = 0.15
TEST_SIZE = 0.15

# Découvre tous les patients prétraités.
def discover_patients():

    patient_ids = []

    for patient_dir in sorted(PREPROCESSED_DIR.iterdir()):

        if patient_dir.is_dir():
            patient_ids.append(patient_dir.name)

    print(f"Patients trouvés : {len(patient_ids)}")

    return patient_ids

# Vérifie si le masque contient au moins une lésion.
def has_lesion(patient_id):
    mask = np.load(
        PREPROCESSED_DIR /
        patient_id /
        "mask.npy"
    )

    return int(mask.sum() > 0)

# Construit un DataFrame 
def build_dataframe(patient_ids):

    rows = []

    for patient in patient_ids:

        rows.append({

            "patient_id": patient,

            "has_lesion": has_lesion(patient)

        })

    return pd.DataFrame(rows)


# Réalise un split stratifié.
def split_dataset(df):
    
    train_df, temp_df = train_test_split(

        df,

        test_size=0.30,

        random_state=RANDOM_STATE,

        stratify=df["has_lesion"],

    )

    valid_df, test_df = train_test_split(

        temp_df,

        test_size=0.50,

        random_state=RANDOM_STATE,

    )

    return train_df, valid_df, test_df


# Sauvegarde les CSV 
def save_split(train_df, valid_df, test_df):

    OUTPUT_DIR.mkdir(parents=True,  exist_ok=True,)

    train_df.to_csv( OUTPUT_DIR / "train.csv",index=False,)

    valid_df.to_csv(OUTPUT_DIR / "validation.csv",index=False,)

    test_df.to_csv( OUTPUT_DIR / "test.csv",index=False,)

    print("\nCSV sauvegardés.")


def print_statistics(train_df, valid_df, test_df):

    print("\n========== DATASET SPLIT ==========\n")

    print(f"Train       : {len(train_df)}")

    print(f"Validation  : {len(valid_df)}")

    print(f"Test        : {len(test_df)}")

    print()

    print("Patients avec lésion")

    print(f"Train       : {train_df['has_lesion'].sum()}")

    print(f"Validation  : {valid_df['has_lesion'].sum()}")

    print(f"Test        : {test_df['has_lesion'].sum()}")


def main():

    patient_ids = discover_patients()

    df = build_dataframe(patient_ids)

    train_df, valid_df, test_df = split_dataset(df)

    save_split(

        train_df,

        valid_df,

        test_df,

    )

    print_statistics(

        train_df,

        valid_df,

        test_df,

    )


if __name__ == "__main__":
    main()