package dissertation.backend.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
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
        .setToken(token)
        .build();
    try {
      FirebaseMessaging.getInstance().send(fcmMessage);
    } catch (FirebaseMessagingException e) {
      logger.error(e.getMessage());
    }
  }

  public static void sendNotifications() {
    for (String token : Controller.getUsersThatNeedToDownloadDistances()) {
      sendNotification("new data", token);
    }
  }
}
