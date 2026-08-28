package tn.esprit.test.stroke_backend.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path baseStoragePath;

    public FileStorageService(
            @Value("${app.storage.base-path:storage}")
            String basePath) {

        this.baseStoragePath =
                Paths.get(basePath)
                     .toAbsolutePath()
                     .normalize();
    }


    /**
     * Sauvegarde le fichier DWI.
     *
     * Exemple :
     *
     * storage/
     *   patients/
     *     P00152/
     *       studies/
     *         S001/
     *           dwi.nii.gz
     */
    public String storeDwiFile(
            MultipartFile file,
            String patientCode,
            String studyCode) throws IOException {

        Path studyDirectory =
                baseStoragePath
                    .resolve("patients")
                    .resolve(patientCode)
                    .resolve("studies")
                    .resolve(studyCode);

        Files.createDirectories(studyDirectory);

        Path targetPath =
                studyDirectory.resolve("dwi.nii.gz");

        try (InputStream inputStream = file.getInputStream()) {

            Files.copy(
                    inputStream,
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return baseStoragePath
                .relativize(targetPath)
                .toString()
                .replace("\\", "/");
    }


    /**
     * Retourne le chemin physique d'un fichier.
     */
    public Path getPhysicalPath(
            String relativePath) {

        return baseStoragePath
                .resolve(relativePath)
                .normalize();
    }


    /**
     * Sauvegarde la segmentation générée par FastAPI.
     *
     * Cette méthode pourra être utilisée si FastAPI
     * renvoie le fichier et non seulement son chemin.
     */
  /* public String storeSegmentationFile(
            byte[] fileBytes,
            String patientCode,
            String studyCode) throws IOException {

        Path studyDirectory =
                baseStoragePath
                    .resolve("patients")
                    .resolve(patientCode)
                    .resolve("studies")
                    .resolve(studyCode);

        Files.createDirectories(studyDirectory);

        Path targetPath =
                studyDirectory.resolve(
                        "segmentation.nii.gz"
                );

        Files.write(
                targetPath,
                fileBytes
        );

        return baseStoragePath
                .relativize(targetPath)
                .toString()
                .replace("\\", "/");
    }
   */
       /**
     * Sauvegarde les 3 fichiers reçus de FastAPI dans
     * storage/patients/{patientId}/studies/{studyCode}/analysis/
     */
    public AnalysisPaths storeAnalysisFiles(
            byte[] predictionBytes,
            byte[] overlayBytes,
            byte[] previewBytes,
            String patientId,
            String studyCode) throws IOException {

        Path analysisDirectory =
                baseStoragePath
                    .resolve("patients")
                    .resolve(patientId)
                    .resolve("studies")
                    .resolve(studyCode)
                    .resolve("analysis");

        Files.createDirectories(analysisDirectory);

        Path predictionPath = analysisDirectory.resolve("prediction.nii.gz");
        Path overlayPath    = analysisDirectory.resolve("overlay.nii.gz");
        Path previewPath    = analysisDirectory.resolve("preview.png");

        Files.write(predictionPath, predictionBytes);
        Files.write(overlayPath, overlayBytes);
        Files.write(previewPath, previewBytes);

        return new AnalysisPaths(
                relativize(predictionPath),
                relativize(overlayPath),
                relativize(previewPath)
        );
    }

    private String relativize(Path path) {
        return baseStoragePath
                .relativize(path)
                .toString()
                .replace("\\", "/");
    }

    public record AnalysisPaths(
            String predictionFile,
            String overlayFile,
            String previewFile
    ) {}

    /**
     * Vérifie qu'un fichier existe.
     */
    public boolean exists(
            String relativePath) {

        Path path =
                getPhysicalPath(relativePath);

        return Files.exists(path);
    }
}