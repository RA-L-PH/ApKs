# ApKs

## Why This App Exists

Google is shifting the Android ecosystem toward a more closed model via a developer verification policy. Starting in late 2026, certified Android devices will restrict apps from developers who have not registered with Google using a government ID. This policy impacts alternative app repositories, sideloading, and independent software distribution.

**Impact on the Android Ecosystem:**

- **Sideloading restrictions**: Installing apps from sources outside the Google Play Store, such as F-Droid, requires navigating complex override steps, including a multi-step 24-hour verification delay.
- **Developer verification**: Developers must submit legal identities, government identification, and app-signing keys to Google, affecting anonymous, open-source, and hobbyist projects.
- **Centralized gatekeeping**: Critics and advocacy groups argue this transition mimics a closed ecosystem by letting Google control software execution on certified hardware.

**Community and Developer Response:**

- Mixed community views, with some arguing that AOSP remains open while Google's proprietary layer tightens control.
- Widespread consensus across privacy and open-source communities that these tighter constraints severely harm developer autonomy and user freedom.

ApKs exists to preserve the ability to install and manage apps freely on your own device. It provides a simple, accessible way to sideload APKs without relying on centralized gatekeepers.

**Learn more:**
- [Keep Android Open](https://keepandroidopen.org/cta/)
- [Pixel Union - Google Closing Android Ecosystem](https://pixelunion.eu/blog/2026/03/google-closing-android-ecosystem/)
- [Linux Tech Tips Discussion](https://linustechtips.com/topic/1636581-android-isnt-open-source-anymore/)

---

## About

ApKs is an Android app that lets you easily install, manage, and uninstall APK/APKS files on your device using the power of Shizuku for elevated operations.

## Features

- **APK Sideloader**: Browse and install APK/APKS files from your device storage
- **Uninstall Apps**: Remove installed apps directly from the APK list
- **Search**: Quickly find APKs by name or package name
- **Pull to Refresh**: Refresh the APK list with a simple swipe
- **Server Management**: Start and manage the Shizuku server via root or wireless ADB
- **Wireless ADB Pairing**: Pair your device for wireless ADB on Android 11+
- **First-Launch Guide**: Onboarding tutorial for new users
- **Accessibility**: Full screen reader support with content descriptions

## How It Works

ApKs uses the Shizuku framework to gain elevated permissions, allowing you to:

1. Install APKs without going through the Play Store
2. Uninstall system and user apps
3. Perform operations that require root or ADB permissions

## Getting Started

### First Launch

When you first open ApKs, you'll see a "Getting Started" guide that walks you through:

1. What ApKs can do
2. How to install APKs
3. How to start the server
4. How to pair your device (for wireless ADB)

### Installing APKs

1. Open the **APKs** tab (default)
2. Grant storage permission when prompted
3. Browse your Downloads, Documents, or root storage
4. Tap **Install** on any APK you want to install
5. The app will install via Shizuku's elevated process

### Starting the Server

1. Go to the **Server** tab
2. Choose your preferred method:
   - **Root**: Direct server start (requires rooted device)
   - **Wireless ADB**: For Android 11+ devices
   - **ADB Command**: Manual start via computer connection

### Pairing (Android 11+)

1. Go to **Settings** (gear icon in toolbar)
2. Tap **Pair Device (Wireless ADB)**
3. Follow the on-screen instructions

## Building from Source

### Prerequisites

- Java 11 or newer (JDK 21 recommended)
- Android SDK with build-tools
- CMake 3.31+

### Build Commands

```bash
# Set Java home (if needed)
export JAVA_HOME="C:\Program Files\Java\jdk-21"

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

The APK will be generated in `manager/build/outputs/apk/`.

### Install on Device

```bash
adb install -r manager/build/outputs/apk/debug/manager-debug.apk
```

## Project Structure

```
app/
├── manager/          # Main app module
│   ├── src/main/
│   │   ├── java/com/rc/apks/
│   │   │   ├── home/           # Server tab (dashboard)
│   │   │   ├── sideloader/     # APKs tab
│   │   │   ├── howto/          # First-launch guide
│   │   │   ├── settings/       # Settings with pairing
│   │   │   ├── adb/            # ADB protocol & pairing
│   │   │   ├── starter/        # Server starter
│   │   │   └── ...
│   │   └── res/
│   │       ├── layout/         # UI layouts
│   │       ├── values/         # Strings & resources
│   │       └── menu/           # Menu definitions
│   └── build.gradle
├── api/              # Shizuku API
├── server/           # Shizuku server
├── shell/            # rish shell
└── starter/          # Native starter
```

## UI Design

ApKs follows **Universal-Centric Design** principles:

- **Accessibility**: Content descriptions on all interactive elements
- **Multiple Interaction Paths**: Search, scroll, and pull-to-refresh
- **Clear Visual Hierarchy**: Status → Actions → Information
- **Error Prevention**: Confirmation dialogs for destructive actions
- **Feedback**: Loading states, success/error messages

## License

All code files in this project are licensed under Apache 2.0.

## Credits

Built on top of the [Shizuku](https://github.com/RikkaApps/Shizuku) framework by Rikka.
