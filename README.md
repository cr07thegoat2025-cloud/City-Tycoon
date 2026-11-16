# City Tycoon

A native Android city-building game written in Kotlin with Jetpack Compose and a custom Canvas renderer.

## Build
- Android Studio
- JDK 17
- Android SDK 35

Open this repository in Android Studio and run the `:app` configuration, or build with:

```bash
./gradlew assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Current milestone
Milestone 1 foundation: native landscape app, tile map rendering, camera drag, building selection and basic placement. Further simulation systems are planned for subsequent milestones.
