package com.example.phone_app.config;

public class Config {
  private static final EncryptionType encryptionType = EncryptionType.SMKHE;
  private static final boolean timingEnabled = false;
  private static final boolean uploadTestLocationsEnabled = false;
  private static final Double epsilon = 0.01;
  private static final int movementTime = 5000, inactivityTime = 1000 * 5 * 60; //Milliseconds
  private static final float distance = 5; // Meters

  public static EncryptionType getEncryptionType() {
    return encryptionType;
  }

  public static boolean isTimingEnabled() {
    return timingEnabled;
  }

  public static boolean isUploadTestLocationsEnabled() {
    return uploadTestLocationsEnabled;
  }

  public static Double getEpsilon() {
    return epsilon;
  }

  public static float getDistance() {
    return distance;
  }

  public static int getInactivityTime() {
    return inactivityTime;
  }

  public static int getMovementTime() {
    return movementTime;
  }
}
