package pro.ra_tech.giga_ai_agent.database;

public record Constants() {
    public static final String PG_VECTOR_DOCKER_IMAGE_NAME = "pgvector/pgvector:0.8.1-pg18-trixie";
    public static final int PG_VECTOR_DOCKER_PORT = 5432;
}
