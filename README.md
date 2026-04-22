# Simple Reminder

A modern, elegant Android application built with **Clean Architecture** and **MVI (Model-View-Intent)** patterns. This app allows users to quickly create, manage, and receive timely notifications for their tasks.

## 🚀 Features

- **Quick Add**: Seamlessly create reminders using a floating elevated input bar on the Home Screen.
- **Modern UI**: Built entirely with Jetpack Compose following Material 3 design principles.
- **Smart Notifications**: Automatically schedules notifications 30 minutes before the 1-hour default deadline.
- **Task Management**: Edit reminder details including name and precise deadline via Date and Time pickers.
- **Auto-Cleanup**: Tasks marked as "DONE" are automatically removed from the list to keep your view clutter-free.
- **Home Screen Widget**: View your live reminders directly on your home screen with real-time synchronization and deep-linking support.

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture + MVI
- **Dependency Injection**: Dagger Hilt
- **Local Database**: Room (SQLite)
- **Scheduling**: AlarmManager
- **Navigation**: Jetpack Compose Navigation
- **Widget**: AppWidget Framework (RemoteViews)

## 🏗 Architecture Overview

The project follows a strict three-layer Clean Architecture:

1.  **Domain**: Contains purely business logic, Models, Repository Interfaces, and Use Cases. It has no dependencies on Android frameworks.
2.  **Data**: Implements the Repository interfaces, manages the Room database, handles notifications via `AlarmManager`, and provides widget updates.
3.  **UI (Presentation)**: Implements the MVI pattern. ViewModels expose a single `State` and handle `Intents` from the Composable screens.

## 📦 Installation & Setup

1.  Clone the repository:
    ```bash
    git clone https://github.com/nadhifhayazee/Simple-Reminder.git
    ```
2.  Open the project in **Android Studio (Ladybug or newer)**.
3.  Ensure you have the latest **Kotlin** and **KSP** plugins installed.
4.  Build and run the app on an emulator or physical device (Android 8.0+ recommended).

## 📄 License

This project is open-source and available under the MIT License.
