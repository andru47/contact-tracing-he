package com.example.phone_app.config;

public class Config {
  private static final EncryptionType encryptionType = EncryptionType.LATTIGO_MK;

  public static EncryptionType getEncryptionType() {
    return encryptionType;
  }
}
