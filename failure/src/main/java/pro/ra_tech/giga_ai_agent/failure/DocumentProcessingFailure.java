package pro.ra_tech.giga_ai_agent.failure;

import lombok.RequiredArgsConstructor;

public class DocumentProcessingFailure extends AbstractFailure {
    private static final String DETAIL = "Document processing failure";
    private final Code code;

    public DocumentProcessingFailure(Code code, String source, Throwable cause) {
        super(source, cause);
        this.code = code;
    }

    @Override
    public String getCode() {
        return code.toString();
    }

    @Override
    public String getDetail() {
        return DETAIL;
    }

    @RequiredArgsConstructor
    public enum Code {
        PDF_PROCESSING_FAILURE("PDF_PROCESSING_FAILURE"),
        EMBEDDING_FAILURE("EMBEDDINGS_PROCESSING_FAILURE");

        private final String value;

        @Override
        public String toString() { return value; }
    }
}
