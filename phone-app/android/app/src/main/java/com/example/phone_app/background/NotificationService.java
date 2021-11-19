package com.example.phone_app.background;

import androidx.annotation.NonNull;

import com.example.phone_app.background.serialization.NewTokenMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.flutter.Log;

public class NotificationService extends FirebaseMessagingService {
  public NotificationService() {
  }

  @Override
  public void onNewToken(@NonNull String newToken) {
    super.onNewToken(newToken);
    Log.i(NotificationService.class.toString(), "Received token " + newToken);
    ConnectionService.sendObject(new NewTokenMessage(Util.getUuid(this), newToken), "upload-fcm-token");
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    Log.i(NotificationService.class.toString(), "Received message " + remoteMessage.getData().toString());
    if (!remoteMessage.getData().containsKey("he-server-message")) {
      return;
    }
    BackgroundDecryptor.getDistances(Util.getUuid(this), Util.getPrivateKey(this));
  }
}
