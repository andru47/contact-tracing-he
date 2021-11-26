package com.example.phone_app.background.serialization;

public class NewTokenMessage {
  private String userId, token;

  public NewTokenMessage(String userId, String token) {
    this.userId = userId;
    this.token = token;
  }
}
