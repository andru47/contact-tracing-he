package com.example.phone_app;

public class JNIBridge {
  static {
    try {
      System.loadLibrary("jni-android-he");
    } catch (Error | Exception e) {
      e.printStackTrace();
    }
  }

  public native CiphertextWrapper encrypt(double latitudeCos, double latitudeSin, double longitudeCos, double longitudeSin, double altitude, char[] publicKey);

  public native char[] getRelinKeys();

  public native char[] getPrivateKey();

  public native char[] getPublicKey();

  public native char[] getMKPublicKey();

  public native void generateKeys();

  public native double decrypt(char[] cipher, char[] privateKey);

  public native Object decryptMulti(char[] cipher, char[] partial, char[] privateKey, boolean finalDecryption);
}
