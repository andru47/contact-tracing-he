package com.example.phone_app.background;

import android.content.Context;
import android.util.Log;

import com.example.phone_app.CiphertextWrapper;
import com.example.phone_app.JNIBridge;
import com.example.phone_app.background.serialization.LocationUploadMessage;
import com.example.phone_app.background.serialization.NewSavedLocations;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestLocationUploader {
  private static final String fileName = "test-locations.json";
  private static final Type savedLocationType = new TypeToken<ArrayList<NewSavedLocations>>() {
  }.getType();

  private static String readFromFile(Context context) {
    try {
      InputStream inputStream = context.getAssets().open(fileName);
      byte[] bytes = new byte[inputStream.available()];
      inputStream.read(bytes);
      return new String(bytes);
    } catch (IOException e) {
      Log.e(TestLocationUploader.class.getName(), "Error reading " + fileName + ": " + e.toString());
    }
    return null;
  }

  public static void readLocationsAndUploadEncrypted(Context context) {
    ExecutorService service = Executors.newFixedThreadPool(5);
    String json = readFromFile(context);
    JNIBridge bridge = new JNIBridge();
    char[] pubKey = Util.getPublicKey(context);

    ArrayList<NewSavedLocations> locations = new Gson().fromJson(json, savedLocationType);
    for (int index = 0; index < locations.size(); ++index) {
      locations.get(index).setRowNr(index);
    }
    for (NewSavedLocations location : locations) {
      service.execute(() -> {
        double latCos = Math.cos(Math.toRadians(location.getLatitude2()));
        double latSin = Math.sin(Math.toRadians(location.getLatitude2()));
        double longCos = Math.cos(Math.toRadians(location.getLongitude2()));
        double longSin = Math.sin(Math.toRadians(location.getLongitude2()));
        CiphertextWrapper wrapper = bridge.encrypt(latCos, latSin, longCos, longSin, 1.0, pubKey);
        ConnectionService.sendObject(new LocationUploadMessage(wrapper.getLatitudeCos(), wrapper.getLatitudeSin(), wrapper.getLongitudeCos(), wrapper.getLongitudeSin(), wrapper.getAltitude(), "second", location.getRowNr().toString()), "upload-location");
      });
    }
  }
}
