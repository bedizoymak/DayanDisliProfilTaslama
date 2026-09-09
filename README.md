# DAYAN Dişli & Profil Taşlama (Android)

Standalone native Android port of the DAYAN CALCULATOR module from the `dayandisli.com` web app.
Kotlin + Jetpack Compose, single activity, fully offline. Does **not** wrap the website — every
screen, formula and PDF is implemented natively.

## Source revision

Ported from `C:\Users\Bediz\Documents\dayandisli.com`, `src/calculator/**`, as it existed on disk
during this port (working tree state, September 2026). The parity fixtures referenced in the
Kotlin unit tests cite the restored/corrected upstream commit
`0f71e8c2de417c5ebc9f6eb11c2bc66fa14a7a82` (the last commit before `src/calculator/**` was
deleted, and the commit the web module's own parity tests were written against). The web project
was treated as read-only; nothing under `dayandisli.com/` was modified.

## Build & run

Requires the Android SDK (compileSdk 35, minSdk 26) and a JDK 11+. This project intentionally
mirrors the Gradle/AGP/Kotlin/Compose versions already proven to work in this environment
(Gradle 9.5.0, AGP 9.3.2, Kotlin 2.2.10, Compose BOM 2024.04.01) — see `gradle/libs.versions.toml`.

```bash
# from this directory
JAVA_HOME="/path/to/jdk-or-android-studio/jbr" ./gradlew assembleDebug
JAVA_HOME="/path/to/jdk-or-android-studio/jbr" ./gradlew testDebugUnitTest
```

On Windows with Android Studio installed, the bundled JBR works as `JAVA_HOME`
(`C:\Program Files\Android\Android Studio\jbr`).

Install on a connected device/emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.dayandisli.calculator/.MainActivity
```

Or open the project root in Android Studio and run the `app` configuration directly.

## Project layout

- `app/src/main/java/.../domain/` — pure Kotlin calculation layer (no Android imports): gear
  spur/helical formulas, gear-combination search, weight/cost geometry, machine list, and
  JS-`toFixed`-compatible number formatting. Ported 1:1 from `src/calculator/utils/*.ts` and
  `src/calculator/data/machines.ts`.
- `app/src/main/java/.../ui/` — Compose screens, navigation graph, shared components and
  ViewModels (`SavedStateHandle`-backed inputs, `StateFlow`-derived results).
- `app/src/main/java/.../pdf/` — native PDF receipt generation via `android.graphics.pdf.PdfDocument`
  (no jsPDF/jspdf-autotable equivalent needed), using the same Roboto TTFs the web app embeds for
  Turkish/Greek glyph coverage (`res/font/roboto_regular.ttf`, `roboto_bold.ttf`, copied from
  `dayandisli.com/public/fonts/`).
- `app/src/test/java/.../domain/` — JUnit parity tests translated directly from
  `src/calculator/utils/*.parity.test.ts`, asserting the same numeric expectations.

## Samples

- `samples/pdf/` — sample receipts generated on-device and pulled for inspection.
- `samples/screenshots/` — on-device screenshots covering every screen (Pixel Tablet emulator).
