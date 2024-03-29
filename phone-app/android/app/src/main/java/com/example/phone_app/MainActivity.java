package com.example.phone_app;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.phone_app.background.LocationModifier;
import com.example.phone_app.background.LocationService;
import com.example.phone_app.background.TestLocationUploader;
import com.example.phone_app.background.Util;
import com.example.phone_app.background.storage.LocationEntity;
import com.example.phone_app.background.storage.MyObjectBox;
import com.example.phone_app.background.storage.StorageController;
import com.example.phone_app.config.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.objectbox.Box;

public class MainActivity extends FlutterActivity {
  private final String CHANNEL = "BRIDGE";
  private final JNIBridge bridge = new JNIBridge();
  private Box<LocationEntity> box;
  private StorageController controller;

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    box = MyObjectBox.builder().androidContext(this).build().boxFor(LocationEntity.class);
    controller = new StorageController(box);
    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1);
    if (Config.isUploadTestLocationsEnabled()) {
      TestLocationUploader.readLocationsAndUploadEncrypted(this);
    } else {
      if (Util.getIsolation(this) == 0L) {
        Intent intent = new Intent(this, LocationService.class);
        startForegroundService(intent);
      }
    }
  }

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler(
                    (call, result) -> {
                      if (call.method.equals("encrypt")) {
                        double latitude1 = call.argument("latitude1");
                        double longitude1 = call.argument("longitude1");
                        double latitudeRad1 = latitude1 * Math.PI / 180.0;
                        double longitudeRad1 = longitude1 * Math.PI / 180.0;
                        double latitude2 = call.argument("latitude2");
                        double longitude2 = call.argument("longitude2");
                        double latitudeRad2 = latitude2 * Math.PI / 180.0;
                        double longitudeRad2 = longitude2 * Math.PI / 180.0;
                        String publicKey = call.argument("publicKey");
                        CiphertextWrapper wrapper1 = bridge.encrypt(Math.cos(latitudeRad1), Math.sin(latitudeRad1), Math.cos(longitudeRad1), Math.sin(longitudeRad1), 0, publicKey.toCharArray());
                        CiphertextWrapper wrapper2 = bridge.encrypt(Math.cos(latitudeRad2), Math.sin(latitudeRad2), Math.cos(longitudeRad2), Math.sin(longitudeRad2), 0, publicKey.toCharArray());
                        result.success(Arrays.asList(wrapper1.getLatitudeCos(), wrapper1.getLatitudeSin(), wrapper1.getLongitudeCos(), wrapper1.getLongitudeSin(),
                                wrapper2.getLatitudeCos(), wrapper2.getLatitudeSin(), wrapper2.getLongitudeCos(), wrapper2.getLongitudeSin()));
                      } else if (call.method.equals("decrypt")) {
                        result.success(bridge.decrypt(((String) call.argument("cipher")).toCharArray(), ((String) call.argument("privateKey")).toCharArray()));
                      } else if (call.method.equals("keys")) {
                        result.success(Arrays.asList(new String(bridge.getRelinKeys()), new String(bridge.getPrivateKey()), new String(bridge.getPublicKey())));
                      } else if (call.method.equals("get-isolation")) {
                        result.success(Util.getIsolation(this));
                      } else if (call.method.equals("get-uid")) {
                        result.success(Util.getUuid(this));
                      } else if (call.method.equals("set-positive")) {
                        Long givenTimestamp = Long.parseLong(call.argument("end"));
                        Util.setIsolationStatus(this, givenTimestamp);
                        controller.sendLocationsToServer(System.currentTimeMillis() / 1000L);
                        result.success("SUCCESS");
                      } else if (call.method.equals("perturb")) {
                        Double latitude = Double.parseDouble(call.argument("lat"));
                        Double longitude = Double.parseDouble(call.argument("long"));
                        Pair<Double, Double> newLocation = LocationModifier.perturbLocation(new Pair<>(latitude, longitude));
                        Map<String, Double> ret = new HashMap<>();
                        ret.put("lat", newLocation.first);
                        ret.put("long", newLocation.second);
                        result.success(ret);
                      }
                    }
            );
  }
}
