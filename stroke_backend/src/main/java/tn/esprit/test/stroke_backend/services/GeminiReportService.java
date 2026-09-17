package tn.esprit.test.stroke_backend.services;

import static tn.esprit.test.stroke_backend.utils.GeminiReportPromptBuilder.CONCLUSION_MARKER;
import static tn.esprit.test.stroke_backend.utils.GeminiReportPromptBuilder.RESULTATS_MARKER;

import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tn.esprit.test.stroke_backend.entities.GeneratedSections;
import tn.esprit.test.stroke_backend.entities.Prediction;
import tn.esprit.test.stroke_backend.services.servicesInterface.IGeminiReportService;
import tn.esprit.test.stroke_backend.utils.GeminiReportPromptBuilder;

@Service
public class GeminiReportService implements IGeminiReportService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    @Value("${gemini.base.url}")
    private String geminiBaseUrl;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final GeminiReportPromptBuilder promptBuilder;

    public GeminiReportService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            GeminiReportPromptBuilder promptBuilder
    ) {
        /*
         * Désactivation des retries automatiques du client Apache.
         * Les retries sont contrôlés manuellement dans callGeminiWithRetry().
         */
        CloseableHttpClient httpClient = HttpClients.custom().disableAutomaticRetries().build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();

        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public GeneratedSections generateReport(Prediction prediction) {

        if (prediction == null) {
            throw new IllegalArgumentException(
                    "Prediction cannot be null"
            );
        }

        String prompt = promptBuilder.buildPrompt(prediction);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        String url = buildGeminiUrl();

        String response = callGeminiWithRetry( url,requestBody);

        String rawText = extractText(response);

        return splitSections(rawText);
    }

    private String buildGeminiUrl() {

        if (geminiBaseUrl == null || geminiBaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "gemini.base.url n'est pas configuré."
            );
        }

        if (geminiModel == null || geminiModel.isBlank()) {
            throw new IllegalStateException(
                    "gemini.model n'est pas configuré."
            );
        }

        String baseUrl = geminiBaseUrl.trim();

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(
                    0,
                    baseUrl.length() - 1
            );
        }

        String model = geminiModel.trim();

        if (model.startsWith("/")) {
            model = model.substring(1);
        }

        if (model.startsWith("models/")) {
            model = model.substring("models/".length());
        }

        return baseUrl
                + "/models/"
                + model
                + ":generateContent";
    }

    private String callGeminiWithRetry( String url,Map<String, Object> requestBody) {

        final int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {

                String response = restClient.post()
                        .uri(url)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                if (response == null || response.isBlank()) {
                    throw new IllegalStateException(
                            "Gemini a retourné une réponse vide."
                    );
                }

                return response;

            } catch (HttpStatusCodeException exception) {

                int statusCode = exception
                        .getStatusCode()
                        .value();

                String responseBody = exception
                        .getResponseBodyAsString();

                if (statusCode == 400) {
                    throw new IllegalStateException(
                            "Requête Gemini invalide. "
                                    + "Vérifiez le modèle, le prompt "
                                    + "et le format de la requête. "
                                    + "Réponse Google : "
                                    + responseBody
                    );
                }

                if (statusCode == 401 || statusCode == 403) {
                    throw new IllegalStateException(
                            "La clé Gemini est invalide, expirée "
                                    + "ou non autorisée. "
                                    + "Vérifiez gemini.api.key, "
                                    + "les permissions de l'API "
                                    + "et la facturation éventuelle. "
                                    + "Réponse Google : "
                                    + responseBody
                    );
                }

                if (statusCode == 404) {
                    throw new IllegalStateException(
                            "Le modèle Gemini est introuvable : "
                                    + geminiModel
                                    + ". Vérifiez gemini.model "
                                    + "et la liste des modèles disponibles. "
                                    + "Réponse Google : "
                                    + responseBody
                    );
                }

                if (statusCode == 429) {

                    if (attempt < maxAttempts) {
                        waitBeforeRetry(attempt);
                        continue;
                    }

                    throw new IllegalStateException(
                            "La limite de requêtes Gemini a été atteinte "
                                    + "après "
                                    + maxAttempts
                                    + " tentatives. "
                                    + "Réessayez plus tard."
                    );
                }

                if (statusCode == 500 || statusCode == 502
                        || statusCode == 503 || statusCode == 504) {

                    if (attempt < maxAttempts) {
                        waitBeforeRetry(attempt);
                        continue;
                    }

                    throw new IllegalStateException(
                            "Le service Gemini est temporairement indisponible "
                                    + "ou surchargé après "
                                    + maxAttempts
                                    + " tentatives. "
                                    + "Veuillez réessayer plus tard. "
                                    + "Code HTTP : "
                                    + statusCode
                    );
                }

                throw new IllegalStateException(
                        "Erreur Gemini HTTP "
                                + statusCode
                                + " : "
                                + responseBody
                );

            } catch (ResourceAccessException exception) {

                throw new IllegalStateException(
                        "Impossible de contacter Gemini. "
                                + "Vérifiez la connexion réseau, le DNS "
                                + "et l'accès à "
                                + "generativelanguage.googleapis.com.",
                        exception
                );
            }
        }

        throw new IllegalStateException(
                "Échec de communication avec Gemini."
        );
    }

    private void waitBeforeRetry(int attempt) {

        long delayInMilliseconds;

        if (attempt == 1) {
            delayInMilliseconds = 2000L;
        } else {
            delayInMilliseconds = 5000L;
        }

        try {

            Thread.sleep(delayInMilliseconds);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "La tentative de communication avec Gemini "
                            + "a été interrompue.",
                    exception
            );
        }
    }

    private String extractText(String response) {

        if (response == null || response.isBlank()) {
            throw new IllegalStateException(
                    "Gemini a retourné une réponse vide."
            );
        }

        try {

            JsonNode root = objectMapper.readTree(response);

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {

                String errorMessage = extractGoogleError(root);

                throw new IllegalStateException(
                        errorMessage
                );
            }

            JsonNode firstCandidate = candidates.path(0);

            JsonNode content = firstCandidate.path("content");

            JsonNode parts = content.path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                throw new IllegalStateException(
                        "La réponse Gemini ne contient aucune partie de contenu."
                );
            }

            JsonNode textNode = parts
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode()
                    || textNode.asText().isBlank()) {

                throw new IllegalStateException(
                        "La réponse Gemini ne contient aucun texte généré."
                );
            }

            return textNode.asText().trim();

        } catch (IllegalStateException exception) {

            throw exception;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Impossible d'analyser la réponse Gemini.",
                    exception
            );
        }
    }

    private String extractGoogleError(JsonNode root) {

        JsonNode errorNode = root.path("error");

        if (!errorNode.isMissingNode()) {

            String message = errorNode
                    .path("message")
                    .asText("");

            String status = errorNode
                    .path("status")
                    .asText("");

            if (!message.isBlank()) {

                return "Erreur Gemini API"
                        + (status.isBlank()
                        ? ""
                        : " [" + status + "]")
                        + " : "
                        + message;
            }
        }

        JsonNode promptFeedback = root.path("promptFeedback");

        if (!promptFeedback.isMissingNode()) {

            String blockReason = promptFeedback
                    .path("blockReason")
                    .asText("");

            if (!blockReason.isBlank()) {
                return "Le prompt Gemini a été bloqué. "
                        + "Raison : "
                        + blockReason;
            }
        }

        return "La réponse Gemini ne contient aucun candidat.";
    }

    private GeneratedSections splitSections(String rawText) {

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalStateException(
                    "Gemini a généré un rapport vide."
            );
        }

        String resultats = between(
                rawText,
                RESULTATS_MARKER,
                CONCLUSION_MARKER
        );

        String conclusion = after(
                rawText,
                CONCLUSION_MARKER
        );

        resultats = cleanGeneratedText(resultats);
        conclusion = cleanGeneratedText(conclusion);

        if (resultats.isBlank() || conclusion.isBlank()) {

            throw new IllegalStateException(
                    "Réponse Gemini mal formatée : "
                            + "les marqueurs "
                            + RESULTATS_MARKER
                            + " et "
                            + CONCLUSION_MARKER
                            + " sont introuvables."
            );
        }

        return new GeneratedSections(
                resultats,
                conclusion
        );
    }

    private String between(String text,String startMarker,String endMarker) {

        int start = text.indexOf(startMarker);
        int end = text.indexOf(endMarker);

        if (start == -1 || end == -1 || end <= start) {
            return "";
        }

        return text.substring( start + startMarker.length(),end);
    }

    private String after(String text,String marker) {

        int index = text.indexOf(marker);

        if (index == -1) {
            return "";
        }

        return text.substring(
                index + marker.length()
        );
    }

    private String cleanGeneratedText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("**", "")
                .replace("*", "")
                .replace("#", "")
                .replace("Résultats :", "")
                .replace("Résultats:", "")
                .replace("Conclusion :", "")
                .replace("Conclusion:", "")
                .replace("RESULTATS :", "")
                .replace("RESULTATS:", "")
                .replace("CONCLUSION :", "")
                .replace("CONCLUSION:", "")
                .trim();
    }
}