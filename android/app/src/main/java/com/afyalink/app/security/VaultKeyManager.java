package com.afyalink.app.security;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/** Android Keystore-backed at-rest key material; private keys are non-exportable. */
public final class VaultKeyManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String AES_ALIAS_V1 = "afyalink.local.aes.v1";
    private static final String AES_ALIAS_V2 = "afyalink.local.aes.v2";
    private static final String RSA_ALIAS = "afyalink.demo.rsa.exchange.v1";

    private VaultKeyManager() {}

    /** Current key alias: no explicit unlocked-device constraint, improving OEM compatibility. */
    public static SecretKey localEncryptionKey() throws Exception {
        return getOrCreateAesKey(AES_ALIAS_V2, false);
    }

    /** Read compatibility for v1 vault files created with the original key policy. */
    public static SecretKey legacyLocalEncryptionKey() throws Exception {
        return getOrCreateAesKey(AES_ALIAS_V1, true);
    }

    /**
     * Last-resort, app-sandbox AES key for devices whose Android Keystore AES
     * provider cannot encrypt the vault. The raw key remains in no-backup,
     * app-private storage; the reading payload itself is still AES-GCM sealed.
     */
    public static synchronized SecretKey softwareFallbackEncryptionKey(Context context) throws Exception {
        java.io.File dir = new java.io.File(context.getNoBackupFilesDir(), "vault");
        if (!dir.exists() && !dir.mkdirs()) throw new java.io.IOException("Cannot create private vault directory");
        android.util.AtomicFile keyFile = new android.util.AtomicFile(new java.io.File(dir, "fallback-key.bin"));
        byte[] raw;
        if (keyFile.getBaseFile().exists()) {
            try (java.io.FileInputStream in = keyFile.openRead();
                 java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[128];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                raw = out.toByteArray();
            }
            if (raw.length != 32) {
                Arrays.fill(raw, (byte) 0);
                throw new java.security.GeneralSecurityException("Fallback vault key is invalid");
            }
        } else {
            raw = new byte[32];
            new java.security.SecureRandom().nextBytes(raw);
            java.io.FileOutputStream out = null;
            try {
                out = keyFile.startWrite();
                out.write(raw);
                out.flush();
                out.getFD().sync();
                keyFile.finishWrite(out);
            } catch (Exception e) {
                if (out != null) keyFile.failWrite(out);
                Arrays.fill(raw, (byte) 0);
                throw e;
            }
        }
        try {
            return new javax.crypto.spec.SecretKeySpec(raw, "AES");
        } finally {
            Arrays.fill(raw, (byte) 0);
        }
    }

    private static SecretKey getOrCreateAesKey(String alias, boolean requireUnlocked) throws Exception {
        KeyStore store = KeyStore.getInstance(ANDROID_KEYSTORE);
        store.load(null);
        if (store.containsAlias(alias)) {
            KeyStore.Entry entry = store.getEntry(alias, null);
            if (entry instanceof KeyStore.SecretKeyEntry) {
                return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
            }
            throw new java.security.KeyStoreException("The saved local encryption key is unavailable");
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true);
        if (requireUnlocked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setUnlockedDeviceRequired(true);
        }
        generator.init(builder.build());
        return generator.generateKey();
    }

    /**
     * Demo recipient key pair for envelope-encryption exercises. Replace the
     * public key with a separately provisioned, audited service public key
     * before any real transmission. The private key never leaves this device.
     */
    public static KeyPair demoExchangeKeyPair() throws Exception {
        KeyStore store = KeyStore.getInstance(ANDROID_KEYSTORE);
        store.load(null);
        if (store.containsAlias(RSA_ALIAS)) {
            java.security.cert.Certificate cert = store.getCertificate(RSA_ALIAS);
            java.security.PrivateKey privateKey = (java.security.PrivateKey) store.getKey(RSA_ALIAS, null);
            return new KeyPair(cert.getPublicKey(), privateKey);
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(RSA_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(3072)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP);
        generator.initialize(builder.build());
        return generator.generateKeyPair();
    }

    public static void deleteAll() throws Exception {
        KeyStore store = KeyStore.getInstance(ANDROID_KEYSTORE);
        store.load(null);
        for (String alias : Arrays.asList(AES_ALIAS_V1, AES_ALIAS_V2, RSA_ALIAS)) {
            if (store.containsAlias(alias)) store.deleteEntry(alias);
        }
    }
}
