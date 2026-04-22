# System Documentation: Simple Reminder

This document details the internal workings, design decisions, and system-level integrations of the Simple Reminder application.

## 1. System Components

### 1.1 Data Flow (MVI)
The application uses a unidirectional data flow (UDF) via the MVI pattern:
- **Intent**: User actions (e.g., `AddReminder`) are sent to the ViewModel.
- **State**: The ViewModel processes the intent through Use Cases and updates a single `HomeState` flow.
- **Effect**: One-time events (e.g., `ShowError`) are sent via a `SharedFlow`.

### 1.2 Persistence (Room)
- **Database**: `ReminderDatabase` manages a single table `reminders`.
- **Sorting**: Queries are sorted by `createdAt DESC` to ensure new tasks appear at the top.
- **Migration**: Uses `fallbackToDestructiveMigration()` for easy development; schema versioning is enforced for major changes.

### 1.3 Notification System
The app uses a decoupled notification strategy:
- **Scheduler**: `ReminderNotificationScheduler` uses `AlarmManager.setExactAndAllowWhileIdle` for precision.
- **Lead Time**: Notifications are triggered 1,800,000ms (30 minutes) before the `deadline`.
- **Receiver**: `ReminderNotificationReceiver` handles the broadcast, creates the `NotificationChannel`, and displays the `NotificationCompat` with a custom vector icon.

### 1.4 Home Screen Widget
The widget is a separate system process integration:
- **Provider**: `ReminderWidgetProvider` manages the lifecycle and sets up the `PendingIntent` template for deep-linking.
- **Service**: `ReminderWidgetService` provides the `RemoteViewsFactory`.
- **Data Fetching**: The Factory uses a Hilt `EntryPoint` to access the repository and `runBlocking` with a 3s timeout to fetch data synchronously for the widget.

## 2. Technical Decisions

- **Clean Architecture**: Chosen to separate business logic from the rapidly changing Android UI and Framework APIs (like AlarmManager vs WorkManager).
- **Hilt**: Standardizes dependency injection across Android components (Activity, ViewModel, and BroadcastReceivers).
- **Deeper Design**:
    - **Floating Elevated Card**: Used for the input field to create a cohesive visual language with task cards.
    - **Vector Drawables**: Preferred over Mipmaps for notifications to ensure compatibility across various launcher icons.

## 3. Deep Link Logic
When a user clicks a reminder in the widget:
1.  The `RemoteViews` sends a `fillInIntent` containing `reminderId`.
2.  `MainActivity` receives this in `onCreate` or `onNewIntent`.
3.  A `LaunchedEffect` in `MainActivity` detects the intent and commands the `NavController` to navigate to `Screen.Edit.createRoute(id)`.

## 4. Key Configurations
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35
- **Permissions**:
    - `POST_NOTIFICATIONS`: Requested at runtime (Android 13+).
    - `SCHEDULE_EXACT_ALARM`: Verified and requested via System Settings (Android 12+).
