package pro.ra_tech.giga_ai_agent.integration.rest.hfs.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommentRequest(
        @JsonProperty("comment") String comment,
        @JsonProperty("uri") String uri
) {
}
