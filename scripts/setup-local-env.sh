#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${SCRIPT_DIR}/../resources/docker"
COMPOSE_FILE="${COMPOSE_DIR}/docker-compose.yaml"
DEMO_PROFILE="demo"

BOOTSTRAP_SERVERS="localhost:29092"
SCHEMA_REGISTRY_URL="http://localhost:8081"
TEST_TOPICS=(users person user-avro)
ECONOMIC_TOPICS=(
	"economic-cpi:2"
	"economic-gdp:2"
	"economic-unemployment:2"
)

usage() {
	cat <<EOF
Usage: $(basename "$0") [command]

Commands:
  up        Start Kafka, Schema Registry, and demo consumers (default)
  down      Stop and remove containers
  restart   Restart the local stack
  status    Show container status
  logs      Follow container logs

Connection details (after 'up'):
  Bootstrap servers:  ${BOOTSTRAP_SERVERS}
  Schema Registry:    ${SCHEMA_REGISTRY_URL}

Demo data (World Bank open economic indexes):
  Topics:             economic-cpi, economic-gdp, economic-unemployment
  Consumer groups:    cpi-analytics (2), gdp-analytics (2), unemployment-analytics (2)
EOF
}

require_docker() {
	if ! command -v docker >/dev/null 2>&1; then
		echo "Docker is required but was not found in PATH." >&2
		exit 1
	fi
	if ! docker compose version >/dev/null 2>&1; then
		echo "Docker Compose v2 is required (docker compose)." >&2
		exit 1
	fi
}

compose() {
	docker compose -f "${COMPOSE_FILE}" "$@"
}

wait_for_kafka() {
	echo "Waiting for Kafka..."
	for _ in $(seq 1 60); do
		if compose exec -T kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >/dev/null 2>&1; then
			echo "Kafka is ready."
			return 0
		fi
		sleep 2
	done
	echo "Kafka did not become ready in time." >&2
	compose logs kafka >&2 || true
	exit 1
}

wait_for_schema_registry() {
	echo "Waiting for Schema Registry..."
	for _ in $(seq 1 30); do
		if curl -sf "${SCHEMA_REGISTRY_URL}/subjects" >/dev/null 2>&1; then
			echo "Schema Registry is ready."
			return 0
		fi
		sleep 2
	done
	echo "Schema Registry did not become ready in time." >&2
	compose logs schema-registry >&2 || true
	exit 1
}

create_topic() {
	local topic="$1"
	local partitions="${2:-1}"

	compose exec -T kafka kafka-topics.sh \
		--bootstrap-server localhost:9092 \
		--create \
		--if-not-exists \
		--topic "${topic}" \
		--partitions "${partitions}" \
		--replication-factor 1 >/dev/null
	echo "  - ${topic} (${partitions} partition(s))"
}

create_test_topics() {
	echo "Creating test topics..."
	for topic in "${TEST_TOPICS[@]}"; do
		create_topic "${topic}" 1
	done
}

create_economic_topics() {
	echo "Creating economic index topics..."
	for entry in "${ECONOMIC_TOPICS[@]}"; do
		IFS=':' read -r topic partitions <<< "${entry}"
		create_topic "${topic}" "${partitions}"
	done
}

start_demo_consumers() {
	echo "Starting economic index producer and consumers (JBang)..."
	compose --profile "${DEMO_PROFILE}" up -d
}

print_connection_details() {
	cat <<EOF

Local environment is ready.

  Bootstrap servers:  ${BOOTSTRAP_SERVERS}
  Schema Registry:    ${SCHEMA_REGISTRY_URL}

Example broker profile for Kafka Tool:
  Name:              local
  Bootstrap servers: ${BOOTSTRAP_SERVERS}
  Schema Registry:   ${SCHEMA_REGISTRY_URL}

Demo economic indexes (World Bank open data):
  Topics:             economic-cpi, economic-gdp, economic-unemployment
  Consumer groups:    cpi-analytics, gdp-analytics, unemployment-analytics (2 members each)

Produce sample user records:
  ${SCRIPT_DIR}/produce-records

Stop the stack:
  $(basename "$0") down
EOF
}

cmd_up() {
	require_docker
	compose up -d
	wait_for_kafka
	wait_for_schema_registry
	create_test_topics
	create_economic_topics
	start_demo_consumers
	print_connection_details
}

cmd_down() {
	require_docker
	compose --profile "${DEMO_PROFILE}" down
	echo "Local environment stopped."
}

cmd_restart() {
	require_docker
	compose --profile "${DEMO_PROFILE}" down
	cmd_up
}

cmd_status() {
	require_docker
	compose --profile "${DEMO_PROFILE}" ps
}

cmd_logs() {
	require_docker
	compose --profile "${DEMO_PROFILE}" logs -f
}

main() {
	local command="${1:-up}"
	case "${command}" in
	up) cmd_up ;;
	down) cmd_down ;;
	restart) cmd_restart ;;
	status) cmd_status ;;
	logs) cmd_logs ;;
	-h | --help | help) usage ;;
	*)
		echo "Unknown command: ${command}" >&2
		usage >&2
		exit 1
		;;
	esac
}

main "$@"
