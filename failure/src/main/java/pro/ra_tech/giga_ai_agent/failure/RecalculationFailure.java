package pro.ra_tech.giga_ai_agent.failure;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

public class RecalculationFailure extends AbstractFailure {
    private static final String DETAIL = "Embeddings recalculation failure";
    private final Code code;

    public RecalculationFailure(Code code, String source, @Nullable Throwable cause) {
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
        EMBEDDINGS_RECALCULATION_TASK_ERROR_FAILURE("EMBEDDINGS_RECALCULATION_TASK_ERROR_FAILURE"),
        EMPTY_EMBEDDING_FROM_MODEL_FAILURE("EMPTY_EMBEDDING_FROM_MODEL_FAILURE");

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }
}
