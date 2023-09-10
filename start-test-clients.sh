#!/bin/bash -e

echo "Starting clients using docker..."

(cd devel/clients && docker-compose up -d)