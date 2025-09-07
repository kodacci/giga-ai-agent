# syntax=docker/dockerfile:1
FROM liquibase/liquibase:4.33

LABEL authors="Andrey Ryabtsev"

COPY --chown=liquibase:liquibase db-migrate/src/main/resources/db ./db/
COPY --chown=liquibase:liquibase distrib/docker/db-migrate/entrypoint.sh ./
RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["/liquibase/entrypoint.sh"]
