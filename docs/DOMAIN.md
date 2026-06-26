# Kafka Tool — Domain Language

Last updated: 2026-06-26

This document is the **ubiquitous language** for Kafka Tool. Agents must **read it before any code change** and **update it in the same change** when terminology, boundaries, or domain behaviour shifts.

Architecture and layering: **`docs/ARCHITECTURE.md`**. UI catalogue: **`docs/UI_COMPONENTS.md`**.

## Bounded contexts

| Context | Package(s) | What it models |
|---------|------------|----------------|
| **Broker configuration** | `settings/` | Saved broker profiles, UI layout, per-topic serializer choices |
| **Cluster inspection** | `inspect/` | Topics, consumer groups, cluster health, connection outcomes, browse metadata |
| **Record consumption** | `consumers/` | Live subscribe, one-shot fetch, key/value interpretation for display |
| **Presentation** | `viewmodels/`, `controllers/`, root panes, `stages/`, `controls/` | Screen state and orchestration — not Kafka domain logic |

Cross-context rule: Kafka I/O and domain decisions stay in `settings/`, `inspect/`, and `consumers/`. Views and controllers orchestrate; they do not re-implement domain rules.

## Ubiquitous language

### Broker configuration

| Term | Meaning |
|------|---------|
| **Broker profile** (`KafkaBroker`) | A named, persisted connection profile: display name, bootstrap servers, optional Schema Registry URL. |
| **Bootstrap servers** | Comma-separated `host:port` list used to reach the Kafka cluster. |
| **Schema Registry URL** | Optional HTTP(S) base URL for Confluent Schema Registry. When absent, value consumption is limited to JSON. |
| **Broker settings** (`KafkaSettings`) | The collection of saved broker profiles in `~/.kafka-tool/kafka-properties.json`. |
| **Serializer settings** (`SerializerSettings`) | Per-topic choices for how record keys and values are interpreted (`KeySerializer`, `ValueSerializer`). |
| **UI settings** (`UiSettings`) | Window and dialog dimensions persisted in `~/.kafka-tool/ui-properties.json`. |
| **Validation result** | Outcome of validating a broker profile — valid flag plus human-readable message. |

### Cluster and topics

| Term | Meaning |
|------|---------|
| **Topic** (`TopicInfo`) | A Kafka topic name plus whether it is an internal (`__…`) topic. |
| **Connection result** (`ConnectionResult`) | Success or failure of connecting to or testing a broker profile. |
| **Cluster summary** (`ClusterSummary`, `ClusterMonitorSnapshot`) | Aggregated cluster health: brokers, partitions, replication, log dirs. |
| **Partition health issue** | Under-replicated, offline, or leader-not-preferred partition state. |
| **Broker log dir summary** | Disk usage for a broker’s log directories. |

### Records and consumption

| Term | Meaning |
|------|---------|
| **Record** / **Kafka message** (`KafkaMessage`, `FetchedRecord`) | A single Kafka record: raw key bytes and a display-ready value string. |
| **Message metadata** (`MessageMetadata`) | Partition, offset, and timestamp for a consumed record. |
| **Live subscribe** | Continuous consumption on a dedicated executor; rows appear in the subscribe UI. |
| **Record browse** | One-shot assign-and-seek fetch at a chosen partition and offset. |
| **Agnostic consumer** (`KafkaAgnosticConsumer`) | Consumer configured for Avro, JSON Schema, Protobuf, or plain bytes — chosen per topic. |
| **Key serializer** / **Value serializer** | Enum describing how key bytes or deserialized value objects are turned into display text. |
| **Formatted record key** | Display string derived from raw key bytes and a `KeySerializer`. |
| **Formatted record value** | Display string derived from a deserialized value object (Avro record, Protobuf message, JSON map, bytes). |

### Consumer groups

| Term | Meaning |
|------|---------|
| **Consumer group** (`ConsumerGroupSummary`) | A Kafka consumer group id and high-level state. |
| **Group member** (`ConsumerGroupMemberInfo`) | A member of a consumer group with client id and assigned partitions. |
| **Partition lag row** (`PartitionLagRow`) | Lag for one topic-partition within a group. |

### Presentation (UI-only)

| Term | Meaning |
|------|---------|
| **Message row** (`MessageRow`) | Table row for the UI: partition, offset, timestamp, formatted key/value. |
| **Consumer state** (`ConsumerState`) | UI lifecycle: `IDLE`, `RUNNING`, `STOPPED`, `ERROR`. |

## Naming and object design

Follow `.cursor/rules/ddd-domain.mdc`. Summary:

- Name types after **what they are** in this language (broker profile, record value, partition lag), not job titles.
- **Do not** introduce new classes whose simple name ends with `Helper`, `Util`, or `Utils`.
- Prefer **immutable** domain types (`record`, `final` fields, no setters on values).
- Domain behaviour belongs **on the type that owns the data** (validate on `KafkaBroker`, format on a `RecordKey` / `RecordValue`), not scattered static procedures.
- New domain types should be **`final`** (or **`abstract`** when intentionally incomplete). See [Seven Virtues of a Good Object](https://www.yegor256.com/2014/11/20/seven-virtues-of-good-object.html).

### Legacy names (rename when you touch the file)

| Current | Preferred direction |
|---------|---------------------|
| `ProtobufHelper` | `ProtobufRecord` or behaviour on formatted record value |
| `WindowHelper` | `ApplicationWindow` / window root factory in `controls/` |
| `ResizeHelper` | `WindowResize` / resize policy object |
| `KeyFormatter` | `RecordKey` (encapsulates bytes + serializer → display text) |
| `RecordValueFormatter` | `RecordValue` (encapsulates deserialized payload → display text) |
| `KafkaBrokerValidator` | validation methods on `KafkaBroker` returning `ValidationResult` |

Existing `*Service` types (`KafkaAdminService`, `TopicConsumerService`, …) remain documented here until refactored; prefer nouns for **new** domain types and keep services as thin orchestration over Kafka clients.

## When to update this document

Update **`docs/DOMAIN.md`** (and set **Last updated**) when you:

1. Add, rename, or remove a domain term used in code or UI copy
2. Change what a term means (e.g. new serializer, new connection outcome)
3. Move domain logic between packages or bounded contexts
4. Introduce a replacement for a legacy name listed above

Do **not** duplicate full architecture diagrams here — link to `docs/ARCHITECTURE.md` instead.
