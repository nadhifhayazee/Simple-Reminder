# AI Context & Mandates: Simple Reminder

This file provides foundational context for AI assistants and agents working on this codebase.

## 🏗 Architectural Mandates
- **Pattern**: Clean Architecture + MVI (Model-View-Intent).
- **Layers**: 
    - `domain`: Pure Kotlin, no Android dependencies. Contains Models, Repository interfaces, and Use Cases.
    - `data`: Implementation of repositories, Room DB, Notifications, and Widgets.
    - `ui`: Jetpack Compose. ViewModels MUST use `StateFlow` for state and `SharedFlow` for one-time effects.
- **Dependency Injection**: Hilt. Always use constructor injection.

## 📋 Naming Conventions
- **MVI Contracts**: Always named `[Feature]Contract.kt` and contain `[Feature]Intent`, `[Feature]State`, and `[Feature]Effect`.
- **Use Cases**: Named `[Action][Entity]UseCase.kt` (e.g., `AddReminderUseCase.kt`).
- **Mappers**: Domain-to-Entity and vice-versa should be extension functions in `data/mapper/`.

## 🛠 Tech Stack Constraints
- **UI**: Jetpack Compose (Material 3).
- **Database**: Room.
- **Async**: Kotlin Coroutines & Flow.
- **Testing**: MockK, Turbine, and JUnit 4.

## ⚠️ Critical Rules
1. **No Logic in UI**: Composables should only emit Intents and render State. Avoid any data transformation or grouping logic in UI files.
2. **Atomic Composables**: Break large UI screens into small, reusable components in `components/` sub-folders. Standardize component extraction.
3. **Encapsulated Side Effects**: UseCases MUST encapsulate all associated side effects (e.g., `WidgetUpdater`, `NotificationScheduler`) to ensure atomic data consistency.
4. **Domain Layer Purity**: The `domain` layer MUST remain a pure Kotlin module. External side effects (Widgets, Notifications) MUST be defined via interfaces in `domain` and implemented in `data`.
5. **Pre-Processed State**: ViewModels or UseCases MUST perform data transformations (grouping, sorting, formatting) before updating State. UI should receive "ready-to-render" data.
6. **Notification Precision**: Always use `AlarmManager.setExactAndAllowWhileIdle` for reminder deadlines.
7. **Widget Sync**: Every data modification (Add/Update/Delete) MUST trigger a widget refresh via the `WidgetUpdater` interface.
8. **Testing**: New features MUST include corresponding Unit Tests for UseCases or ViewModels. Verify rollover and lifecycle logic explicitly.
9. **No Logic Duplication**: Complex logic (like date math or rollover rules) MUST reside in a single UseCase and be reused across the app and widget.

## 🔗 Key Entry Points
- `MainActivity.kt`: Handles Deep Links from the Widget.
- `ReminderNotificationReceiver.kt`: Entry point for scheduled alarms.
- `ReminderWidgetProvider.kt`: Manages the Home Screen widget lifecycle.
