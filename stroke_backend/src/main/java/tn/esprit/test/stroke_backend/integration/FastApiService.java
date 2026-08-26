package tn.esprit.test.stroke_backend.integration;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class FastApiService {

    private final RestClient restClient;
    private final String predictEndpoint;

    public FastApiService( RestClient.Builder restClientBuilder,

            @Value("${app.fastapi.base-url}")
            String baseUrl,

            @Value("${app.fastapi.predict-endpoint:}")
            String predictEndpoint) {

        this.restClient = createRestClient(restClientBuilder, baseUrl );

        this.predictEndpoint = predictEndpoint;
    }

    /**
     * Analyse un fichier DWI avec le modèle FastAPI.
     */
    public FastApiPredictionResponse predict(Path dwiFilePath) {

        FileSystemResource fileResource = getFileResource(dwiFilePath);

        MultiValueMap<String, HttpEntity<?>> body = createMultipartBody(fileResource);

        try {

            FastApiPredictionResponse response =
                    restClient
                            .post()
                            .uri(predictEndpoint)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(body)
                            .retrieve()
                            .body(FastApiPredictionResponse.class);

            if (response == null) {
                throw new RuntimeException(
                        "FastAPI returned an empty response"
                );
            }

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "FastAPI prediction failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Teste la communication Spring Boot -> FastAPI.
     */
    public FastApiConnectionTestResponse testConnection(Path dwiFilePath) {

        FileSystemResource fileResource = getFileResource(dwiFilePath);

        MultiValueMap<String, HttpEntity<?>> body = createMultipartBody(fileResource);

        try {

            FastApiConnectionTestResponse response =
                    restClient
                            .post()
                            .uri("/predict/test")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(body)
                            .retrieve()
                            .body(FastApiConnectionTestResponse.class);

            if (response == null) {
                throw new RuntimeException(
                        "FastAPI test returned an empty response"
                );
            }

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "FastAPI connection test failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Crée le RestClient avec Apache HttpClient 5.
     *
     * Apache HttpClient est utilisé pour éviter le problème
     * d'upgrade HTTP/2 h2c rencontré avec le client JDK.
     */
    private RestClient createRestClient( RestClient.Builder restClientBuilder, String baseUrl) {

        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setConnectionRequestTimeout(
                                60,
                                TimeUnit.SECONDS
                        )
                        .setResponseTimeout(
                                180,
                                TimeUnit.SECONDS
                        )
                        .build();

        CloseableHttpClient httpClient =
                HttpClients.custom()
                        .setDefaultRequestConfig(requestConfig)
                        .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(
                        httpClient
                );

        return restClientBuilder
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Vérifie et transforme le fichier DWI en Resource.
     */
    private FileSystemResource getFileResource(Path dwiFilePath) {

        FileSystemResource fileResource =
                new FileSystemResource(
                        dwiFilePath.toFile()
                );

        if (!fileResource.exists()) {
            throw new RuntimeException(
                    "DWI file does not exist: "
                            + dwiFilePath
            );
        }

        if (!fileResource.isReadable()) {
            throw new RuntimeException(
                    "DWI file is not readable: "
                            + dwiFilePath
            );
        }

        return fileResource;
    }

    /**
     * Construit le multipart attendu par FastAPI.
     *
     * FastAPI attend :
     *
     * file: UploadFile = File(...)
     *
     * Le nom de la partie doit donc être "file".
     */
    private MultiValueMap<String, HttpEntity<?>> createMultipartBody(FileSystemResource fileResource) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part(
                        "file",
                        fileResource
                )
                .filename(
                        fileResource.getFilename()
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                );

        return builder.build();
    }
}