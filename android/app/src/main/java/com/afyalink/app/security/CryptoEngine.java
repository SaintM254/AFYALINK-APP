package com.afyalink.app.security;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** AES-256-GCM authenticated encryption plus RSA-OAEP envelope wrapping. */
public final class CryptoEngine {
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final String AES_TRANSFORM = "AES/GCM/NoPadding";
    private static final String RSA_TRANSFORM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoEngine() {}

    public static byte[] encryptLocal(byte[] plaintext, byte[] associatedData) throws Exception {
        SecretKey key = VaultKeyManager.localEncryptionKey();
        byte[] iv = new byte[GCM_IV_BYTES];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        if (associatedData != null) cipher.updateAAD(associatedData);
        byte[] ciphertext = cipher.doFinal(plaintext);
        byte[] envelope = new byte[1 + iv.length + ciphertext.length];
        envelope[0] = 1; // envelope format version
        System.arraycopy(iv, 0, envelope, 1, iv.length);
        System.arraycopy(ciphertext, 0, envelope, 1 + iv.length, ciphertext.length);
        Arrays.fill(plaintext, (byte) 0);
        return envelope;
    }

    public static byte[] decryptLocal(byte[] envelope, byte[] associatedData) throws Exception {
        if (envelope == null || envelope.length < 1 + GCM_IV_BYTES + 16 || envelope[0] != 1) {
            throw new SecurityException("Unsupported or truncated encrypted record");
        }
        byte[] iv = Arrays.copyOfRange(envelope, 1, 1 + GCM_IV_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(envelope, 1 + GCM_IV_BYTES, envelope.length);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, VaultKeyManager.localEncryptionKey(),
                new GCMParameterSpec(GCM_TAG_BITS, iv));
        if (associatedData != null) cipher.updateAAD(associatedData);
        return cipher.doFinal(ciphertext);
    }

    /**
     * Creates a demo E2E envelope: random per-payload AES-256 key encrypted
     * with a recipient RSA public key, and payload sealed by AES-GCM. AAD is
     * authenticated in both the payload and the caller's record metadata.
     */
    public static Envelope encryptForRecipient(byte[] plaintext, byte[] associatedData,
                                                java.security.PublicKey recipient) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256, RANDOM);
        SecretKey sessionKey = generator.generateKey();
        byte[] iv = new byte[GCM_IV_BYTES];
        RANDOM.nextBytes(iv);
        Cipher aes = Cipher.getInstance(AES_TRANSFORM);
        aes.init(Cipher.ENCRYPT_MODE, sessionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
        if (associatedData != null) aes.updateAAD(associatedData);
        byte[] ciphertext = aes.doFinal(plaintext);
        Cipher rsa = Cipher.getInstance(RSA_TRANSFORM);
        rsa.init(Cipher.ENCRYPT_MODE, recipient);
        byte[] wrappedKey = rsa.doFinal(sessionKey.getEncoded());
        Arrays.fill(plaintext, (byte) 0);
        return new Envelope(wrappedKey, iv, ciphertext);
    }

    /** For local demo verification only. A production relay must hold the private key server-side. */
    public static byte[] decryptDemoEnvelope(Envelope envelope, KeyPair localDemoPair,
                                              byte[] associatedData) throws Exception {
        Cipher rsa = Cipher.getInstance(RSA_TRANSFORM);
        rsa.init(Cipher.DECRYPT_MODE, localDemoPair.getPrivate());
        byte[] rawKey = rsa.doFinal(envelope.wrappedKey);
        SecretKey key = new javax.crypto.spec.SecretKeySpec(rawKey, "AES");
        Cipher aes = Cipher.getInstance(AES_TRANSFORM);
        aes.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, envelope.iv));
        if (associatedData != null) aes.updateAAD(associatedData);
        return aes.doFinal(envelope.ciphertext);
    }

    public static String base64(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public static byte[] fromBase64(String text) {
        return Base64.decode(text, Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    public static final class Envelope {
        public final byte[] wrappedKey;
        public final byte[] iv;
        public final byte[] ciphertext;

        public Envelope(byte[] wrappedKey, byte[] iv, byte[] ciphertext) {
            this.wrappedKey = wrappedKey.clone();
            this.iv = iv.clone();
            this.ciphertext = ciphertext.clone();
        }

        public String wrappedKeyBase64() { return base64(wrappedKey); }
        public String ivBase64() { return base64(iv); }
        public String ciphertextBase64() { return base64(ciphertext); }
    }
}
