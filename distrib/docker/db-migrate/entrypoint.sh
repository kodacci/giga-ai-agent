#!/bin/bash

set -e

PROPS_FILE_PATH=/config/liquibase/liquibase.properties
echo "Liquibase properties file path: $PROPS_FILE_PATH"
while [ ! -e $PROPS_FILE_PATH ]
do
  echo "Waiting for props file $PROPS_FILE_PATH"
  sleep 1
done

echo "Got liquibase props. Running liquibase ..."
echo ""

liquibase --log-level=INFO --defaults-file $PROPS_FILE_PATH --changelog-file db/changelog-root.yaml update

echo ""
echo "Stopping istio-proxy sidecar to end job execution"
echo ""
curl -v -X POST http://localhost:15020/quitquitquit