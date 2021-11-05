package com.example.phone_app;

public class JNIBridge {
  static {
    try {
      System.loadLibrary("jni-android-he");
    } catch (Error | Exception e) {
      e.printStackTrace();
    }
  }

  public native CiphertextWrapper encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin);

  public native char[] getRelinKeys();

  public native char[] getPrivateKey();

  public native double decrypt(char[] cipher);
}
