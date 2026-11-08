package com.afyalink.app.security;

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
