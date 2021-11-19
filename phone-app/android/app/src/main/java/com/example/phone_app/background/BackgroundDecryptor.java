package com.example.phone_app.background;

import android.util.Log;

import com.example.phone_app.JNIBridge;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundDecryptor {
  private final static ExecutorService decryptorService = Executors.newFixedThreadPool(5);
  private final static JNIBridge bridge = new JNIBridge();

  public static void getDistances(String userId, char[] privateKey) {
    while (true) {
      List<String> givenCiphertexts = ConnectionService.getDistances(userId);
      if (givenCiphertexts.size() == 0) {
        return;
      }
      Log.i(BackgroundDecryptor.class.getName(), "I have received " + givenCiphertexts.size() + " ciphertexts.");
      wasInContact(givenCiphertexts, privateKey);
    }
  }

  private static void wasInContact(List<String> givenCiphertexts, char[] privateKey) {
    for (String cipherText : givenCiphertexts) {
      Log.i(BackgroundDecryptor.class.getName(), "Current ciphertext size is " + cipherText.length());
      decryptorService.execute(() -> {
        double initialResult = bridge.decrypt(cipherText.toCharArray(), privateKey);
        if (initialResult < 0) {
          Log.i(BackgroundDecryptor.class.getName(), "The location was very close");
        }
        initialResult = Math.asin(Math.sqrt(initialResult / 2.0)) * 6378.8 * 2.0;
        Log.i(BackgroundDecryptor.class.getName(), "The distance was " + initialResult + " km.");
      });
    }
  }
}
