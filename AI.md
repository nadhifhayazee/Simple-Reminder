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
1. **No Logic in UI**: Composables should only emit Intents and render State.
2. **Atomic Composables**: Break large UI screens into small, reusable components (e.g., in a `components/` sub-folder). Avoid monolithic `@Composable` functions longer than 50-100 lines.
3. **Notification Precision**: Always use `AlarmManager.setExactAndAllowWhileIdle` for reminder deadlines.
4. **Widget Sync**: Any data change (Add/Update/Delete) MUST trigger `WidgetUpdater.updateWidget()`.
5. **Testing**: New features MUST include a corresponding Unit Test for the Use Case or ViewModel.

## 🔗 Key Entry Points
- `MainActivity.kt`: Handles Deep Links from the Widget.
- `ReminderNotificationReceiver.kt`: Entry point for scheduled alarms.
- `ReminderWidgetProvider.kt`: Manages the Home Screen widget lifecycle.
