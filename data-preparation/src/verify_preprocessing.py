from pathlib import Path

import numpy as np
import pandas as pd

from preprocessing.constants import (
    OUTPUT_DIR,
    TARGET_SHAPE,
)


def check_patient(patient_dir: Path) -> dict:
    """
    Verify the integrity of one preprocessed patient.
    """

    problems = []

    try:
        adc = np.load(patient_dir / "adc.npy")
        dwi = np.load(patient_dir / "dwi.npy")
        flair = np.load(patient_dir / "flair.npy")
        mask = np.load(patient_dir / "mask.npy")

    except FileNotFoundError as e:

        return {
            "patient_id": patient_dir.name,
            "n_problemes": 1,
            "problemes": f"Fichier manquant : {e}",
        }

    volumes = {
        "adc": adc,
        "dwi": dwi,
        "flair": flair,
        "mask": mask,
    }

    # ==========================================================
    # Shape verification
    # ==========================================================

    for name, volume in volumes.items():

        if volume.shape != TARGET_SHAPE:

            problems.append(
                f"{name}: shape {volume.shape} != {TARGET_SHAPE}"
            )

        if np.isnan(volume).any():

            problems.append(
                f"{name}: NaN detected"
            )

        if np.isinf(volume).any():

            problems.append(
                f"{name}: Inf detected"
            )

    # ==========================================================
    # Mask verification
    # ==========================================================

    mask_values = np.unique(mask)

    invalid_values = [

        value

        for value in mask_values

        if not np.isclose(value, 0)
        and not np.isclose(value, 1)

    ]

    if invalid_values:

        problems.append(
            f"Mask contains non-binary values: {invalid_values}"
        )

    lesion_ratio = mask.sum() / mask.size

    if lesion_ratio > 0.15:

        problems.append(
            f"Suspicious lesion ratio ({lesion_ratio:.3%})"
        )

    # ==========================================================
    # Normalization verification
    # ==========================================================

    for modality in ["adc", "dwi", "flair"]:

        volume = volumes[modality]

        if abs(volume.mean()) > 1.0 or not (
            0.3 < volume.std() < 3.0
        ):

            problems.append(
                f"{modality}: abnormal normalization "
                f"(mean={volume.mean():.2f}, std={volume.std():.2f})"
            )

    return {

        "patient_id": patient_dir.name,

        "n_problemes": len(problems),

        "problemes": (
            "; ".join(problems)
            if problems
            else "aucun"
        ),

        "lesion_voxels": int(mask.sum()),

        "lesion_ratio": round(
            lesion_ratio,
            5,
        ),
    }


def main():

    if not OUTPUT_DIR.exists():

        raise FileNotFoundError(
            f"{OUTPUT_DIR} introuvable. Lancez le preprocessing d'abord."
        )

    patient_dirs = sorted(
        directory
        for directory in OUTPUT_DIR.iterdir()
        if directory.is_dir()
    )

    print(
        f"Verification de {len(patient_dirs)} patients...\n"
    )

    rows = []

    for patient_dir in patient_dirs:

        result = check_patient(patient_dir)

        rows.append(result)

        status = (
            "OK"
            if result["n_problemes"] == 0
            else f"{result['n_problemes']} probleme(s)"
        )

        print(
            f"[{status}] {result['patient_id']}"
        )

    df = pd.DataFrame(rows)

    report_path = Path(
        "reports/preprocessing_verification.csv"
    )

    report_path.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    df.to_csv(
        report_path,
        index=False,
    )

    n_ok = (df["n_problemes"] == 0).sum()

    n_ko = (df["n_problemes"] > 0).sum()

    print("\n" + "=" * 60)

    print(
        f"Verification terminee : {n_ok} OK | {n_ko} avec probleme(s)"
    )

    print(f"Rapport : {report_path}")

    print("=" * 60)

    if n_ko > 0:

        print("\nPatients a examiner :\n")

        print(
            df[df["n_problemes"] > 0][
                ["patient_id", "problemes"]
            ].to_string(index=False)
        )


if __name__ == "__main__":
    main()