from load_data import ISLESDatasetLoader

from metadata import (
    build_metadata_dataframe,
    summarize_dataset,
    show_shape_distribution,
    export_reports,
)

from visualize import run_visualization

from quality_check import (
    run_quality_check,
    analyze_lesions,
    export_qc_report,
)

from preprocessing.dataset_pipeline import (
    run_preprocessing,
    print_summary,
)

from split_dataset import (
    build_dataframe,
    split_dataset,
    save_split,
    print_statistics,
)


def main():

    dataset_path = "data/ISLES-2022"
    output_dir = "reports"

    print("=" * 60)
    print("Loading dataset")
    print("=" * 60)

    loader = ISLESDatasetLoader(dataset_path)

    loader.validate_dataset_path()

    patient_ids = loader.discover_patients()

    # Metadata

    print("\nMetadata analysis")

    metadata_df = build_metadata_dataframe(
        loader,
        patient_ids,
    )

    summarize_dataset(metadata_df)

    show_shape_distribution(metadata_df)

    export_reports(
        metadata_df,
        output_dir,
    )

    # Visualization

    print("\nVisualization")

    run_visualization(
        loader,
        patient_ids,
        output_dir="reports/visualizations",
        n_samples=10,
    )

    # Quality Check

    print("\nQuality Check")

    qc_df = run_quality_check(
        loader,
        patient_ids,
    )

    lesion_df = analyze_lesions(
        loader,
        patient_ids,
    )

    lesion_df.to_csv(
        "reports/lesion_statistics.csv",
        index=False,
    )

    export_qc_report(
        qc_df,
        output_dir,
    )

    # Preprocessing

    print("\nPreprocessing")

    preprocessing_summary = run_preprocessing(
        loader,
        patient_ids,
    )

    print_summary(preprocessing_summary)

    # Dataset Split

    print("\nTrain / Validation / Test Split")

    split_df = build_dataframe(
        preprocessing_summary["output"]
    )

    train_df, valid_df, test_df = split_dataset(
        split_df
    )

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

    # End Pipeline

    print("\n" + "=" * 60)
    print("PIPELINE TERMINE")
    print("=" * 60)

    print(f"Rapports disponibles dans : {output_dir}/")

    print(
        f"Donnees pretraitees disponibles dans : "
        f"{preprocessing_summary['output']}/"
    )


if __name__ == "__main__":
    main()