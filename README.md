# Afyalink Android

**Afyalink v1.1.1** is an offline-first blood-pressure log for Android. Manual readings are encrypted with AES-256-GCM before they are written to the app's private, no-backup vault. The monitor screen is hidden for now; the existing Bluetooth SIG Blood Pressure Service adapter is reserved for a later release.

The app has no `INTERNET` permission. Its demo sync performs a real RSA-3072 OAEP + AES-256-GCM envelope round-trip against a device-local relay and then discards the payload. **No reading is transmitted to a server.** A production relay stays disabled until the owner has completed the required legal, privacy, security, operational and vendor reviews.

> **Demo security warning:** the release APK is signed with the repository's intentionally public demo keystore. This allows reproducible sideload builds, but anyone can obtain that signing key. Do not use it for Google Play, sensitive production data, or trusted upgrade signing. See [`signing/README.md`](signing/README.md).

## Included in v1.1.1

- Dashboard-first manual logging: systolic, diastolic and optional pulse fields are ready as soon as the app opens; readings are encrypted and saved locally without a sync dependency.
- Automatic MAP calculation using (SBP + 2 × DBP) ÷ 3, shown with each reading; the readings section explains SBP, DBP, MAP and educational category bands.
- Encrypted local reading history and stage summaries; no cloud account or internet connection required.
- WHO/ISH-style educational bands using sage, amber and warning red. These are not diagnoses.
- Trends, local daily reminders, a compact Dashboard / Readings / Trends / Reminders / Settings menu, profile greeting, and three-way Light / Dark / System appearance controls.
- Accessible, full-screen teal menu based on the repository logo, day/night palettes, and the complete logo composition in the launcher icon.
- Pluggable `IReadingSync` boundary with a local-only demo implementation.

## Build

The Android project uses Gradle 9.8.2, Android Gradle Plugin 9.4.0 and Java 17. GitHub Actions uses the runner's preinstalled Gradle to avoid a first-build download timeout; a local Android SDK and internet access for Gradle dependencies are needed for a local build.

```bash
cd android
./gradlew testDebugUnitTest assembleRelease
```

The signed APK is written to `android/app/build/outputs/apk/release/app-release.apk`.

## GitHub Release

The repository workflow is `.github/workflows/android-release.yml`. It builds automatically when the working branch is pushed and also reacts to `v*` tags. GitHub only lists a **Run workflow** control when a `workflow_dispatch` workflow exists on the repository's default branch; this workflow intentionally stays on the development branch so the app branch is not merged. Use the branch build artifact and GitHub's release page instead:

1. If Actions are disabled, open [Repository Actions settings](https://github.com/SaintM254/AFYALINK-APP/settings/actions), allow GitHub Actions, and save.
2. A push to `arena/9067e9fc-afyalink-app` starts **Android release** automatically. Open [the Actions page](https://github.com/SaintM254/AFYALINK-APP/actions), wait for that run, and download `app-release.apk` and `SHA256SUMS.txt` from its **Artifacts** section.
3. Open [New release targeting the working branch](https://github.com/SaintM254/AFYALINK-APP/releases/new?target=arena%2F9067e9fc-afyalink-app). Enter tag `v1.1.1`, choose the working branch as the tag target if GitHub asks, set the title to **Afyalink v1.1.1**, attach the downloaded APK and checksum, and publish.

The tag build also attempts to attach its APK and checksum automatically. The branch remains unmerged; this release process does not require merging app code into `main`.

Full release and signing steps: [`docs/RELEASE-PROCESS.md`](docs/RELEASE-PROCESS.md).

## Repo map

```text
brand/                         Source logo SVG
android/app/src/main/java/     Domain, crypto, storage, monitor, sync, reminders and UI
android/app/src/main/res/      Palette, icon vectors, full-logo PNGs and Android resources
android/app/src/test/          JVM unit tests
signing/                       Public demo keystore and key-safety instructions
tools/                         SVG-to-vector and full-logo launcher asset build tools
docs/                          Release and health-data compliance notes
.github/workflows/             Android build/release automation
```

## Development notes

- The menu background uses the SVG's teal `#095D7E`; the attached example's green is intentionally not substituted.
- The monitor adapter supports the standard BLE Blood Pressure Service. A monitor using a proprietary protocol needs an explicitly selected adapter; supply the exact manufacturer/model to add one without guessing.
- Keep all changes on `arena/9067e9fc-afyalink-app` for this session. Do not merge it to `main` unless you explicitly change that instruction.
- Do not represent this demo as HIPAA-compliant or as a medical device. Review [`docs/PRIVACY-COMPLIANCE.md`](docs/PRIVACY-COMPLIANCE.md) before any real data flow.
