package tn.esprit.test.stroke_backend.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastApiConnectionTestResponse {

    private String status;

    private String message;

    private String filename;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("size_bytes")
    private long sizeBytes;

    public FastApiConnectionTestResponse() {
    }

    // ============================================================
    // STATUS
    // ============================================================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ============================================================
    // MESSAGE
    // ============================================================

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // ============================================================
    // FILENAME
    // ============================================================

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    // ============================================================
    // CONTENT TYPE
    // ============================================================

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    // ============================================================
    // SIZE
    // ============================================================

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    // ============================================================
    // TO STRING
    // ============================================================

    @Override
    public String toString() {

        return "FastApiConnectionTestResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", filename='" + filename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", sizeBytes=" + sizeBytes +
                '}';
    }
}