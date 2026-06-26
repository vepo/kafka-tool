# Kafka Tool

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=coverage)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=vepo_kafka-tool&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=vepo_kafka-tool)

A Kafka GUI client for browsing clusters, consuming topics, and monitoring consumer groups.

## Features

* **Configure brokers** — add/edit/delete profiles with validation; test connection before connect
* **Connect / disconnect** — connect to a saved cluster; disconnect to return to the connect screen
* **Topics** — list topics, empty topic, live subscribe, browse records by partition/offset
* **Subscribe** — live consume with Avro, JSON, Protobuf, or Plain Text; partition/offset/timestamp columns; message viewer
* **Browse records** — fetch up to 500 records from a chosen partition and start offset
* **Consumer groups** — list groups, member assignments, per-partition lag (auto-refresh optional)
* **Windows installer** — MSI via CI on version tags

UI building blocks are cataloged in [docs/UI_COMPONENTS.md](docs/UI_COMPONENTS.md).

## Run locally

```bash
mvn javafx:run
```

Local Kafka stack: `./scripts/setup-local-env.sh` (bootstrap `localhost:29092`, Schema Registry `http://localhost:8081`).
