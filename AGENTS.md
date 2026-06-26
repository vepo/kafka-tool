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

# Local Kafka stack (KRaft Kafka + Schema Registry)
./scripts/setup-local-env.sh
# Bootstrap: localhost:29092  |  Schema Registry: http://localhost:8081
```

## Architecture

Read **`docs/ARCHITECTURE.md`** before making structural changes. Update that file when your change affects layers, data flows, dependencies, or conventions (see `.cursor/rules/architecture-docs.mdc`).

### Package layout

```
io.vepo.kafka.tool/
├── (root)              Entry point + top-level panes (ClusterConnectPane, TopicsPane)
├── controllers/        MVC controllers (ApplicationController, TopicsController, …)
├── viewmodels/         Presentation models (MessageRow, ConsumerState)
├── inspect/            Kafka Admin API + domain DTOs (KafkaAdminService, TopicInfo, …)
├── consumers/          Format-specific Kafka consumers + TopicConsumerService, KeyFormatter
├── settings/           JSON persistence to ~/.kafka-tool/
│   └── service/        SettingsService (injectable facade for controllers)
├── stages/             Secondary window views (subscribe, broker config, message viewer)
└── controls/           Reusable UI widgets, builders, helpers
    ├── base/           AbstractKafkaToolStage
    ├── builders/       ScreenBuilder, ResizePolicy
    └── helpers/        WindowHelper, ResizeHelper
```

### Layer rules (short)

| Layer | May call | Must not |
|-------|----------|----------|
| Views (`controls/`, `stages/`, root panes) | `controllers/`, `viewmodels/` | `inspect/`, `consumers/`, `Settings` directly |
| `controllers/` | `inspect/`, `consumers/`, `SettingsService`, `viewmodels/` | JavaFX node mutation; Kafka clients directly |
| `inspect/` | kafka-clients Admin API | JavaFX APIs |
| `consumers/` | kafka-clients Consumer API, Confluent deserializers | JavaFX APIs |
| `settings/` | Jackson, filesystem | Kafka or JavaFX |

## Critical constraints

1. **JavaFX thread safety** — never update UI from Kafka/executor threads; controllers use `Platform.runLater`.
2. **Kafka I/O off the FX thread** — admin work through `KafkaAdminService`; consumption through `TopicConsumerService` executor.
3. **Settings writes** — controllers use `SettingsService` (`updateKafka`, `updateUi`, serializer updates); never write JSON files from views.
4. **Undecorated windows** — use `AbstractKafkaToolStage.setup()` and `WindowHelper.rootControl()` for consistent chrome.

## Testing

- Tests live under `src/test/java/`.
- Run with `mvn test` or `mvn verify`.
- Follow **TDD with Gherkin scenarios** (see `.cursor/rules/tdd-gherkin.mdc`): every feature uses `Feature.feature(...).scenario(...).start()` in try-with-resources; integration tests add `.withKafkaBroker()` / `.withSchemaRegistry()` and `-Dkafka.integration=true`.
- Add tests for new logic in `settings/`, `consumers/`, `inspect/`, and `controllers/` (pure Java, no FX thread needed).
- UI stages are not integration-tested; extract logic and scenario-test it.
- Optional readable specs: `src/test/resources/features/*.feature` (JUnit scenario is source of truth).
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
Follow `.cursor/rules/tdd-gherkin.mdc` for tests and new features (Gherkin scenario first).

## What not to do

- Do not commit secrets or local `~/.kafka-tool/` config.
- Do not add FXML unless the project explicitly moves to FXML.
- Do not call removed Kafka 3.x APIs (e.g. `DescribeTopicsResult.all()` — use `allTopicNames()`).
- Do not expand scope beyond the requested task (no drive-by refactors).

## CI

- `.github/workflows/build.yml` — Windows build + SonarCloud on push.
- `.github/workflows/build-artifact.yml` — MSI installer on tags (Windows + jpackage).
