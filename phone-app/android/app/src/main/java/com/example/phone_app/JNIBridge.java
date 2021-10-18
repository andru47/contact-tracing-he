package com.example.phone_app;

public class JNIBridge {
  static {
    try {
      System.loadLibrary("jni-android-he");
    } catch (Error | Exception e) {
      e.printStackTrace();
    }
  }

  public String getGreetingMessage(String name) {
    return getNativeGreetingMessage(name);
  }

  private native String getNativeGreetingMessage(String name);
}
