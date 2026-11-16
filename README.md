# CITY TYCOON

A native Android city-building game built with Kotlin, Jetpack Compose and a custom Canvas map renderer. This is a genuine Android app, not a website or WebView.

## Playable systems in this build

- Landscape tile map with building placement, selection, demolition and land expansion.
- Residential, commercial, industrial, road, public-service and decoration catalogues.
- Building prices, daily maintenance, business income, housing capacity and jobs.
- Simulated days, tax control, cash flow and bankruptcy warnings.
- Residents grow over time when housing, employment and happiness conditions allow; population is saved locally.
- Happiness responds to parks, services, pollution and tax pressure.
- City levels unlock larger buildings.
- Achievement panel with city-building milestones.
- Simple moving traffic markers appear on connected adjacent road tiles.
- Local save/continue using Android SharedPreferences.
- Touch controls, camera drag, pinch zoom and OTG keyboard shortcuts.

## Controls

| Input | Action |
|---|---|
| Tap a building tool, then a map tile | Place a building |
| Select tool, then a building | Inspect a building |
| Drag | Pan camera |
| Pinch | Zoom |
| WASD / Arrow keys | Move camera |
| B | Open build tab |
| R | Select road |
| Space | Pause / resume |
| 1 / 2 / 3 | Normal / 2x / 3x speed |
| Escape | Cancel current selection |

Mouse click/tap uses the same map pointer controls. External keyboard support is available when the Android device reports hardware key events.

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

GitHub Actions builds a debug APK on pushes to `main`. Open the repository's **Actions** tab, choose the latest successful **Android APK** run, and download the `city-tycoon-debug-apk` artifact. Extract the ZIP and install `app-debug.apk` on an Android device.

## Simulation scope

This is a playable early version, not the full feature list from the original master brief. Residents are simulated as a city-wide population value rather than individual agents with destinations. Traffic markers animate on adjacent road tiles, but there is no full road pathfinding, congestion model, delivery routing or emergency-vehicle dispatch yet. Public buildings currently affect service/happiness values in a simplified way. Sound, music, detailed service coverage, and advanced upgrades are not included yet.
