package dissertation.backend.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import dissertation.backend.database.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FCMNotificationManager {
  private final static Logger logger = LogManager.getLogger(FCMNotificationManager.class);

  static {
    try {
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.getApplicationDefault())
          .setProjectId("dissertation-31cc0")
          .build();
      FirebaseApp.initializeApp(options);
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

  public static void sendNotification(String message, String token) {
    Message fcmMessage = Message.builder()
        .putData("he-server-message", message)
        .setApnsConfig(ApnsConfig.builder().setAps(Aps.builder().setContentAvailable(true).build()).build())
        .setToken(token)
        .build();
    try {
      FirebaseMessaging.getInstance().send(fcmMessage);
    } catch (FirebaseMessagingException e) {
      logger.error(e.getMessage());
    }
  }

  public static void sendContactNotification(String userId, String endIsolation) {
    String token = Controller.getTokenForUser(userId);
    if (token == null) {
      logger.error("Could not find token for user " + userId);
    }
    sendNotification(endIsolation, token);
  }

  public static void sendDataNotifications() {
    for (String token : Controller.getInfectedUsersThatNeedToHalfDecrypt()) {
      sendNotification("new partial data", token);
    }

    for (String token : Controller.getUsersThatNeedToDownloadDistances()) {
      sendNotification("new data", token);
    }
  }
}
