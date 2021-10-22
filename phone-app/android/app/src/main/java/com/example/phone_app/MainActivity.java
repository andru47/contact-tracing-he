package com.example.phone_app;

import androidx.annotation.NonNull;

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
                        result.success(new String(bridge.encrypt((String) call.argument("plain"))));
                      } else {
                        result.success(bridge.decrypt(((String) call.argument("cipher")).toCharArray()));
                      }
                    }
            );
  }
}
