package com.example.phone_app.background.serialization;

public class NewDistanceMessage {
  private String ciphertext, contactUserId;
  private long timestamp, timestampEnd;

  public long getTimestamp() {
    return timestamp;
  }

  public String getContactUserId() {
    return contactUserId;
  }

  public long getTimestampEnd() {
    return timestampEnd;
  }

  public String getCiphertext() {
    return ciphertext;
  }
}