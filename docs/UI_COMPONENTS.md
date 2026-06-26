# UI Component Catalog

Last updated: 2026-06-25

Reusable JavaFX building blocks for Kafka Tool. All UIs are programmatic (no FXML). Prefer these over ad-hoc layouts in stages.

| Catalog id | Source | Purpose | Style / CSS |
|------------|--------|---------|-------------|
| `AppShell` | `WindowHelper`, `WindowHead`, `ResizeHelper` | Main window chrome: title bar, draggable/resizable undecorated frame | `WindowHead`, `.window-chrome-button` |
| `MainNav` | `MainWindowPane` | Left sidebar tab navigation between main views | `MainWindowPane .button.selected` |
| `CenteredPanel` | `CentralizedPane` | Vertically centered content (connect screen) | `ClusterConnectPane` background |
| `DialogShell` | `AbstractKafkaToolStage` | Modal/non-modal secondary windows with persisted size | via `SettingsService` |
| `FormGrid` | `ScreenBuilder.grid()` | Label + field grid for forms and dialogs | `.screen-grid` |
| `FormLabel` | `ScreenBuilder.addText()` | Static form labels | `.form-label` |
| `FormTextField` | `ScreenBuilder.addTextField()` | Single-line text input | `.text-field`, `.text-field.invalid` |
| `FormComboBox` | `ScreenBuilder.addComboBox()` | Dropdown selection | `.combo-box` |
| `FormTextArea` | `ScreenBuilder.addTextArea()` | Multi-line read-only or editable text | default |
| `FormButton` | `ScreenBuilder.addButton()` | Primary actions in forms | `.button` |
| `FormValidationLabel` | `ScreenBuilder.addValidationLabel()` | Inline validation errors | `.validation-message` |
| `DataTable` | `ScreenBuilder.addTableView()` + column builders | Sortable/editable tables with action columns | `.screen-grid .table-view` |
| `ConsumerStatusBar` | `TopicConsumerStatusBar` | Live consumer status and current offset | default text |
| `EmptyState` | `EmptyStatePane` | Placeholder when a list has no items | `.empty-state`, `.empty-state-message` |
| `ProgressStatusBar` | `ProgressStatusBar` | Loading indicator + status text | `.progress-status-bar` |
| `StatusAlert` | `UserMessage` | Standard error/info/warning dialogs | JavaFX `Alert` |
| `ResizePolicy` | `ResizePolicy` | Fixed vs proportional table column widths | n/a |

## Examples

**Broker form** — `BrokerConfigurationStage` uses `FormGrid`, `FormTextField`, `FormValidationLabel`, `DataTable`.

**Topic subscribe** — `TopicSubscribeStage` uses `FormComboBox`, `DataTable`, `ConsumerStatusBar`.

**Connect screen** — `ClusterConnectPane` uses `CenteredPanel`, `FormComboBox`, `FormButton`.

**Consumer groups** — `ConsumerGroupsPane` uses `DataTable`, `EmptyState`, `ProgressStatusBar`, `FormButton`.

**Record browse** — `RecordBrowseStage` uses `FormGrid`, `FormComboBox`, `DataTable`, `EmptyState`.

## Planned / avoid duplicating

- Do not add raw `GridPane` layouts in stages when `FormGrid` suffices.
- Do not instantiate `Alert` directly in views; use `StatusAlert` (`UserMessage`).

When adding a component, update this table and set **Last updated**.
