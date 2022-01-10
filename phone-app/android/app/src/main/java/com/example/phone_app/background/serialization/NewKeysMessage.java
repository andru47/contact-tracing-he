package com.example.phone_app.background.serialization;

public class NewKeysMessage {
  private final String pubKey;
  private final String relinKey;
  private final String userId;

  public NewKeysMessage(String pubKey, String relinKey, String userId) {
    this.pubKey = pubKey;
    this.relinKey = relinKey;
    this.userId = userId;
  }
}
