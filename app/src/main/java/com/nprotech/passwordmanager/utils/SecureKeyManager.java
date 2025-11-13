package com.nprotech.passwordmanager.utils;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecureKeyManager {

    private static final String KEY_ALIAS = "nprotech_db_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String FILE_NAME = "db_key_secure.bin";

    public static byte[] getDatabasePassphrase(Context context) throws Exception {
        File keyFile = new File(context.getFilesDir(), FILE_NAME);
        if (keyFile.exists()) {
            return decryptKey(keyFile);
        } else {
            byte[] randomKey = generateRandomKey();
            encryptAndStoreKey(randomKey, keyFile);
            return randomKey;
        }
    }

    private static byte[] generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        return key;
    }

    private static SecretKey getOrCreateSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build());
            return keyGenerator.generateKey();
        } else {
            return ((KeyStore.SecretKeyEntry)
                    keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }
    }

    private static void encryptAndStoreKey(byte[] plainKey, File outFile) throws Exception {
        SecretKey secretKey = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(plainKey);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(iv.length);
            fos.write(iv);
            fos.write(encrypted);
        }
    }

    private static byte[] decryptKey(File inFile) throws Exception {
        SecretKey secretKey = getOrCreateSecretKey();

        try (FileInputStream fis = new FileInputStream(inFile)) {
            int ivLen = fis.read();
            if (ivLen <= 0 || ivLen > 32) { // sanity check
                throw new IllegalStateException("Invalid IV length");
            }

            byte[] iv = new byte[ivLen];
            int bytesRead = fis.read(iv);
            if (bytesRead != ivLen) {
                throw new IllegalStateException("Failed to read full IV bytes");
            }

            // ✅ Replace readAllBytes() with manual read (works on all Android versions)
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] temp = new byte[4096];
            int read;
            while ((read = fis.read(temp)) != -1) {
                buffer.write(temp, 0, read);
            }
            byte[] encrypted = buffer.toByteArray();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return cipher.doFinal(encrypted);
        }
    }
}