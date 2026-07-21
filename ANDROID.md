# Android app

Kotlin + Jetpack Compose. The project root **is** the Android project — open this folder
directly in Android Studio.

## Setup

```bash
cp .env.example .env                          # set API_BASE_URL
cp local.properties.example local.properties  # set sdk.dir
```

Then press **Run** in Android Studio, or:

```bash
gradlew.bat assembleDebug        # build a debug APK
gradlew.bat compileDebugKotlin   # type-check only, faster
```

## Requirements

- Android Studio (Giraffe or newer), JDK 11
- compileSdk / targetSdk **34**, minSdk **24**
- The backend running and reachable

## Pointing the app at a backend

One line in `.env`:

| Setup | Value |
|---|---|
| Emulator | `http://10.0.2.2:8000/api/v1/` |
| Physical device | `http://<your-computer-LAN-IP>:8000/api/v1/` |
| Production | `https://api.your-college.edu/api/v1/` |

`10.0.2.2` is a special address meaning "the computer running the emulator". For a real
device, use your computer's LAN IP and keep the phone on the same Wi-Fi. Keep the trailing
slash. No Kotlin changes are needed.

## Layout

```
app/src/main/java/com/example/
├── core/
│   ├── network/      Retrofit service, DTOs, AuthInterceptor
│   ├── database/     Room cache (speeds up screens; not the real data)
│   ├── navigation/   routes
│   ├── repository/   API responses -> UI models
│   ├── theme/  ui/   shared styling and widgets
│   └── di/           dependency wiring
└── features/
    ├── auth/         role selection, login, password change
    ├── student/  parent/  faculty/  hod/  principal/  admin/
    └── shared/       reused across roles
```

Data flows **Screen → ViewModel → Repository → Retrofit**. Screens never call the network
directly.

## Release builds

Signing values come from `local.properties` and are never committed:

```bash
gradlew.bat assembleRelease
```

## Troubleshooting

**All screens fail to load** — the backend is unreachable. Check it is running and that
`API_BASE_URL` matches your setup (emulator vs physical device).

**Images or documents do not appear** — these are served through an authenticated endpoint.
The app uses an image loader wired to the auth interceptor; if you add a new image call,
use that loader rather than a plain URL.

**Login says too many attempts** — the backend rate-limits failed logins per account
(10 per 15 minutes). Wait, or use a different test account.
