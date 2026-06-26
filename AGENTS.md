# AGENTS.md — Kafka Tool

Guidance for AI agents working in this repository.

## Project summary

**Kafka Tool** is a JavaFX desktop GUI for Kafka cluster management: configure brokers, list topics, subscribe to topics (Avro / JSON / Protobuf), view messages, and empty topics. Version 0.0.2.

- **Language:** Java 25
- **UI:** JavaFX 25 (no FXML — programmatic UI via **`UI`** builder API)
- **Build:** Maven
- **Main class:** `io.vepo.kafka.tool.KafkaManagerMainWindow`

## Essential commands

```bash
# Run the app
mvn javafx:run

# Compile, test, package (unit tests only)
mvn verify

# Unit + Kafka bridge integration tests (Testcontainers; requires Docker)
mvn verify -Dkafka.integration=true

# Local Kafka stack (KRaft Kafka + Schema Registry)
./scripts/setup-local-env.sh
# Bootstrap: localhost:29092,localhost:29093,localhost:29094  |  Schema Registry: http://localhost:8081
```

## Domain and architecture

Read **`docs/DOMAIN.md`** before any code change — ubiquitous language, bounded contexts, naming rules.

Read **`docs/ARCHITECTURE.md`** before making structural changes. UI components: **`docs/UI_COMPONENTS.md`** (see `.cursor/rules/ui-components.mdc`).

### Package layout

```
io.vepo.kafka.tool/
├── (root)              Entry point + top-level panes (ClusterConnectPane, TopicsPane)
├── controllers/        MVC controllers (ApplicationController, TopicsController, …)
├── viewmodels/         Presentation models (MessageRow, ConsumerState)
├── inspect/            Domain DTOs + KafkaAdminService facade
│   └── bridge/         KafkaAdminBridge, KafkaConsumerBridge (impl uses kafka-clients)
├── consumers/          TopicConsumerService, formatters (no direct kafka-clients)
├── settings/           JSON persistence to ~/.kafka-tool/
│   └── service/        SettingsService (injectable facade for controllers)
├── stages/             Secondary window views
└── controls/           Reusable UI widgets (see docs/UI_COMPONENTS.md)
    ├── base/           AbstractKafkaToolStage
    ├── builders/       UI, TableBuilder, ScreenBuilder, ResizePolicy
    └── helpers/        WindowHelper, ResizeHelper
```

### Layer rules (short)

| Layer | May call | Must not |
|-------|----------|----------|
| Views (`controls/`, `stages/`, root panes) | `controllers/`, `viewmodels/` | `inspect/`, `consumers/`, `Settings` directly |
| `controllers/` | `KafkaAdminService`, `SettingsService`, `viewmodels/` | JavaFX node mutation; `org.apache.kafka.clients.*` |
| `KafkaAdminService`, `TopicConsumerService`, `RecordBrowseService` | `inspect/bridge/*` | `AdminClient`, `KafkaConsumer` |
| `inspect/bridge/impl/` | kafka-clients | JavaFX |
| `settings/` | Jackson, filesystem | Kafka or JavaFX |

## Critical constraints

1. **JavaFX thread safety** — never update UI from Kafka/executor threads; controllers use `Platform.runLater`.
2. **Kafka I/O off the FX thread** — admin work through `KafkaAdminService`; consumption through `TopicConsumerService` executor.
3. **Settings writes** — controllers use `SettingsService` (`updateKafka`, `updateUi`, serializer updates); never write JSON files from views.
4. **Undecorated windows** — use `AbstractKafkaToolStage.setup()` and `WindowHelper.rootControl()` for consistent chrome.

## Testing

- Tests live under `src/test/java/`.
- Run with `mvn test` or `mvn verify`.
- Follow **TDD with Gherkin scenarios** (see `.cursor/rules/tdd-gherkin.mdc`): every feature uses `Feature.feature(...).scenario(...).start()` in try-with-resources; integration tests add `.withKafkaBroker()` / `.withSchemaRegistry()`, `@Tag("integration")`, and `-Dkafka.integration=true` (Testcontainers — no manual Compose required in CI).
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

Follow `.cursor/rules/ddd-domain.mdc` for domain naming, ubiquitous language, and object design.
Follow `.cursor/rules/immutability.mdc` for immutable data carriers — prefer `record` over POJOs.
Follow `.cursor/rules/java-quality.mdc` for all Java changes.
Follow `.cursor/rules/ui-builder.mdc` for all UI in panes, stages, and controls.
Follow `.cursor/rules/tdd-gherkin.mdc` for tests and new features (Gherkin scenario first).
Follow `.cursor/rules/project-scripts.mdc` for scripts and automation.

## Scripts

- Do **not** use Python, Node.js, or other general-purpose script languages — use **JBang** instead (see `scripts/produce-records`).
- **Bash** scripts are allowed (e.g. `scripts/setup-local-env.sh` for Docker Compose).

## What not to do

- Do not use Python, Node.js, or similar script runtimes for tooling in this repo.
- Do not commit secrets or local `~/.kafka-tool/` config.
- Do not add FXML unless the project explicitly moves to FXML.
- Do not call removed Kafka 3.x APIs (e.g. `DescribeTopicsResult.all()` — use `allTopicNames()`).
- Do not expand scope beyond the requested task (no drive-by refactors).
- Do not add classes named `*Helper`, `*Util`, or `*Utils`; use domain nouns from `docs/DOMAIN.md`.

## CI

- `.github/workflows/build.yml` — Linux build, tests, SonarCloud on push/PR.
- `.github/workflows/release-prepare.yml` — manual **Prepare release** (Actions → Run workflow): runs tests on the current `-SNAPSHOT` in `pom.xml`, commits the release version (without `-SNAPSHOT`), pushes the `v*` tag, bumps to the next patch `-SNAPSHOT`, then triggers **Release** (tag pushes from `GITHUB_TOKEN` do not start other workflows).
- `.github/workflows/build-artifact.yml` — MSI installer on `v*` tags or when triggered by prepare (Windows + jpackage → GitHub Release).
