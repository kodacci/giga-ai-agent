# syntax=docker/dockerfile:1
FROM docker-registry.ra-tech.pro/pro.ra-tech/docker-base/eclipse-jre-25-curl:noble

LABEL authors="Andrey Ryabtsev"

RUN useradd -U giga-ai-agent
WORKDIR /home/giga-ai-agent
USER giga-ai-agent
COPY --chown=giga-ai-agent:giga-ai-agent  ../../../core/target/giga-ai-agent-core.jar ./
COPY --chown=giga-ai-agent:giga-ai-agent distrib/docker/core/entrypoint.sh ./
RUN chmod +x ./entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/home/giga-ai-agent/entrypoint.sh"]