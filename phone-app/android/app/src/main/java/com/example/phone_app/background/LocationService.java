package com.example.phone_app.background;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.phone_app.MainActivity;

public class LocationService extends Service {
  private boolean isStarted = false;

  public LocationService() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void onCreate() {
    super.onCreate();
    startForeground(1, createNotification());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(LocationService.class.getName(), "Started the service that sends the location.");
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

  @SuppressLint("MissingPermission")
  private void startService() {
    if (isStarted) {
      Log.d(LocationService.class.getName(), "Detected start.");
      return;
    }
    isStarted = true;

    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2.5F,
            new CTLocationListener(Util.getUuid(this), Util.getPublicKey(this)));
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5 * 60, 0F,
            new CTLocationListener(Util.getUuid(this), Util.getPublicKey(this)));
  }
}
