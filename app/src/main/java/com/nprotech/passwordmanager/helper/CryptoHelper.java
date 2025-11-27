package com.nprotech.passwordmanager.helper;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public static SecretKey secretKey() {
        byte[] keyBytes =
                Base64.decode(PreferenceManager.INSTANCE.getSecretKey(), Base64.NO_WRAP);

        // Fix: support AES-256
        if (keyBytes.length != 16 && keyBytes.length != 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32); // Trim or pad to 32 bytes
        }

        return new SecretKeySpec(keyBytes, "AES");
    }

    // ---- ENCRYPT ----
    public static String encrypt(String plain) throws Exception {

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey());

        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    // ---- DECRYPT (Safe version) ----
    public static String decrypt(String encryptedBase64) throws Exception {

        byte[] decoded = Base64.decode(encryptedBase64, Base64.NO_WRAP);

        byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
        byte[] cipherBytes = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
    }
}
