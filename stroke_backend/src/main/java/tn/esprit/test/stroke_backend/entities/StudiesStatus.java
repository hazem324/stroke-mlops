package tn.esprit.test.stroke_backend.entities;

public enum StudiesStatus {
    UPLOADED,          // fichier reçu, pas encore traité (remplace READY)
    PROCESSING,        // FastAPI en train de traiter
    COMPLETED,         // segmentation générée, en attente de revue médecin
    AWAITING_REVIEW,   // alias explicite de COMPLETED côté médecin — voir note plus bas
    VALIDATED,         // médecin a validé (Accept ou Correct + Save)
    FAILED
}