package com.example.phone_app.background;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.phone_app.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class LocationService extends Service {
  private boolean isStarted = false;
  protected final static String SHARED_PREFERENCES_FILENAME = "com.example.phone_app.PRIVATE_PREFS";
  private final static String SHARED_PREFERENCES_UID_KEY = "uuid";
  private static String uuid = null;
  private static char[] publicKey = null;

  public LocationService() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void onCreate() {
    if (uuid == null) {
      uuid = getOrUpdateSharedPrefIfNotPresent();
      publicKey = getPublicKey();
    }
    super.onCreate();
    startForeground(1, createNotification());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(getPackageName(), "Started the service that sends the location.");
    startService();
    return START_STICKY;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private Notification createNotification() {
    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    NotificationChannel channel = new NotificationChannel("HE-CONTACT-TRACING", "Location getter", NotificationManager.IMPORTANCE_HIGH);

    notificationManager.createNotificationChannel(channel);
    return new Notification.Builder(this, "HE-CONTACT-TRACING")
            .setContentTitle("HE location getter")
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
            .build();
  }

  private char[] getPublicKey() {
    try (InputStream stream = getAssets().open("pubKey.bin")) {
      int size = stream.available();
      Log.i("publicKeyGetter", "The size of the public key is " + size);
      byte[] bytes = new byte[size];
      stream.read(bytes);
      return new String(bytes).toCharArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Log.e("publicKeyGetter", "The public key was not read.");
    return null;
  }

  private String getOrUpdateSharedPrefIfNotPresent() {
    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_FILENAME, MODE_PRIVATE);
    if (sharedPreferences.contains(SHARED_PREFERENCES_UID_KEY)) {
      return sharedPreferences.getString(SHARED_PREFERENCES_UID_KEY, "");
    }
    SharedPreferences.Editor editor = sharedPreferences.edit();
    String generatedId = UUID.randomUUID().toString();
    editor.putString(SHARED_PREFERENCES_UID_KEY, generatedId);
    editor.apply();

    return generatedId;
  }

  @SuppressLint("MissingPermission")
  private void startService() {
    if (isStarted) {
      Log.i(getPackageName(), "detected start");
      return;
    }
    isStarted = true;

    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2.5F, new CTLocationListener(uuid, publicKey));
  }
}
