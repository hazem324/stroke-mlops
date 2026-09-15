package tn.esprit.test.stroke_backend.services;

import org.springframework.stereotype.Component;

import tn.esprit.test.stroke_backend.entities.Prediction;

@Component
public class GeminiReportPromptBuilder {

    public static final String RESULTATS_MARKER = "###RESULTATS###";
    public static final String CONCLUSION_MARKER = "###CONCLUSION###";

    public String buildPrompt(Prediction prediction) {

        boolean lesionDetected =
                Boolean.TRUE.equals(prediction.getLesionDetected());

        if (lesionDetected) {
            return buildPromptLesionDetectee(prediction);
        }

        return buildPromptSansLesion();
    }

    private String buildPromptLesionDetectee(Prediction prediction) {

        String volume = formatValue(prediction.getLesionVolumeMm3());
        String voxels = formatValue(prediction.getLesionVoxels());

        return """
                Tu rédiges uniquement les sections Résultats et Conclusion
                d’un compte rendu classique d’IRM cérébrale en français.

                Le style doit être celui d’un radiologue :
                sobre, professionnel, descriptif, clair et objectif.

                RÈGLES STRICTES :
                - N’invente aucune information.
                - N’établis pas de diagnostic définitif.
                - Utilise une formulation prudente comme :
                  "aspect compatible avec..."
                - Ne mentionne jamais l’intelligence artificielle.
                - Ne mentionne jamais Gemini.
                - Ne mentionne jamais un algorithme.
                - Ne mentionne jamais la segmentation automatique.
                - Ne mentionne jamais le temps de traitement.
                - Ne mentionne jamais les coordonnées voxel.
                - Ne mentionne jamais les coordonnées physiques.
                - Ne mentionne jamais la boîte englobante.
                - Ne déduis pas le lobe cérébral, l’hémisphère ou le territoire artériel.
                - Ne parle pas de l’ADC, de la FLAIR ou d’autres séquences
                  qui ne sont pas fournies.
                - Ne prétends pas avoir analysé des images non disponibles.
                - N’utilise ni markdown, ni puces, ni astérisques.
                - Ne répète pas les intitulés Résultats et Conclusion.
                - Rédige deux paragraphes séparés.
                - La conclusion doit recommander une corrélation
                  clinico-radiologique et une validation spécialisée.

                Données disponibles :
                - Une anomalie focale a été détectée sur la séquence DWI.
                - Volume estimé de la région concernée : %s mm³.
                - Nombre de voxels concernés : %s.

                Format de sortie obligatoire :

                %s
                <paragraphe Résultats>

                %s
                <paragraphe Conclusion>
                """.formatted(
                volume,
                voxels,
                RESULTATS_MARKER,
                CONCLUSION_MARKER
        );
    }

    private String buildPromptSansLesion() {

        return """
                Tu rédiges uniquement les sections Résultats et Conclusion
                d’un compte rendu classique d’IRM cérébrale en français.

                Le style doit être celui d’un radiologue :
                sobre, professionnel, descriptif, clair et objectif.

                RÈGLES STRICTES :
                - N’invente aucune information.
                - Ne mentionne jamais l’intelligence artificielle.
                - Ne mentionne jamais Gemini.
                - Ne mentionne jamais un algorithme.
                - Ne mentionne jamais la segmentation automatique.
                - Ne parle que de la séquence DWI étudiée.
                - Ne parle pas de l’ADC, de la FLAIR ou d’autres séquences
                  qui ne sont pas fournies.
                - N’établis pas de diagnostic définitif.
                - N’utilise ni markdown, ni puces, ni astérisques.
                - Ne répète pas les intitulés Résultats et Conclusion.
                - Rédige deux paragraphes séparés.
                - La conclusion doit recommander une corrélation clinique
                  et une confrontation aux autres séquences d’imagerie
                  lorsqu’elles sont disponibles.

                Données disponibles :
                - Aucune lésion focale détectée sur la séquence DWI étudiée.

                Format de sortie obligatoire :

                %s
                <paragraphe Résultats>

                %s
                <paragraphe Conclusion>
                """.formatted(
                RESULTATS_MARKER,
                CONCLUSION_MARKER
        );
    }

    private String formatValue(Object value) {
        return value == null ? "non renseigné" : value.toString();
    }
}