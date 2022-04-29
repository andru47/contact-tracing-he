package com.example.phone_app.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.phone_app.JNIBridge;
import com.example.phone_app.background.serialization.NewKeysMessage;
import com.example.phone_app.config.Config;
import com.example.phone_app.config.EncryptionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

public class Util {
  protected final static String SHARED_PREFERENCES_FILENAME = "com.example.phone_app.PRIVATE_PREFS";
  private final static String SHARED_PREFERENCES_UID_KEY = "uuid";
  private final static String SHARED_PREFERENCES_ISO_END_KEY = "isolation-end";
  private final static String SHARED_PREFERENCES_KEYS_CREATED = "are-keys-created";
  private static String uuid = null;
  private static char[] privateKey = null;
  private static char[] publicKey = null;
  private static Long isolationEnd = null;

  public static synchronized String getUuid(Context givenContext) {
    if (uuid == null) {
      uuid = getOrUpdateSharedPrefIfNotPresent(givenContext);
    }

    return uuid;
  }

  public static char[] getPublicKey(Context givenContext) {
    if (publicKey == null) {
      String fileName;
      if (Config.getEncryptionType() == EncryptionType.SEAL || Config.getEncryptionType() == EncryptionType.MULTI_KEY) {
        fileName = "pubKey.bin";
      } else if (Config.getEncryptionType() == EncryptionType.LATTIGO) {
        fileName = "pubKeyLattigo.bin";
      } else {
        fileName = "pubKeySMKHE.bin";
      }
      publicKey = readKey(fileName, givenContext);
    }

    return publicKey;
  }

  public static char[] getPrivateKey(Context givenContext) {
    if (privateKey == null) {
      String fileName;
      if (Config.getEncryptionType() == EncryptionType.SEAL || Config.getEncryptionType() == EncryptionType.MULTI_KEY) {
        fileName = "privateKey.bin";
      } else if (Config.getEncryptionType() == EncryptionType.LATTIGO) {
        fileName = "privateKeyLattigo.bin";
      } else {
        fileName = "privateKeySMKHE.bin";
      }
      privateKey = readKey(fileName, givenContext);
    }

    return privateKey;
  }

  private static char[] readFromInputStream(InputStream is, String fileName) {
    try {
      int size = is.available();
      Log.d(Util.class.getName(), "The size of the " + fileName + " is " + size);
      byte[] bytes = new byte[size];
      is.read(bytes);
      return new String(bytes).toCharArray();
    } catch (IOException e) {
      Log.e(Util.class.getName(), e.toString());
    }

    Log.e(Util.class.getName(), "The " + fileName + "was not read.");
    return null;
  }

  private static char[] readKey(String fileName, Context context) {
    if (Config.getEncryptionType() == EncryptionType.MULTI_KEY) {
      if (getAreKeysCreated(context)) {
        try (FileInputStream fis = new FileInputStream(new File(context.getFilesDir(), "generated_key/" + fileName))) {
          return readFromInputStream(fis, fileName);
        } catch (FileNotFoundException e) {
          Log.e(Util.class.getName(), "The " + fileName + "does not exist.");
        } catch (IOException e) {
          Log.e(Util.class.getName(), "The " + fileName + "cannot be opened.");
        }
        return null;
      }
      generateKeys(context);
      return readKey(fileName, context);
    }

    try (InputStream stream = context.getAssets().open(fileName)) {
      return readFromInputStream(stream, fileName);
    } catch (IOException e) {
      Log.e(Util.class.getName(), e.toString());
    }
    Log.e(Util.class.getName(), "The " + fileName + "was not read.");
    return null;
  }

  private static void writeKeyTo(Context context, String fileName, String contents) {
    File directory = new File(context.getFilesDir(), "generated_key/");
    if (!directory.exists()) {
      directory.mkdir();
    }
    File file = new File(directory, fileName);
    try {
      Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      Log.e(Util.class.getName(), "Couldn't write to " + fileName + " :" + e.toString());
    }
  }

  private static synchronized void generateKeys(Context context) {
    if (getAreKeysCreated(context)) {
      return;
    }

    JNIBridge bridge = new JNIBridge();
    bridge.generateKeys();
    publicKey = bridge.getPublicKey();
    privateKey = bridge.getPrivateKey();
    char[] mkPubKey = bridge.getMKPublicKey();
    char[] rlk = bridge.getRelinKeys();

    writeKeyTo(context, "pubKey.bin", new String(publicKey));
    writeKeyTo(context, "privateKey.bin", new String(privateKey));
    new Thread(() -> ConnectionService.sendObject(new NewKeysMessage(new String(mkPubKey), new String(rlk), getUuid(context)), "new-user-keys")).start();
    setAreKeysCreated(context, true);
  }

  public static Long getIsolation(Context context) {
    return getIsolationEnd(context);
  }

  private static Long getIsolationEnd(Context context) {
    if (isolationEnd == null) {
      SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
      isolationEnd = sharedPreferences.getLong(SHARED_PREFERENCES_ISO_END_KEY, 0L);
      if (isolationEnd < System.currentTimeMillis() / 1000L) {
        isolationEnd = 0L;
        setIsolationStatus(context, 0L);
      }
    }

    return isolationEnd;
  }

  public static void setIsolationStatus(Context context, Long endIsolation) {
    Log.d(Util.class.getName(), "Got isolation end " + endIsolation);
    isolationEnd = endIsolation;
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong(SHARED_PREFERENCES_ISO_END_KEY, endIsolation);
    editor.commit();
  }

  private static String getOrUpdateSharedPrefIfNotPresent(Context context) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    if (sharedPreferences.contains(SHARED_PREFERENCES_UID_KEY)) {
      return sharedPreferences.getString(SHARED_PREFERENCES_UID_KEY, "");
    }
    SharedPreferences.Editor editor = sharedPreferences.edit();
    String generatedId = UUID.randomUUID().toString();
    Log.d(Util.class.getName(), "Generated uuid " + generatedId);
    editor.putString(SHARED_PREFERENCES_UID_KEY, generatedId);
    editor.apply();

    return generatedId;
  }

  private static boolean getAreKeysCreated(Context context) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    return sharedPreferences.getBoolean(SHARED_PREFERENCES_KEYS_CREATED, false);
  }

  public static void setAreKeysCreated(Context context, boolean value) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(SHARED_PREFERENCES_KEYS_CREATED, value);
    editor.apply();
  }
}
