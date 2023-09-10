#!/bin/bash -e

echo "Starting Kafka cluster using docker..."

(cd docker && docker-compose up -d)