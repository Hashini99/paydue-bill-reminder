# PayDue

PayDue is an Android app for keeping track of money you need to pay and money other people owe you. You get a reminder on the due date. It works fully offline: all data stays on your device.

## Features

- **Track dues in both directions:** record bills you need to pay, and payments owed to you.
- **Categories:** Rent, Subscription, EMI / Loan, Utility, Insurance, Education, Shopping, Client work and Other.
- **Repeating dues:** set a due to happen once, monthly or yearly.
- **Reminders:** get a notification on the due date. Reminders are set again after the phone restarts.
- **Insights:** see where your money goes, with category and 6-month charts.
- **Invoices:** create invoices, save them as PDFs and share them.
- **App lock:** protect the app with a PIN, fingerprint or face unlock.
- **Backup and restore:** export your data as JSON or CSV, and import it again to replace or add to what you have.
- **Multiple currencies:** choose the currency you want amounts shown in.

## Tech stack

- Kotlin and Jetpack Compose (Material 3)
- Navigation Compose with an MVVM structure (ViewModels and repositories)
- Room to store data on the device
- DataStore Preferences for settings
- AndroidX Biometric for the app lock
- AlarmManager and BroadcastReceivers for reminders
- Android `PdfDocument` for invoice PDFs

## Requirements

- Android 8.0 (API 26) or newer
- Android Studio (a recent stable version) with JDK 11 or newer

## Getting started

```bash
git clone https://github.com/<your-username>/paydue-android.git
cd paydue-android
./gradlew assembleDebug
```

Or open the project in Android Studio and run the `app` configuration on a device or emulator.

## Project structure

```
app/src/main/java/com/hrdevzone/paydue/
├── data/
│   ├── local/        # Room entities, DAOs, database
│   ├── repository/   # Dues, invoices, payment events
│   ├── backup/       # JSON / CSV export and import
│   ├── pdf/          # Invoice PDF generator
│   ├── prefs/        # DataStore user preferences
│   └── model/        # Categories, direction, repeat type
├── notification/     # Reminder scheduling and receivers
├── di/               # App container (manual dependency injection)
├── ui/               # Compose screens, ViewModels, theme, navigation
└── util/             # PIN hashing, biometrics, formatters, sharing
```
