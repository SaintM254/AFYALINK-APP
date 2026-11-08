package com.afyalink.app.security;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/** Optional app-entry biometric check; device credentials remain managed by Android. */
public final class SecurityGate {
    private SecurityGate() {}
    public static boolean available(FragmentActivity activity) {
        int result = BiometricManager.from(activity).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }
    public static void authenticate(FragmentActivity activity, Runnable onSuccess, Runnable onCancel) {
        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt prompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) { onSuccess.run(); }
            @Override public void onAuthenticationError(int code, CharSequence message) { onCancel.run(); }
        });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock AfyaLink")
                .setSubtitle("Confirm it is you to open your encrypted readings")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        prompt.authenticate(info);
    }
}
