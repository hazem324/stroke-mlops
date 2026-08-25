package tn.esprit.test.stroke_backend.integration;

import java.io.IOException;
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


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public FastApiService(

            RestClient.Builder restClientBuilder,

            @Value("${app.fastapi.base-url:http://localhost:8000}")
            String baseUrl,

            @Value("${app.fastapi.predict-endpoint:/predict/}")
            String predictEndpoint

    ) {


        // ========================================================
        // APACHE HTTP CLIENT 5
        // ========================================================

        RequestConfig requestConfig =
                RequestConfig.custom()

                        .setConnectionRequestTimeout(
                                30,
                                TimeUnit.SECONDS
                        )

                        .setResponseTimeout(
                                120,
                                TimeUnit.SECONDS
                        )

                        .build();


        CloseableHttpClient httpClient =
                HttpClients.custom()

                        .setDefaultRequestConfig(
                                requestConfig
                        )

                        .build();


        // ========================================================
        // REQUEST FACTORY
        // ========================================================

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(
                        httpClient
                );


        // ========================================================
        // REST CLIENT
        // ========================================================

        this.restClient = restClientBuilder

                .requestFactory(
                        requestFactory
                )

                .requestInterceptor(
                        (request, body, execution) -> {

                            System.out.println();

                            System.out.println(
                                    "============================================================"
                            );

                            System.out.println(
                                    "SPRING BOOT -> FASTAPI HTTP DEBUG"
                            );

                            System.out.println(
                                    "============================================================"
                            );


                            System.out.println(
                                    "METHOD : "
                                            + request.getMethod()
                            );


                            System.out.println(
                                    "URI : "
                                            + request.getURI()
                            );


                            System.out.println();

                            System.out.println(
                                    "HEADERS:"
                            );


                            request.getHeaders().forEach(
                                    (name, values) ->

                                            System.out.println(
                                                    name
                                                            + " = "
                                                            + values
                                            )
                            );


                            System.out.println();

                            System.out.println(
                                    "BODY SIZE : "
                                            + (
                                            body == null
                                                    ? 0
                                                    : body.length
                                    )
                                            + " bytes"
                            );


                            System.out.println();

                            System.out.println(
                                    "============================================================"
                            );


                            return execution.execute(
                                    request,
                                    body
                            );
                        }
                )

                .baseUrl(
                        baseUrl
                )

                .build();


        this.predictEndpoint =
                predictEndpoint;
    }


    // ============================================================
    // REAL PREDICTION
    // ============================================================

    public FastApiPredictionResponse predict(
            Path dwiFilePath
    ) {


        // ========================================================
        // FILE RESOURCE
        // ========================================================

        FileSystemResource fileResource =
                new FileSystemResource(
                        dwiFilePath.toFile()
                );


        // ========================================================
        // FILE EXISTS
        // ========================================================

        if (!fileResource.exists()) {

            throw new RuntimeException(
                    "DWI file does not exist: "
                            + dwiFilePath
            );
        }


        // ========================================================
        // FILE READABLE
        // ========================================================

        if (!fileResource.isReadable()) {

            throw new RuntimeException(
                    "DWI file is not readable: "
                            + dwiFilePath
            );
        }


        try {


            // ====================================================
            // DEBUG
            // ====================================================

            System.out.println();

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "CALLING FASTAPI"
            );

            System.out.println(
                    "============================================================"
            );


            System.out.println(
                    "URL : "
                            + predictEndpoint
            );


            System.out.println(
                    "File : "
                            + fileResource.getFilename()
            );


            System.out.println(
                    "Path : "
                            + dwiFilePath.toAbsolutePath()
            );


            System.out.println(
                    "Exists : "
                            + fileResource.exists()
            );


            System.out.println(
                    "Readable : "
                            + fileResource.isReadable()
            );


            System.out.println(
                    "Size : "
                            + fileResource.contentLength()
                            + " bytes"
            );


            System.out.println(
                    "============================================================"
            );


            // ====================================================
            // MULTIPART BODY
            // ====================================================

            MultipartBodyBuilder builder =
                    new MultipartBodyBuilder();


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


            MultiValueMap<String, HttpEntity<?>> multipartBody =
                    builder.build();


            // ====================================================
            // CALL FASTAPI
            // ====================================================

            System.out.println();

            System.out.println(
                    "Sending multipart request to FastAPI..."
            );


            FastApiPredictionResponse response =

                    restClient

                            .post()

                            .uri(
                                    predictEndpoint
                            )

                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )

                            .body(
                                    multipartBody
                            )

                            .retrieve()

                            .body(
                                    FastApiPredictionResponse.class
                            );


            // ====================================================
            // EMPTY RESPONSE
            // ====================================================

            if (response == null) {

                throw new RuntimeException(
                        "FastAPI returned an empty response"
                );
            }


            // ====================================================
            // SUCCESS
            // ====================================================

            System.out.println();

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "FASTAPI RESPONSE RECEIVED"
            );

            System.out.println(
                    "============================================================"
            );


            System.out.println(
                    response
            );


            System.out.println(
                    "============================================================"
            );


            return response;


        } catch (IOException e) {


            // ====================================================
            // FILE ERROR
            // ====================================================

            System.out.println();

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "DWI FILE ERROR"
            );

            System.out.println(
                    "============================================================"
            );


            System.out.println(
                    "File : "
                            + dwiFilePath
            );


            System.out.println(
                    "Error : "
                            + e.getMessage()
            );


            System.out.println(
                    "============================================================"
            );


            throw new RuntimeException(
                    "Unable to read DWI file: "
                            + e.getMessage(),
                    e
            );


        } catch (Exception e) {


            // ====================================================
            // FASTAPI ERROR
            // ====================================================

            System.out.println();

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "FASTAPI REQUEST ERROR"
            );

            System.out.println(
                    "============================================================"
            );


            System.out.println(
                    "Exception type : "
                            + e.getClass().getName()
            );


            System.out.println(
                    "Message : "
                            + e.getMessage()
            );


            System.out.println(
                    "Cause : "
                            + (
                            e.getCause() == null
                                    ? "none"
                                    : e.getCause().toString()
                    )
            );


            System.out.println(
                    "============================================================"
            );


            throw new RuntimeException(
                    "FastAPI prediction failed: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // ============================================================
    // TEST CONNECTION
    // ============================================================

    public FastApiConnectionTestResponse testConnection(
            Path dwiFilePath
    ) {


        // ========================================================
        // FILE RESOURCE
        // ========================================================

        FileSystemResource fileResource =
                new FileSystemResource(
                        dwiFilePath.toFile()
                );


        // ========================================================
        // FILE EXISTS
        // ========================================================

        if (!fileResource.exists()) {

            throw new RuntimeException(
                    "DWI file does not exist: "
                            + dwiFilePath
            );
        }


        // ========================================================
        // FILE READABLE
        // ========================================================

        if (!fileResource.isReadable()) {

            throw new RuntimeException(
                    "DWI file is not readable: "
                            + dwiFilePath
            );
        }


        try {


            // ====================================================
            // DEBUG
            // ====================================================

            System.out.println();

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "TEST SPRING BOOT -> FASTAPI"
            );

            System.out.println(
                    "============================================================"
            );


            System.out.println(
                    "Endpoint : /predict/test"
            );


            System.out.println(
                    "File : "
                            + fileResource.getFilename()
            );


            System.out.println(
                    "Path : "
                            + dwiFilePath.toAbsolutePath()
            );


            System.out.println(
                    "Exists : "
                            + fileResource.exists()
            );


            System.out.println(
                    "Readable : "
                            + fileResource.isReadable()
            );


            System.out.println(
                    "Size : "
                            + fileResource.contentLength()
                            + " bytes"
            );


            System.out.println(
                    "============================================================"
            );


            // ====================================================
            // MULTIPART BODY
            // ====================================================

            MultipartBodyBuilder builder =
                    new MultipartBodyBuilder();


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


            MultiValueMap<String, HttpEntity<?>> multipartBody =
                    builder.build();


            // ====================================================
            // TEST CALL
            // ====================================================

            System.out.println();

            System.out.println(
                    "Sending test multipart request..."
            );


            FastApiConnectionTestResponse response =

                    restClient

                            .post()

                            .uri(
                                    "/predict/test"
                            )

                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )

                            .body(
                                    multipartBody
                            )

                            .retrieve()

                            .body(
                                    FastApiConnectionTestResponse.class
                            );


            // ====================================================
            // EMPTY RESPONSE
            // ====================================================

            if (response == null) {

                throw new RuntimeException(
                        "FastAPI test returned an empty response"
                );
            }


            // ====================================================
            // SUCCESS
            // ====================================================

            System.out.println();

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "FASTAPI TEST SUCCESS"
            );

            System.out.println(
                    "============================================================"
            );


            System.out.println(
                    response
            );


            System.out.println(
                    "============================================================"
            );


            return response;


        } catch (IOException e) {


            throw new RuntimeException(
                    "Unable to read DWI file: "
                            + e.getMessage(),
                    e
            );


        } catch (Exception e) {


            System.out.println();

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "FASTAPI TEST ERROR"
            );

            System.out.println(
                    "============================================================"
            );


            System.out.println(
                    "Exception type : "
                            + e.getClass().getName()
            );


            System.out.println(
                    "Message : "
                            + e.getMessage()
            );


            System.out.println(
                    "Cause : "
                            + (
                            e.getCause() == null
                                    ? "none"
                                    : e.getCause().toString()
                    )
            );


            System.out.println(
                    "============================================================"
            );


            throw new RuntimeException(
                    "FastAPI test failed: "
                            + e.getMessage(),
                    e
            );
        }
    }
}