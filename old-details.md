# Old details (pre-2026-11-24 refresh)

Archived on 2026-11-24 before the repo-details refresh. Current details are in
README.md and the repo metadata.

## About description (before)

Generic proposals and hypothetical economics. No model or live offer has been selected.

## Website field (before)

(none)

## README.md (before)

```markdown
# Tumbuh

Public proposals and hypothetical economics. No model has been selected; no workshop is on sale; no signup, payment, customer-data collection, domain purchase or availability claim.

Open `index.html`, `economics/index.html` or `workshop-proposal/index.html` via any static HTTP server. The pages use local HTML/CSS/SVG only, no build step, tracking or network calls except when following cited external links.

## Contents
- `economics/`: seven models, historical estimates, both scenario charts, assumptions/rounding note, CSV and plain-text copy. Inputs dated24Oct2026, not current quotes.
- `workshop-proposal/`: generic landing-copy proposal, disabled enrollment controls and explicit pilot-loss note.

This repository contains the public proposal site and the Android app. A disabled workshop concept is also preserved in Kaji. Personal biography and identifying context are excluded. No license or trademark rights are asserted by this publication.


## Android app

The Android project is in [`android/`](android/), with its original commit history and release tags retained. Its AGPL-3.0 license is in [`android/LICENSE`](android/LICENSE); this consolidation does not change the website licensing terms above.

Build from that directory with `cd android && ./gradlew testDebugUnitTest assembleDebug` using JDK 17 and Android SDK 35. The package ID remains `com.vanipy.tumbuh`; installed app data and signing identity are unchanged. No app feature or version change is part of this repository consolidation.

Android qualification, emulator and signed-release workflows are at the repository root under `.github/workflows/android-*.yml`. Pages deploys only the existing website files, never the Android source tree. Existing website paths are unchanged.

Historical downloads remain available in the source repository while the matching releases are preserved here. No repositories or release assets are deleted.

```
