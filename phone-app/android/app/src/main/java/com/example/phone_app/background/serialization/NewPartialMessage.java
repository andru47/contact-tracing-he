package com.example.phone_app.background.serialization;

public class NewPartialMessage {
  private final String partialDistance, partialAltitudeDifference, rowId;

  public NewPartialMessage(String partialDistance, String partialAltitudeDifference, String rowId) {
    this.partialDistance = partialDistance;
    this.partialAltitudeDifference = partialAltitudeDifference;
    this.rowId = rowId;
  }
}
