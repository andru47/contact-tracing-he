package com.example.phone_app.config;

public class Config {
  private static final EncryptionType encryptionType = EncryptionType.LATTIGO_MK;
  private static final boolean timingEnabled = false;
  private static final boolean uploadTestLocationsEnabled = true;

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
