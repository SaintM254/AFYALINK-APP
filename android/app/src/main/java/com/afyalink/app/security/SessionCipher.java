package com.afyalink.app.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

/** Explicit wrapper for the demo RSA/AES session-envelope flow. */
public final class SessionCipher {
    private SessionCipher() {}

    public static CryptoEngine.Envelope seal(byte[] payload, String recordContext,
                                              java.security.PublicKey recipient) throws Exception {
        return CryptoEngine.encryptForRecipient(payload,
                recordContext.getBytes(StandardCharsets.UTF_8), recipient);
    }

    public static byte[] openForDemo(CryptoEngine.Envelope envelope, String recordContext,
                                     KeyPair localDemoKeyPair) throws Exception {
        return CryptoEngine.decryptDemoEnvelope(envelope, localDemoKeyPair,
                recordContext.getBytes(StandardCharsets.UTF_8));
    }
}
