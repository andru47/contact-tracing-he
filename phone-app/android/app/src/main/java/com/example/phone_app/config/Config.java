package com.example.phone_app.config;

public class Config {
  private static final EncryptionType encryptionType = EncryptionType.SEAL;
  private static final boolean timingEnabled = false;
  private static final boolean uploadTestLocationsEnabled = false;

  public static EncryptionType getEncryptionType() {
    return encryptionType;
  }

  public static boolean isTimingEnabled() {
    return timingEnabled;
  }

  public static boolean isUploadTestLocationsEnabled() {
    return uploadTestLocationsEnabled;
  }
}
