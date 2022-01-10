package com.example.phone_app.background;

import android.util.Log;

import com.example.phone_app.CiphertextWrapper;
import com.example.phone_app.JNIBridge;
import com.example.phone_app.background.serialization.ContactMessage;
import com.example.phone_app.background.serialization.NewDistanceMessage;
import com.example.phone_app.background.serialization.NewPartialMessage;
import com.example.phone_app.config.Config;
import com.example.phone_app.config.EncryptionType;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundDecryptor {
  private final static ExecutorService decryptorService = Executors.newFixedThreadPool(5);
  private final static JNIBridge bridge = new JNIBridge();

  public static void getDistances(String userId, char[] privateKey, boolean partial) {
    while (true) {
      List<NewDistanceMessage> givenCiphertexts = ConnectionService.getDistances(userId, partial);
      if (givenCiphertexts.size() == 0) {
        return;
      }
      Log.d(BackgroundDecryptor.class.getName(), "I have received " + givenCiphertexts.size() + " ciphertexts.");
      if (partial) {
        computePartial(givenCiphertexts, privateKey);
      } else {
        wasInContact(givenCiphertexts, privateKey, userId);
      }
    }
  }

  public static void computePartial(List<NewDistanceMessage> givenCiphertexts, char[] privateKey) {
    for (NewDistanceMessage distanceMessage : givenCiphertexts) {
      decryptorService.execute(() -> {
        Log.d(BackgroundDecryptor.class.getName(), "Start timestamp " + distanceMessage.getTimestamp());
        Log.d(BackgroundDecryptor.class.getName(), "End timestamp " + distanceMessage.getTimestampEnd());
        Log.d(BackgroundDecryptor.class.getName(), "Row id " + distanceMessage.getRowId());
        String partialDistanceString = ((CiphertextWrapper) bridge.decryptMulti(distanceMessage.getCiphertext().toCharArray(), "".toCharArray(), privateKey, false)).getLatitudeCos();
        String partialAltitudeDifference = ((CiphertextWrapper) bridge.decryptMulti(distanceMessage.getAltitudeDifference().toCharArray(), "".toCharArray(), privateKey, false)).getLatitudeCos();
        ConnectionService.sendObject(new NewPartialMessage(partialDistanceString, partialAltitudeDifference, distanceMessage.getRowId()), "new-partial-distance");
      });
    }
  }

  private static void wasInContact(List<NewDistanceMessage> givenCiphertexts, char[] privateKey, String userId) {
    for (NewDistanceMessage distanceMessage : givenCiphertexts) {
      decryptorService.execute(() -> {
        Log.d(BackgroundDecryptor.class.getName(), "Start timestamp " + distanceMessage.getTimestamp());
        Log.d(BackgroundDecryptor.class.getName(), "End timestamp " + distanceMessage.getTimestampEnd());
        Log.d(BackgroundDecryptor.class.getName(), "Contact user id " + distanceMessage.getContactUserId());
        Log.d(BackgroundDecryptor.class.getName(), "Current ciphertext size is " + distanceMessage.getCiphertext().length());

        double initialResult;
        double differenceInAltitude;

        if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK) {
          initialResult = (Double) bridge.decryptMulti(distanceMessage.getCiphertext().toCharArray(), distanceMessage.getPartialDistance().toCharArray(),
                  privateKey, true);
          differenceInAltitude = (Double) bridge.decryptMulti(distanceMessage.getAltitudeDifference().toCharArray(), distanceMessage.getPartialAltitudeDifference().toCharArray(),
                  privateKey, true);
        } else {
          initialResult = bridge.decrypt(distanceMessage.getCiphertext().toCharArray(), privateKey);
          differenceInAltitude = bridge.decrypt(distanceMessage.getAltitudeDifference().toCharArray(), privateKey);
        }
        Log.d(BackgroundDecryptor.class.getName(), "Altitude difference was " + differenceInAltitude);
        if (initialResult < 0) {
          Log.d(BackgroundDecryptor.class.getName(), "The location was very close");
        }
        initialResult = Math.asin(Math.sqrt(initialResult / 2.0)) * 6378.8 * 2.0 * 1000;
        Log.d(BackgroundDecryptor.class.getName(), "The distance was " + initialResult + " meters.");
        if ((initialResult <= 6 || Double.isNaN(initialResult)) && differenceInAltitude * 100 < 210) {
          Log.d(BackgroundDecryptor.class.getName(), "Found contact " + distanceMessage.getContactUserId());
          ConnectionService.sendObject(new ContactMessage(userId, distanceMessage.getContactUserId(),
                  distanceMessage.getTimestamp(), distanceMessage.getTimestampEnd()), "new-contact");
        }
      });
    }
  }
}
