package com.example.phone_app;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
  private final String CHANNEL = "BRIDGE";
  private final JNIBridge bridge = new JNIBridge();

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler(
                    (call, result) -> {
                      if (call.method.equals("encrypt")) {
                        double latitude = (double) call.argument("latitude");
                        double longitude = (double) call.argument("longitude");
                        double latitudeRad = latitude * Math.PI / 180.0;
                        double longitudeRad = longitude * Math.PI / 180.0;
                        String publicKey = (String) call.argument("publicKey");
                        CiphertextWrapper wrapper = bridge.encrypt(Math.cos(latitudeRad), Math.sin(latitudeRad), Math.cos(longitudeRad), Math.sin(longitudeRad), publicKey.toCharArray());
                        result.success(Arrays.asList(wrapper.getLatitudeCos(), wrapper.getLatitudeSin(), wrapper.getLongitudeCos(), wrapper.getLongitudeSin()));
                      } else if (call.method.equals("decrypt")) {
                        result.success(bridge.decrypt(((String) call.argument("cipher")).toCharArray(), ((String) call.argument("privateKey")).toCharArray()));
                      } else if (call.method.equals("keys")) {
                        result.success(Arrays.asList(new String(bridge.getRelinKeys()), new String(bridge.getPrivateKey()), new String(bridge.getPublicKey())));
                      }
                    }
            );
  }
}
