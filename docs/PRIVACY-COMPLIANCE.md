# Health-data privacy and compliance boundary

**This is an engineering demo, not a compliance certification or legal opinion.** Do not collect, upload, or use real patient data with the demo relay or demo signing key. HIPAA applies to covered entities and business associates in the circumstances defined by US law; other regimes may apply based on the people, organizations, locations, purposes and vendors involved. Have qualified privacy counsel and security assess the exact deployment before processing real data.

## What v1 does today

- The app has no Android `INTERNET` permission and includes no server URL, analytics SDK or cloud SDK.
- A measurement is serialized in the app and sealed as a complete vault with AES-256-GCM using a key held by Android Keystore. GCM authenticates the ciphertext; file metadata does not reveal the measurements, dates, notes, sync flags or source.
- The encrypted vault is written under `noBackupFilesDir`; Android cloud backup and device-transfer backup are disabled for app data. Losing the device or uninstalling the app can mean losing the readings.
- BLE scanning is performed locally for the Bluetooth SIG Blood Pressure Service. The app does not request location on Android 12+ and does not store a cuff address. Android versions through 11 require the location permission for BLE scanning; the app uses it only to scan and does not access or retain location.
- The demo relay generates a fresh AES-256 session key, encrypts a batch with AES-GCM, and wraps the key using a device-local RSA-3072 OAEP key. It decrypts and verifies the batch locally, discards the payload and marks the demo queue as locally verified. It does **not** send data to a remote system. The local RSA demo key is not a production recipient key.
- Optional reminders run on-device and their notification text contains no reading values. No readings are stored in SharedPreferences.

## Before implementing real transmission

A remote implementation must not be enabled merely by adding a URL. Treat this checklist as a starting point for a qualified review, not as a complete or sufficient compliance program:

1. **Determine applicable laws and roles** with counsel. Evaluate HIPAA scope and required agreements where applicable, Kenya's Data Protection Act and regulations, GDPR or other laws for relevant users, and any medical-device rules arising from intended use or claims.
2. **Complete a documented risk assessment** and define controller/processor, covered-entity/business-associate, and subprocessor responsibilities as applicable. Agree on purpose, lawful basis/authorizations, retention, deletion, access rights, breach response and cross-border transfers.
3. **Choose and review hosting and vendors.** Sign required contracts (including a BAA where HIPAA requires one) before a vendor handles regulated data. Verify residency, subprocessor lists, backups, incident notification, support access and deletion commitments.
4. **Design a production key protocol.** Provision an authenticated, pinned recipient public key; validate key rotation and revocation; protect server private keys in managed HSM/KMS; define per-record or per-batch envelopes, forward secrecy requirements, replay protection, nonce uniqueness, signatures where needed, and recovery/deletion behavior. The demo's device-local RSA key is not suitable for remote use.
5. **Harden transport and service.** Use TLS 1.2+ (prefer TLS 1.3), certificate validation/pinning strategy, authenticated users/devices, least-privilege authorization, rate limits, idempotent sync, replay protection, secure error handling and no sensitive data in logs, crash reports, URLs or push payloads.
6. **Add operational controls.** Threat model and penetration test the client and service; audit privileged access; monitor without exposing health values; define incident/breach procedures, backups, retention/deletion, disaster recovery, vulnerability response and access reviews.
7. **Review consent and UX.** Explain exactly what leaves the phone, recipient, purpose, retention, sharing and revocation before enabling sync. Make offline queueing and local deletion behavior clear.
8. **Reassess app-store and clinical claims.** Validate published thresholds and all emergency messaging with a qualified clinician. Do not imply diagnosis, treatment or regulatory clearance without evidence and authorization.

## Current implementation boundary

`IReadingSync` is the transport seam. `DemoRelay` is the only wired implementation; it is deliberately local. A future HTTPS implementation should be a separate reviewed class, accompanied by explicit consent, a privacy notice, security tests, server documentation, deployment secrets, and an owner decision to add network permission. Until that review is complete, keep the app offline-only.

The code's AES-GCM/RSA primitives are educational implementation choices, not proof of end-to-end security. Production systems require expert cryptographic review, threat modeling, and tests on supported Android versions and hardware.
