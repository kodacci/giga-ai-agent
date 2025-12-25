package pro.ra_tech.giga_ai_agent.failure;

import lombok.RequiredArgsConstructor;

public class DatabaseFailure extends AbstractFailure<DatabaseFailure.Code> {
    private static final String DETAIL =  "Database access error";

    public DatabaseFailure(Code code, String source, Throwable cause) {
        super(code, DETAIL, source, cause);
    }

    @RequiredArgsConstructor
    public enum Code {
        TRANSACTION_FAILURE("TRANSACTION_FAILURE"),
        TAG_REPOSITORY_FAILURE("TAGS_REPOSITORY_FAILURE"),
        SOURCE_REPOSITORY_FAILURE("SOURCE_REPOSITORY_FAILURE"),
        EMBEDDINGS_REPOSITORY_FAILURE("EMBEDDINGS_REPOSITORY_FAILURE"),
        DOC_PROCESSING_TASK_REPOSITORY_FAILURE("DOC_PROCESSING_TASK_REPOSITORY_FAILURE"),
        EMBEDDINGS_RECALCULATION_TASK_REPOSITORY_FAILURE("EMBEDDINGS_RECALCULATION_TASK_REPOSITORY_FAILURE");

        private final String value;

        @Override
        public String toString() { return value; }
    }
}
