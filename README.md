# CAMS - College Academic Management System

CAMS is a comprehensive, multi-portal College ERP (Enterprise Resource Planning) application built natively for Android using modern Kotlin and Jetpack Compose. It serves as a unified digital ecosystem connecting all stakeholders of an educational institution.

## 🚀 Portals & Demo Credentials

CAMS provides role-based access for different users. Use the following credentials to explore each portal:

| Portal | Role | Email | Password |
| :--- | :--- | :--- | :--- |
| **Student Portal** | Access academics, attendance, fees, and campus life | `student@cams.edu` | `password` |
| **Parent Portal** | Track student progress, attendance, and fee payments | `parent@cams.edu` | `password` |
| **Faculty Portal** | Mark attendance, upload grades, and manage assignments | `faculty@cams.edu` | `password` |
| **HOD Portal** | Monitor department metrics and manage faculty | `hod@cams.edu` | `password` |
| **Principal Portal** | College-wide analytics and top-level approvals | `principal@cams.edu` | `password` |
| **Admin Portal** | System configuration and user management | `admin@cams.edu` | `password` |

## 🛠️ Software Requirements

To build and run this application locally, you need:

1. **Android Studio**: Android Studio Jellyfish (2023.3.1) or newer is recommended.
2. **Java Development Kit (JDK)**: JDK 17 is required by the Android Gradle Plugin.
3. **Android SDK**: Minimum SDK 26 (Android 8.0), Target SDK 34 (Android 14).
4. **Git**: To clone the repository and manage version control.

## ⚙️ Configuration & Environment

The core configuration for the application is located at:
`app/src/main/java/com/example/core/config/AppConfig.kt`

Before deploying to a real environment, you should modify this file:
- `BASE_URL`: Change this to point to your live production server (e.g., `https://api.yourcollege.edu/v1/`).
- `DATABASE_NAME`: Update the local Room database name if desired.
- `TIMEOUT`: Adjust the `CONNECT_TIMEOUT_MS` network timeout settings if your servers are slow.

## 📦 Build Instructions

Use the following Gradle commands in the terminal at the root of the project to build the app:

**To build a Debug APK for local testing:**
```bash
./gradlew assembleDebug
```
*Output location: `app/build/outputs/apk/debug/app-debug.apk`*

**To build a Release APK for production deployment:**
```bash
./gradlew assembleRelease
```
*Output location: `app/build/outputs/apk/release/app-release.apk`*

**To build an Android App Bundle (AAB) for Google Play Store upload:**
```bash
./gradlew bundleRelease
```
*Output location: `app/build/outputs/bundle/release/app-release.aab`*

## 🔄 Version Control (GitHub Setup)

To push this repository to your own GitHub account:

1. Open your terminal in the project root directory.
2. Run the following commands:
```bash
git init
git add .
git commit -m "Initial commit - CAMS application"
git branch -M main
git remote add origin https://github.com/yourusername/your-repo-name.git
git push -u origin main
```
*(Make sure to replace the GitHub URL with your actual repository link).*

## 🤐 Packaging for Sharing

If you need to share the project code with other developers without using Git, you should compress it into a clean ZIP file. 

**Using AI Studio:**
Simply click on the **Settings Menu** (gear icon) -> **Download Project as ZIP**.

**Using Mac/Linux Terminal:**
Run the following command to package the code while ignoring heavy build folders:
```bash
zip -r CAMS_Project.zip . -x "*.git*" "*build*" "*.gradle*" "*.idea*"
```

**Using Windows:**
Delete the `.gradle`, `.idea`, `build`, and `app/build` folders manually, then right-click the main folder and select **Compress to ZIP file**.
