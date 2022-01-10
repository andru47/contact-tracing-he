package com.example.phone_app.background;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.phone_app.MainActivity;
import com.example.phone_app.R;
import com.example.phone_app.background.serialization.NewTokenMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import io.flutter.Log;

public class NotificationService extends FirebaseMessagingService {
  public NotificationService() {
  }

  @Override
  public void onNewToken(@NonNull String newToken) {
    super.onNewToken(newToken);
    Log.d(NotificationService.class.toString(), "Received token " + newToken);
    ConnectionService.sendObject(new NewTokenMessage(Util.getUuid(this), newToken), "upload-fcm-token");
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    Log.d(NotificationService.class.toString(), "Received message " + remoteMessage.getData().toString());
    if (!remoteMessage.getData().containsKey("he-server-message")) {
      return;
    }
    if (remoteMessage.getData().get("he-server-message").equals("new data")) {
      BackgroundDecryptor.getDistances(Util.getUuid(this), Util.getPrivateKey(this), false);
    } else if (remoteMessage.getData().get("he-server-message").equals("new partial data")) {
      BackgroundDecryptor.getDistances(Util.getUuid(this), Util.getPrivateKey(this), true);
    } else {
      long unixSeconds = Long.parseLong(remoteMessage.getData().get("he-server-message"));
      showContactNotification(unixSeconds);
      Util.setIsolationStatus(this, unixSeconds);
    }
  }

  private void showContactNotification(long unixSeconds) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "HE-CONTACT-TRACING")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("You need to isolate!")
            .setContentText("You have been in contact and need to isolate until " + getCurrentDateString(unixSeconds))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

    notificationManager.notify((int) (unixSeconds / 1000), builder.build());
  }

  private String getCurrentDateString(long unixSeconds) {
    return new Date(unixSeconds * 1000).toString();
  }
}
