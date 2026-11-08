# Release process

## Current release

The intended first release is **Afyalink v1.0.0**, tag `v1.0.0`. The workflow builds `assembleRelease`, runs the release unit tests, calculates SHA-256, uploads an Actions artifact, and creates or updates a GitHub Release with the APK and checksum attached.

## One-time human setup

GitHub Actions is not currently enabled for this repository. The agent's GitHub connection cannot change the repository-level Actions permission, so an administrator must enable it:

1. Open <https://github.com/SaintM254/AFYALINK-APP/settings/actions>.
2. Under **Actions permissions**, select **Allow all actions and reusable workflows**, or a policy that permits GitHub's `actions/checkout`, `actions/setup-java`, and `actions/upload-artifact` actions.
3. Click **Save** if GitHub shows a save button.
4. Open <https://github.com/SaintM254/AFYALINK-APP/actions>.
5. In the workflow list select **Android release**, choose **Run workflow**, select branch `arena/9067e9fc-afyalink-app`, keep **Create or update GitHub Release** enabled, then confirm **Run workflow**.
6. Wait for the run to finish. Check both the workflow's **Artifacts** section and <https://github.com/SaintM254/AFYALINK-APP/releases> for `Afyalink v1.0.0`.

No assumption that this setup is already done: if the Actions permission or workflow controls do not appear, the repository administrator must resolve that GitHub setting before a release can run.

The workflow can also run on a pushed `v*` tag. A manual run from a branch builds version 1.0.0 and targets that run's commit when creating the tag.

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
./gradlew testReleaseUnitTest assembleRelease
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
