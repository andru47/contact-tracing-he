package com.example.phone_app.background;

import android.util.Log;

import com.example.phone_app.JNIBridge;
import com.example.phone_app.background.serialization.ContactMessage;
import com.example.phone_app.background.serialization.NewDistanceMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundDecryptor {
  private final static ExecutorService decryptorService = Executors.newFixedThreadPool(5);
  private final static JNIBridge bridge = new JNIBridge();

  public static void getDistances(String userId, char[] privateKey) {
    while (true) {
      List<NewDistanceMessage> givenCiphertexts = ConnectionService.getDistances(userId);
      if (givenCiphertexts.size() == 0) {
        return;
      }
      Log.d(BackgroundDecryptor.class.getName(), "I have received " + givenCiphertexts.size() + " ciphertexts.");
      wasInContact(givenCiphertexts, privateKey, userId);
    }
  }

  private static void wasInContact(List<NewDistanceMessage> givenCiphertexts, char[] privateKey, String userId) {
    for (NewDistanceMessage distanceMessage : givenCiphertexts) {
      decryptorService.execute(() -> {
        Log.d(BackgroundDecryptor.class.getName(), "Start timestamp " + distanceMessage.getTimestamp());
        Log.d(BackgroundDecryptor.class.getName(), "End timestamp " + distanceMessage.getTimestampEnd());
        Log.d(BackgroundDecryptor.class.getName(), "Contact user id " + distanceMessage.getContactUserId());
        Log.d(BackgroundDecryptor.class.getName(), "Current ciphertext size is " + distanceMessage.getCiphertext().length());

        double initialResult = bridge.decrypt(distanceMessage.getCiphertext().toCharArray(), privateKey);
        if (initialResult < 0) {
          Log.d(BackgroundDecryptor.class.getName(), "The location was very close");
        }
        initialResult = Math.asin(Math.sqrt(initialResult / 2.0)) * 6378.8 * 2.0 * 1000;
        Log.d(BackgroundDecryptor.class.getName(), "The distance was " + initialResult + " meters.");
        if (initialResult <= 6 || Double.isNaN(initialResult)) {
          Log.d(BackgroundDecryptor.class.getName(), "Found contact " + distanceMessage.getContactUserId());
          ConnectionService.sendObject(new ContactMessage(userId, distanceMessage.getContactUserId(),
                  distanceMessage.getTimestamp(), distanceMessage.getTimestampEnd()), "new-contact");
        }
      });
    }
  }
}
