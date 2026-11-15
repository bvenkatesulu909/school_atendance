# Student Attendance Tracker (Android)

A native Android app that lets teachers manage classes and students, mark daily attendance, and view attendance reports. It works fully offline with a local Room database.

**Stack:** Kotlin · Jetpack Compose · Material 3 · Room · MVVM (ViewModel + Repository) · Robolectric

## Features

- **Dashboard** with an attendance summary for the day
- **Classes:** create and manage classes
- **Student directory** with per-student details and history
- **Mark attendance:** quick present/absent marking per class, with notes
- **Reports:** aggregated attendance statistics per class and per student

## Architecture

```
ui/screens      Compose screens (Dashboard, Classes, MarkAttendance, Reports, StudentDetail, StudentDirectory)
ui/viewmodel    AttendanceViewModel (state + business logic)
data/           Room database, DAO, repository, type converters
data/model      Student, SchoolClass, AttendanceRecord, AttendanceStatus, AggregateData
```

## Build and run

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run on an emulator or a device.

Unit tests (Robolectric) live in `app/src/test`. Run them from Android Studio or with `gradle test`.
