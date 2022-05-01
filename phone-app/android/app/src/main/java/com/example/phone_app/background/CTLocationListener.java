package com.example.phone_app.background;

import android.location.Location;
import android.location.LocationListener;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.example.phone_app.CiphertextWrapper;
import com.example.phone_app.JNIBridge;
import com.example.phone_app.background.serialization.LocationUploadMessage;
import com.example.phone_app.background.storage.StorageController;

import io.flutter.Log;

public class CTLocationListener implements LocationListener {
  private final static JNIBridge bridge = new JNIBridge();
  private final String userId;
  private final char[] publicKey;
  private final StorageController controller;

  CTLocationListener(String userId, char[] publicKey, StorageController controller) {
    this.userId = userId;
    this.publicKey = publicKey;
    this.controller = controller;
  }

  @Override
  public void onLocationChanged(@NonNull Location location) {
    Log.d(CTLocationListener.class.getName(), "Got new location " + location.getLatitude() + " " + location.getLongitude() + " " + location.getAltitude());

    Pair<Double, Double> alteredLocation = LocationModifier.perturbLocation(new Pair<>(location.getLatitude(), location.getLongitude()));
    controller.addLocation(alteredLocation.first, alteredLocation.second, Integer.valueOf(getTimestamp(location.getTime())));

    double latitudeCos = Math.cos(Math.toRadians(location.getLatitude()));
    double longitudeCos = Math.cos(Math.toRadians(location.getLongitude()));
    double latitudeSin = Math.sin(Math.toRadians(location.getLatitude()));
    double longitudeSin = Math.sin(Math.toRadians(location.getLongitude()));
    CiphertextWrapper wrapper = bridge.encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin, location.getAltitude(), publicKey);

    Thread thread = new Thread() {
      @Override
      public void run() {
        ConnectionService.sendObject(new LocationUploadMessage(wrapper.getLatitudeCos(),
                wrapper.getLatitudeSin(),
                wrapper.getLongitudeCos(),
                wrapper.getLongitudeSin(),
                wrapper.getAltitude(),
                userId,
                getTimestamp(location.getTime())), "upload-location");
      }
    };
    thread.start();
  }

  private String getTimestamp(long utcMillis) {
    return String.valueOf(utcMillis / 1000);
  }
}
