# AGENTS.md — Kafka Tool

Guidance for AI agents working in this repository.

## Project summary

**Kafka Tool** is a JavaFX desktop GUI for Kafka cluster management: configure brokers, list topics, subscribe to topics (Avro / JSON / Protobuf), view messages, and empty topics. Version 0.0.2.

- **Language:** Java 25
- **UI:** JavaFX 25 (no FXML — programmatic UI via `ScreenBuilder`)
- **Build:** Maven
- **Main class:** `io.vepo.kafka.tool.KafkaManagerMainWindow`

## Essential commands

```bash
# Run the app
mvn javafx:run

# Compile, test, package
mvn verify

# Local Kafka stack (Zookeeper + Kafka + Schema Registry)
cd resources/docker && docker compose up
# Bootstrap: localhost:29092  |  Schema Registry: http://localhost:8081
```

## Architecture

Read **`docs/ARCHITECTURE.md`** before making structural changes. Update that file when your change affects layers, data flows, dependencies, or conventions (see `.cursor/rules/architecture-docs.mdc`).

### Package layout

```
io.vepo.kafka.tool/
├── (root)              Entry point + top-level panes (ClusterConnectPane, TopicsPane)
├── inspect/            Kafka Admin API + domain DTOs (KafkaAdminService, TopicInfo, …)
├── consumers/          Format-specific Kafka consumers (Avro, JSON, Protobuf)
├── settings/           JSON persistence to ~/.kafka-tool/
├── stages/             Secondary windows (subscribe, broker config, message viewer)
└── controls/           Reusable UI widgets, builders, helpers
    ├── base/           AbstractKafkaToolStage
    ├── builders/       ScreenBuilder, ResizePolicy
    └── helpers/        WindowHelper, ResizeHelper, ProtobufHelper
```

### Layer rules (short)

| Layer | May call | Must not |
|-------|----------|----------|
| `controls/`, `stages/`, root panes | `inspect/`, `consumers/`, `settings/` | Kafka clients directly |
| `inspect/` | kafka-clients Admin API | JavaFX APIs |
| `consumers/` | kafka-clients Consumer API, Confluent deserializers | JavaFX APIs |
| `settings/` | Jackson, filesystem | Kafka or JavaFX |

## Critical constraints

1. **JavaFX thread safety** — never update UI from Kafka/executor threads; use `Platform.runLater`.
2. **Kafka I/O off the FX thread** — admin work goes through `KafkaAdminService` executor; consumption through stage `consumerExecutor`.
3. **Settings writes** — always via `Settings.updateKafka()`, `Settings.updateUi()`, or serializer update methods; never write JSON files directly from UI code.
4. **Undecorated windows** — use `AbstractKafkaToolStage.setup()` and `WindowHelper.rootControl()` for consistent chrome.

## Testing

- Tests live under `src/test/java/`.
- Run with `mvn test` or `mvn verify`.
- Add tests for new logic in `settings/`, `consumers/`, `inspect/`, and `controls/helpers/` (pure Java, no FX thread needed).
- UI stages are not integration-tested; keep Kafka logic out of stages when possible.
- Protobuf test classes are generated from `src/test/protobuf/` during `generate-test-sources`.

## Dependencies (managed in pom.xml)

| Property | Purpose |
|----------|---------|
| `javafx.version` | OpenJFX controls |
| `kafka.version` | kafka-clients (Admin + Consumer) |
| `confluent.version` | Avro/JSON/Protobuf serializers (Confluent Maven repo required) |
| `jackson.version` | Settings JSON + message formatting |

Bump versions in `pom.xml` properties only; keep Kafka and Confluent versions aligned per [Confluent compatibility matrix](https://docs.confluent.io/platform/current/installation/versions-interoperability.html).

## Code quality rules

Follow `.cursor/rules/java-quality.mdc` for all Java changes.

## What not to do

- Do not commit secrets or local `~/.kafka-tool/` config.
- Do not add FXML unless the project explicitly moves to FXML.
- Do not call removed Kafka 3.x APIs (e.g. `DescribeTopicsResult.all()` — use `allTopicNames()`).
- Do not expand scope beyond the requested task (no drive-by refactors).

## CI

- `.github/workflows/build.yml` — Windows build + SonarCloud on push.
- `.github/workflows/build-artifact.yml` — MSI installer on tags (Windows + jpackage).
