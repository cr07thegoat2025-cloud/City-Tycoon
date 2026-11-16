# CITY TYCOON

A native Android city-building game built with Kotlin, Jetpack Compose and a custom Canvas map renderer. This is an Android app, not a website or WebView.

## Current playable systems

- Landscape city map with tile placement, camera panning and pinch zoom.
- Residential, commercial, industrial, service, road and decoration building catalog.
- Building prices, maintenance, business income, housing capacity and job capacity.
- Day-based economy simulation, tax control, happiness, pollution pressure and bankruptcy warnings.
- City level unlocks, purchasable land expansion, selection and demolition refunds.
- Save/continue using Android local preferences.
- Touch controls plus keyboard camera movement, build shortcuts, pause and simulation speed controls.

## Controls

| Input | Action |
|---|---|
| Tap building tool, then map tile | Place a building |
| Select tool, then building | Inspect a building |
| Drag | Pan camera |
| Pinch | Zoom |
| WASD / Arrow keys | Move camera |
| B | Open build tab |
| R | Select road |
| Space | Pause / resume |
| 1 / 2 / 3 | Normal / 2x / 3x speed |
| Escape | Cancel current selection |

A mouse can tap/click the map and use the same pointer controls. Keyboard and mouse support depends on the Android device and connected OTG hardware.

## Build an APK

Requirements:
- JDK 17
- Android SDK 35
- Gradle 8.9

Open the repository in Android Studio and run the app, or build from a machine with Android SDK installed:

```bash
gradle --no-daemon assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds the debug APK on pushes to `main`. Open the repository's **Actions** tab, choose the latest **Android APK** run, and download the `city-tycoon-debug-apk` artifact after the run succeeds. Extract the ZIP and install the APK on an Android device.

## Scope note

This is an evolving indie-game implementation. Citizen behaviour, traffic routing, emergency response, achievements, sound, and detailed service coverage are not yet implemented as full simulation systems. The current population and employment figures are lightweight management estimates, not individually animated citizen agents. Do not treat a successful APK build as proof that every feature in the original design brief is complete.
