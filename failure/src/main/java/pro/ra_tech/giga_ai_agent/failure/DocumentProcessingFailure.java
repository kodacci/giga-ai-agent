package pro.ra_tech.giga_ai_agent.failure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class DocumentProcessingFailure extends AbstractFailure<DocumentProcessingFailure.Code> {
    private static final String DETAIL = "Document processing failure";

    @Getter
    private final HttpStatus httpStatus;

    public DocumentProcessingFailure(Code code, String source, @Nullable Throwable cause) {
        super(code, DETAIL, source, cause);

        httpStatus = code == Code.UNSUPPORTED_DOCUMENT_CONTENT_TYPE
                ? HttpStatus.UNSUPPORTED_MEDIA_TYPE
                : HttpStatus.BAD_REQUEST;
    }

    public DocumentProcessingFailure(Code code, String source) {
        super(code, DETAIL, source, (Throwable) null);

        httpStatus = code == Code.UNSUPPORTED_DOCUMENT_CONTENT_TYPE
                ? HttpStatus.UNSUPPORTED_MEDIA_TYPE
                : HttpStatus.BAD_REQUEST;
    }

    @RequiredArgsConstructor
    public enum Code {
        DOCUMENT_PROCESSING_FAILURE("DOCUMENT_PROCESSING_FAILURE"),
        PDF_PROCESSING_FAILURE("PDF_PROCESSING_FAILURE"),
        EMBEDDING_FAILURE("EMBEDDINGS_PROCESSING_FAILURE"),
        HFS_FILE_PROCESSING_FAILURE("HFS_FILE_PROCESSING_FAILURE"),
        CHUNK_PROCESSING_TASK_ERROR("CHUNK_PROCESSING_TASK_ERROR"),
        UNSUPPORTED_DOCUMENT_CONTENT_TYPE("UNSUPPORTED_DOCUMENT_CONTENT_TYPE");

        private final String value;

        @Override
        public String toString() { return value; }
    }
}
