package com.example.phone_app;

public class JNIBridge {
  static {
    try {
      System.loadLibrary("jni-android-he");
    } catch (Error | Exception e) {
      e.printStackTrace();
    }
  }

  public native char[] encrypt(String plain);

  public native String decrypt(char[] cipher);
}
