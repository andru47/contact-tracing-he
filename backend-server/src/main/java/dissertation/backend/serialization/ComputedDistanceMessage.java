package dissertation.backend.serialization;

public class ComputedDistanceMessage {
  private final String partialDistance, partialAltitudeDifference, rowId;
  private final String ciphertext, altitudeDifference, contactUserId;
  private final long timestamp, timestampEnd;

  public ComputedDistanceMessage(String ciphertext, String altitudeDifference, String rowId, String contactUserId, long timestamp, long timestampEnd) {
    this.partialDistance = "";
    this.partialAltitudeDifference = "";
    this.rowId = rowId;
    this.ciphertext = ciphertext;
    this.altitudeDifference = altitudeDifference;
    this.contactUserId = contactUserId;
    this.timestamp = timestamp;
    this.timestampEnd = timestampEnd;
  }

  public ComputedDistanceMessage(String partialDistance, String partialAltitudeDifference, String ciphertext, String altitudeDifference, String contactUserId, long timestamp, long timestampEnd) {
    this.partialDistance = partialDistance;
    this.partialAltitudeDifference = partialAltitudeDifference;
    this.rowId = "";
    this.ciphertext = ciphertext;
    this.altitudeDifference = altitudeDifference;
    this.contactUserId = contactUserId;
    this.timestamp = timestamp;
    this.timestampEnd = timestampEnd;
  }
}
