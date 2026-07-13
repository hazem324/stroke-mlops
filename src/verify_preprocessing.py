"""
verify_preprocessing.py

Verifie que les donnees pretraitees (data/preprocessed/) sont
coherentes : shapes correctes, pas de NaN, masque binaire, stats
d'intensite plausibles. A lancer APRES preprocessing.py.

Usage :
    python src/verify_preprocessing.py
"""

from pathlib import Path
import numpy as np
import pandas as pd

PREPROCESSED_DIR = Path("data/preprocessed")
EXPECTED_SHAPE = (128, 128, 64)


def check_patient(patient_dir: Path) -> dict:
    problems = []

    try:
        adc = np.load(patient_dir / "adc.npy")
        dwi = np.load(patient_dir / "dwi.npy")
        flair = np.load(patient_dir / "flair.npy")
        mask = np.load(patient_dir / "mask.npy")
    except FileNotFoundError as e:
        return {"patient_id": patient_dir.name, "n_problemes": 1, "problemes": f"fichier manquant: {e}"}

    volumes = {"adc": adc, "dwi": dwi, "flair": flair, "mask": mask}

    for name, vol in volumes.items():
        if vol.shape != EXPECTED_SHAPE:
            problems.append(f"{name}: shape {vol.shape} != {EXPECTED_SHAPE}")
        if np.isnan(vol).any():
            problems.append(f"{name}: NaN present")
        if np.isinf(vol).any():
            problems.append(f"{name}: Inf present")

    mask_vals = np.unique(mask)
    invalid = [v for v in mask_vals if not np.isclose(v, 0) and not np.isclose(v, 1)]
    if invalid:
        problems.append(f"mask: valeurs non binaires {invalid}")

    lesion_ratio = mask.sum() / mask.size
    if lesion_ratio > 0.15:
        problems.append(f"mask: ratio lesion suspect eleve ({lesion_ratio:.3%})")

    for name in ["adc", "dwi", "flair"]:
        vol = volumes[name]
        if abs(vol.mean()) > 1.0 or not (0.3 < vol.std() < 3.0):
            problems.append(f"{name}: normalisation suspecte (mean={vol.mean():.2f}, std={vol.std():.2f})")

    return {
        "patient_id": patient_dir.name,
        "n_problemes": len(problems),
        "problemes": "; ".join(problems) if problems else "aucun",
        "lesion_voxels": int(mask.sum()),
        "lesion_ratio": round(lesion_ratio, 5),
    }


def main():
    if not PREPROCESSED_DIR.exists():
        raise FileNotFoundError(f"{PREPROCESSED_DIR} introuvable, lancez preprocessing.py d'abord")

    rows = []
    patient_dirs = sorted([d for d in PREPROCESSED_DIR.iterdir() if d.is_dir()])

    print(f"Verification de {len(patient_dirs)} patients pretraites...\n")

    for patient_dir in patient_dirs:
        result = check_patient(patient_dir)
        rows.append(result)
        status = "OK" if result["n_problemes"] == 0 else f"{result['n_problemes']} probleme(s)"
        print(f"[{status}] {result['patient_id']}")

    df = pd.DataFrame(rows)

    output_path = Path("reports/preprocessing_verification.csv")
    output_path.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(output_path, index=False)

    n_ok = (df["n_problemes"] == 0).sum()
    n_ko = (df["n_problemes"] > 0).sum()

    print(f"\n{'='*60}")
    print(f"Resultat : {n_ok} OK, {n_ko} avec probleme(s)")
    print(f"Rapport sauvegarde : {output_path}")
    print(f"{'='*60}")

    if n_ko > 0:
        print("\nPatients a examiner :")
        print(df[df["n_problemes"] > 0][["patient_id", "problemes"]].to_string(index=False))


if __name__ == "__main__":
    main()