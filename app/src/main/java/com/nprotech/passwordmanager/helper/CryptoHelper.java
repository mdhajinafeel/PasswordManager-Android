package com.nprotech.passwordmanager.helper;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * CryptoHelper
 * Handles encryption/decryption using Android Keystore with alias-based keys
 */
public class CryptoHelper {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Creates a key if not already created for the given alias
     */
    public static void createKeyIfNeeded(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);

        // If key doesn't exist, generate a new one
        if (!keyStore.containsAlias(alias)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEY_STORE
            );

            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    // Optional security hardening:
                    // .setUserAuthenticationRequired(true)
                    // .setUserAuthenticationValidityDurationSeconds(60)
                    .build();

            keyGenerator.init(spec);
            keyGenerator.generateKey();
        }
    }

    /**
     * Encrypts the given plain text using the key associated with the alias
     */
    public static String encrypt(String alias, String plainText) throws Exception {
        createKeyIfNeeded(alias); // Ensure key exists

        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        SecretKey secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null)).getSecretKey();

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV(); // Initialization Vector (12 bytes for GCM)
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combine IV + Encrypted content → Base64 encode
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    /**
     * Decrypts text using the alias key
     */
    public static String decrypt(String alias, String encryptedText) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        SecretKey secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null)).getSecretKey();

        byte[] decoded = Base64.decode(encryptedText, Base64.NO_WRAP);

        // Extract IV and ciphertext
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] cipherBytes = new byte[decoded.length - GCM_IV_LENGTH];
        System.arraycopy(decoded, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(decoded, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(cipherBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}