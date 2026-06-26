# UI Component Catalog

Last updated: 2026-06-25

Reusable JavaFX building blocks for Kafka Tool. All UIs are programmatic (no FXML). Prefer these over ad-hoc layouts in stages.

Follow [Eclipse UI Guidelines](https://www.eclipse.org/articles/Article-UI-Guidelines/Index.html): each view has a **title**, **description**, and **inline message area** for status (no modal dialogs for success/error).

| Catalog id | Source | Purpose | Style / CSS |
|------------|--------|---------|-------------|
| `AppShell` | `WindowHelper`, `WindowHead`, `ResizeHelper` | Main window chrome: title bar, draggable/resizable undecorated frame | `WindowHead`, `.window-chrome-button` |
| `MainNav` | `MainWindowPane` | Left sidebar tab navigation between main views | `MainWindowPane .button.selected` |
| `ViewHeader` | `ViewHeader`, `ViewMessageModel` | Eclipse-style view title, description, inline info/success/warning/error banner | `.view-header`, `.view-header-message.*` |
| `CenteredPanel` | `CentralizedPane` | Vertically centered content (legacy; prefer `ViewHeader` + `VBox`) | `ClusterConnectPane` background |
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
| `ProgressStatusBar` | `ProgressStatusBar` | Loading indicator (bind `loading`; status text lives in `ViewHeader`) | `.progress-status-bar` |
| `UserConfirmation` | `UserConfirmation` | Modal OK/Cancel for destructive actions only | JavaFX `Alert` CONFIRMATION |
| `ResizePolicy` | `ResizePolicy` | Fixed vs proportional table column widths | n/a |

## Examples

**Broker form** — `BrokerConfigurationStage` uses `ViewHeader`, `FormGrid`, `FormTextField`, `FormValidationLabel`, `DataTable`.

**Topic subscribe** — `TopicSubscribeStage` uses `ViewHeader`, `FormComboBox`, `DataTable`, `ConsumerStatusBar`.

**Connect screen** — `ClusterConnectPane` uses `ViewHeader`, `FormComboBox`, `FormButton`, `ProgressStatusBar`.

**Consumer groups** — `ConsumerGroupsPane` uses `ViewHeader`, `DataTable`, `EmptyState`, `ProgressStatusBar`, `FormButton`.

**Record browse** — `RecordBrowseStage` uses `ViewHeader`, `FormComboBox`, `DataTable`, `EmptyState`.

## Planned / avoid duplicating

- Do not add raw `GridPane` layouts in stages when `FormGrid` suffices.
- Do not use modal `Alert` for success/error/info; use `ViewHeader` + `ViewMessageModel`.
- Use `UserConfirmation` only when the user must commit to a destructive action.

When adding a component, update this table and set **Last updated**.
