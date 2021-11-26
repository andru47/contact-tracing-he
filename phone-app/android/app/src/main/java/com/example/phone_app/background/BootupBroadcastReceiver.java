package com.example.phone_app.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class BootupBroadcastReceiver extends BroadcastReceiver {

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(BootupBroadcastReceiver.class.getName(), "Received broadcast " + intent.getAction());
    if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
      Intent locationService = new Intent(context, LocationService.class);
      context.startForegroundService(locationService);
    }
  }
}
