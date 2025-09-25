package com.nprotech.passwordmanager.utils;

import java.security.SecureRandom;
import android.util.Base64;

public class CommonUtils {

    public static String generateNonce(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] nonceBytes = new byte[length];
        secureRandom.nextBytes(nonceBytes);

        // Use android.util.Base64 to support all SDK versions
        return Base64.encodeToString(nonceBytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
}