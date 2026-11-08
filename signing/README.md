# Signing keys

## Repository demo key — do not trust for production

`afyalink-demo.jks` and `keystore.properties` are committed intentionally so the public demo workflow can build an installable APK without a secret-management prerequisite. The keystore is PKCS#12, alias `afyalink`, protected by the demo password shown in `keystore.properties`. Since the repository is public, the key is publicly compromised by design.

Use the resulting APK only for a disposable sideload demonstration. **Do not upload it to Google Play, use it for a customer release, or rely on it to authenticate future production updates.** Anyone with the file and password can sign another APK with the same certificate. Keep production signing separate.

## Before a production/store release

1. On a trusted offline machine create a new private keystore and record its password in an approved password manager. Do not put the key or passwords in Git, chat, issue trackers or build logs.
2. Back up the keystore securely in at least two controlled locations. Losing the key can prevent updates to existing installs.
3. In the repository's [Actions secrets settings](https://github.com/SaintM254/AFYALINK-APP/settings/secrets/actions), create repository secrets named `AFYALINK_KEYSTORE_FILE`, `AFYALINK_KEYSTORE_PASSWORD`, `AFYALINK_KEY_ALIAS`, and `AFYALINK_KEY_PASSWORD`.
4. Provide the keystore as a protected CI file (for example, store a base64-encoded file in an Actions secret and decode it to the runner temp directory in the workflow). Update the release workflow to decode it and set `AFYALINK_KEYSTORE_FILE` to that temporary path. Never print the secret or key contents.
5. Remove the demo signing configuration and public demo keystore from the production release path, then verify the release certificate fingerprint independently before publishing.
6. If a demo APK has already been installed, a production APK signed by a different key will not update it in place. Uninstall the demo first (which deletes its local vault) or use a separate application ID for the production app.

The build accepts the four environment variables above. They override `signing/keystore.properties` when all the CI variables are present. This repository does **not** currently decode or consume a production keystore secret automatically; adding that flow is a deliberate pre-production task.

## Key fingerprints

The demo signing certificate SHA-256 fingerprint is `C3:36:AE:15:C5:D2:2C:0D:BC:86:74:2F:F4:1A:7B:99:34:02:4F:E8:45:8E:B0:25:63:86:E1:45:A5:04:C0:08`. Compare it with the actual APK signer before use. It is not an assurance that the APK is safe or official; the private key is public.

For a local fingerprint check:

```bash
keytool -list -v -keystore signing/afyalink-demo.jks -alias afyalink
```
