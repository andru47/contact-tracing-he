package com.example.phone_app.background;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

import com.example.phone_app.CiphertextWrapper;
import com.example.phone_app.JNIBridge;
import com.example.phone_app.background.serialization.LocationUploadMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.flutter.Log;

public class CTLocationListener implements LocationListener {
  private final String userId;
  private final char[] publicKey;
  private final static JNIBridge bridge = new JNIBridge();

  CTLocationListener(String userId, char[] publicKey) {
    this.userId = userId;
    this.publicKey = publicKey;
  }
  @Override
  public void onLocationChanged(@NonNull Location location) {
    Log.i("location_listener", "Got new location " + location.getLatitude() + location.getLongitude());
    double latitudeCos = Math.cos(Math.toRadians(location.getLatitude()));
    double longitudeCos = Math.cos(Math.toRadians(location.getLongitude()));
    double latitudeSin = Math.sin(Math.toRadians(location.getLatitude()));
    double longitudeSin = Math.sin(Math.toRadians(location.getLongitude()));
    CiphertextWrapper wrapper = bridge.encrypt(latitudeCos, latitudeSin, longitudeCos, longitudeSin, publicKey);

    Thread thread = new Thread() {
      @Override
      public void run() {
        ConnectionService.sendLocation(new LocationUploadMessage(wrapper.getLatitudeCos(),
                wrapper.getLatitudeSin(),
                wrapper.getLongitudeCos(),
                wrapper.getLongitudeSin(),
                userId,
                getTimestamp(location.getTime())));
      }
    };
    thread.start();
  }

  private String getTimestamp(long utcMillis) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf.format(new Date(utcMillis));
  }
}
