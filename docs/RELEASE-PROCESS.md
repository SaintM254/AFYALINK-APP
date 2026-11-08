# Release process

## Current release

The intended first release is **Afyalink v1.0.0**, tag `v1.0.0`. The workflow uses the Ubuntu 26.04 runner's preinstalled Gradle 9.8.2 with Android Gradle Plugin 9.4.0, runs the debug-variant JVM unit tests and builds `assembleRelease`, calculates SHA-256, uploads an Actions artifact, and creates or updates a GitHub Release with the APK and checksum attached.

## Why the workflow may not appear in GitHub's workflow list

GitHub only exposes the **Run workflow** button for `workflow_dispatch` workflows whose YAML exists on the repository's default branch. This release workflow intentionally lives on `arena/9067e9fc-afyalink-app`, not `main`, to honor the no-merge requirement. Therefore the empty **Get started with GitHub Actions** screen is expected before a branch push run; it does not mean the source branch is missing.

## Build and publish without merging

1. If GitHub Actions are disabled for this repository, an administrator must open <https://github.com/SaintM254/AFYALINK-APP/settings/actions>, allow GitHub Actions (or allow the GitHub-published `actions/checkout`, `actions/setup-java`, and `actions/upload-artifact` actions), and save.
2. A push to `arena/9067e9fc-afyalink-app` automatically starts the **Android release** build. Open <https://github.com/SaintM254/AFYALINK-APP/actions>, open the latest run, and wait for it to complete. Download `app-release.apk` and `SHA256SUMS.txt` from the run's **Artifacts** section.
3. Open <https://github.com/SaintM254/AFYALINK-APP/releases/new?target=arena%2F9067e9fc-afyalink-app>. Enter tag `v1.0.0`, choose the working branch as the target if asked, set the title to **Afyalink v1.0.0**, attach both downloaded files, and publish the release.
4. The workflow also listens for `v*` tags and attempts to upload the build APK and checksum to the matching release. If the release page already shows both assets, no duplicate manual upload is needed; otherwise attach the run artifact from step 2.

This does not merge the application branch into `main`. If the Actions permission is off, the run will not start until an administrator changes that setting. The workflow does not offer a manual **Run workflow** button while it remains branch-only.

## What the workflow signs

`android/app/build.gradle` reads the demo key in `signing/keystore.properties`, unless all of these environment variables are supplied by CI secrets:

- `AFYALINK_KEYSTORE_FILE`
- `AFYALINK_KEYSTORE_PASSWORD`
- `AFYALINK_KEY_ALIAS`
- `AFYALINK_KEY_PASSWORD`

The public repository key is for sideloaded demonstration APKs only. It is compromised by design because its private keystore and password are visible in Git. **Never use it for Play Store signing or a trusted production update path.** Follow [`../signing/README.md`](../signing/README.md) to create and store an owner-controlled key before that work.

## Local build

Use Java 17 and an Android SDK with API 35 installed:

```bash
cd android
./gradlew testDebugUnitTest assembleRelease
```

Output: `android/app/build/outputs/apk/release/app-release.apk`.

## Verify the released APK

Download both files from the release. On Linux/macOS:

```bash
sha256sum -c SHA256SUMS.txt
```

On Windows PowerShell:

```powershell
(Get-FileHash .\app-release.apk -Algorithm SHA256).Hash.ToLower()
```

Compare the result with `SHA256SUMS.txt` and the release notes. Confirm the version in Android app information after installing.

## Branch policy

All work for this session stays on `arena/9067e9fc-afyalink-app`. The branch is not to be merged into `main`; release automation can target this branch directly.
