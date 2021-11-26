package com.example.phone_app.background.serialization;

public class ContactMessage {
  private final String userId, infectedUserId;
  private final long timestamp, timestampEnd;

  public ContactMessage(String userId, String infectedUserId, long timestamp, long timestampEnd) {
    this.userId = userId;
    this.infectedUserId = infectedUserId;
    this.timestamp = timestamp;
    this.timestampEnd = timestampEnd;
  }
}
