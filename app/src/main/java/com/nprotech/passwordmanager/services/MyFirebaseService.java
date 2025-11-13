package com.nprotech.passwordmanager.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nprotech.passwordmanager.utils.AppLogger;

public class MyFirebaseService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle incoming FCM messages here
        AppLogger.d(getClass(), "onMessageReceived: " + remoteMessage.getData());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        AppLogger.d(getClass(), "onNewToken: " + token);

        // Send this token to your server if needed
    }
}