package com.example.phone_app.background.serialization;

public class NewDistanceMessage {
  private String partialDistance, partialAltitudeDifference, rowId;
  private String ciphertext, altitudeDifference, contactUserId;
  private long timestamp, timestampEnd;

  public long getTimestamp() {
    return timestamp;
  }

  public String getAltitudeDifference() {
    return altitudeDifference;
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

  public String getPartialAltitudeDifference() {
    return partialAltitudeDifference;
  }

  public String getPartialDistance() {
    return partialDistance;
  }

  public String getRowId() {
    return rowId;
  }
}
