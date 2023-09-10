#!/bin/bash -e

echo "Starting Kafka cluster using docker..."

(cd devel/cluster && docker-compose up -d)