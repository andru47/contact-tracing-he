package com.example.phone_app.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Util {
  protected final static String SHARED_PREFERENCES_FILENAME = "com.example.phone_app.PRIVATE_PREFS";
  private final static String SHARED_PREFERENCES_UID_KEY = "uuid";
  private final static String SHARED_PREFERENCES_ISO_KEY = "isolation";
  private final static String SHARED_PREFERENCES_ISO_END_KEY = "isolation-end";
  private static String uuid = null;
  private static char[] privateKey = null;
  private static char[] publicKey = null;
  private static Boolean isIsolating = null;

  protected static synchronized String getUuid(Context givenContext) {
    if (uuid == null) {
      uuid = getOrUpdateSharedPrefIfNotPresent(givenContext);
    }

    return uuid;
  }

  public static char[] getPublicKey(Context givenContext) {
    if (publicKey == null) {
      publicKey = readKey("pubKey.bin", givenContext);
    }

    return publicKey;
  }

  public static char[] getPrivateKey(Context givenContext) {
    if (privateKey == null) {
      privateKey = readKey("privateKey.bin", givenContext);
    }

    return privateKey;
  }

  public static synchronized boolean isIsolating(Context givenContext) {
    if (isIsolating == null) {
      isIsolating = getIsolationStatus(givenContext);
    }

    return isIsolating;
  }

  private static char[] readKey(String fileName, Context context) {
    try (InputStream stream = context.getAssets().open(fileName)) {
      int size = stream.available();
      Log.i(Util.class.getName(), "The size of the" + fileName + " is " + size);
      byte[] bytes = new byte[size];
      stream.read(bytes);
      return new String(bytes).toCharArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Log.e(Util.class.getName(), "The " + fileName + "was not read.");
    return null;
  }

  private static boolean getIsolationStatus(Context context) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    return sharedPreferences.getBoolean(SHARED_PREFERENCES_ISO_KEY, false);
  }

  public static void setIsolationStatus(Context context, boolean value, Long endIsolation) {
    Log.d(Util.class.getName(), "Setting isolation to " + value + " end " + endIsolation);
    isIsolating = value;
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(SHARED_PREFERENCES_ISO_KEY, value);
    editor.putLong(SHARED_PREFERENCES_ISO_END_KEY, endIsolation);
    editor.apply();
  }

  private static String getOrUpdateSharedPrefIfNotPresent(Context context) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    if (sharedPreferences.contains(SHARED_PREFERENCES_UID_KEY)) {
      return sharedPreferences.getString(SHARED_PREFERENCES_UID_KEY, "");
    }
    SharedPreferences.Editor editor = sharedPreferences.edit();
    String generatedId = UUID.randomUUID().toString();
    editor.putString(SHARED_PREFERENCES_UID_KEY, generatedId);
    editor.apply();

    return generatedId;
  }
}
