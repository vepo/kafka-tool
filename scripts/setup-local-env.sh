#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${SCRIPT_DIR}/../resources/docker"
COMPOSE_FILE="${COMPOSE_DIR}/docker-compose.yaml"
DEMO_PROFILE="demo"
KAFKA_BROKERS=(kafka-1 kafka-2 kafka-3)
PRIMARY_BROKER="${KAFKA_BROKERS[0]}"
REPLICATION_FACTOR=3

BOOTSTRAP_SERVERS="localhost:29092,localhost:29093,localhost:29094"
SCHEMA_REGISTRY_URL="http://localhost:8081"
TEST_TOPICS=(
	"users:3"
	"person:3"
	"user-avro:3"
)
ECONOMIC_TOPICS=(
	"economic-cpi:3"
	"economic-gdp:3"
	"economic-unemployment:3"
	"economic-cpi-1m-avg:3"
)

usage() {
	cat <<EOF
Usage: $(basename "$0") [command]

Commands:
  up        Start 3-broker Kafka, Schema Registry, and demo workloads (default)
  down      Stop and remove containers
  restart   Restart the local stack
  reset     Stop the stack and wipe broker data volumes
  status    Show container status
  logs      Follow container logs

Connection details (after 'up'):
  Bootstrap servers:  ${BOOTSTRAP_SERVERS}
  Schema Registry:    ${SCHEMA_REGISTRY_URL}

Demo workloads (JBang containers):
  Producers:          demo-records (users/person/user-avro), economic indexes (World Bank)
  Consumers:          2 instances per demo topic and economic index
  Kafka Streams:      1-minute CPI averages -> economic-cpi-1m-avg

Note: Upgrading from the old single-broker layout requires resetting broker data:
  $(basename "$0") reset
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

wait_for_broker() {
	local broker="$1"
	for _ in $(seq 1 60); do
		if compose exec -T "${broker}" kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >/dev/null 2>&1; then
			echo "  ${broker} is ready."
			return 0
		fi
		sleep 2
	done
	echo "${broker} did not become ready in time." >&2
	compose logs "${broker}" >&2 || true
	return 1
}

wait_for_kafka() {
	echo "Waiting for Kafka cluster (3 brokers)..."
	for broker in "${KAFKA_BROKERS[@]}"; do
		wait_for_broker "${broker}" || exit 1
	done
	echo "Kafka cluster is ready."
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
	local partitions="${2:-3}"

	compose exec -T "${PRIMARY_BROKER}" kafka-topics.sh \
		--bootstrap-server localhost:9092 \
		--create \
		--if-not-exists \
		--topic "${topic}" \
		--partitions "${partitions}" \
		--replication-factor "${REPLICATION_FACTOR}" >/dev/null
	echo "  - ${topic} (${partitions} partitions, RF ${REPLICATION_FACTOR})"
}

create_test_topics() {
	echo "Creating test topics..."
	for entry in "${TEST_TOPICS[@]}"; do
		IFS=':' read -r topic partitions <<< "${entry}"
		create_topic "${topic}" "${partitions}"
	done
}

create_economic_topics() {
	echo "Creating economic index topics..."
	for entry in "${ECONOMIC_TOPICS[@]}"; do
		IFS=':' read -r topic partitions <<< "${entry}"
		create_topic "${topic}" "${partitions}"
	done
}

start_demo_workloads() {
	echo "Starting demo producers, consumers, and Kafka Streams (JBang)..."
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

Demo workloads:
  Brokers:            3 (localhost:29092, localhost:29093, localhost:29094)
  Demo records:       users, person, user-avro (producer + 2 consumers each)
  Economic indexes:   economic-cpi, economic-gdp, economic-unemployment
  Kafka Streams:      economic-cpi -> economic-cpi-1m-avg (1-minute tumbling average)

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
	start_demo_workloads
	print_connection_details
}

cmd_down() {
	require_docker
	compose --profile "${DEMO_PROFILE}" down
	echo "Local environment stopped."
}

cmd_reset() {
	require_docker
	compose --profile "${DEMO_PROFILE}" down 2>/dev/null || true
	docker run --rm -v "${COMPOSE_DIR}/volume:/volume" alpine sh -c \
		"rm -rf /volume/kafka /volume/kafka-1 /volume/kafka-2 /volume/kafka-3"
	echo "Broker data volumes wiped."
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
	reset) cmd_reset ;;
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
