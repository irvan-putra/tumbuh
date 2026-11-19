> Development has moved to [tumbuh/android](https://github.com/irvan-putra/tumbuh/tree/main/android). The consolidated repository contains the original app history, release tags and matching APKs. This repository retains old download links for compatibility. Do not uninstall the app or clear its data for this repository move. Package ID and signing identity are unchanged.

# tumbuh (Android app)

tumbuh helps you find your bearings when you are carrying too much: dropped commitments, a full inbox, a messy space, and people you lost touch with.

You add what is weighing on you by hand, give it a weight from 1 to 5, and decide for each item: keep it, renegotiate it, ask for help, let it go, or mark it done. For each decision you get a small first step. For renegotiating, asking for help and reconnecting you get a draft message to edit and send yourself. "Start here" on the home screen shows the heaviest open item.

Version 0.1.1 (icon update; 0.1.0 was the first MVP).

## What it does not do

- It does not read your email, messages, contacts or calendar. Everything is added by hand.
- It does not send anything. Drafts are text you copy or share yourself.
- It has no internet permission, no account, no sync and no reminders yet.
- It does not know how heavy something is unless you say so.
- Not yet tested on a real phone.

## Your data

Items are stored in the app's private storage on the phone. Android's app backup is off. Use "Save a backup file" to keep a JSON copy, and "Restore from a backup file" to load one; restoring first keeps a copy of the current data. Archived items are kept and can be brought back; nothing is deleted.

## Build

Android, Java, no third-party libraries. `./gradlew testDebugUnitTest assembleDebug` (JDK 17, Android SDK 35). The GitHub Actions workflow runs the tests, builds the debug APK and checks its package ID, version and that it requests no permissions.

Package ID: `com.vanipy.tumbuh`.

## License

GNU Affero General Public License v3.0. See `LICENSE`.
