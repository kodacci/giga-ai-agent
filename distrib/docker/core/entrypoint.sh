#!/bin/bash

ENV_PATH=/application/.env
echo "Configuration file path: $ENV_PATH"
while [ ! -e $ENV_PATH ]
do
  echo "Waiting for application environment $ENV_PATH"
  sleep 1
done

echo "Environment file found, starting main app ..."

env -S "$(cat $ENV_PATH)" java -jar /home/giga-ai-agent/giga-ai-agent-core.jar
