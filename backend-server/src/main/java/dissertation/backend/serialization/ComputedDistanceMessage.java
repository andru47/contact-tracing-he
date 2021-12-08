package dissertation.backend.serialization;

public class ComputedDistanceMessage {
  private final String ciphertext, altitudeDifference, contactUserId;
  private final long timestamp, timestampEnd;

  public ComputedDistanceMessage(String ciphertext, String altitudeDifference, String contactUserId, long timestamp, long timestampEnd) {
    this.ciphertext = ciphertext;
    this.altitudeDifference = altitudeDifference;
    this.contactUserId = contactUserId;
    this.timestamp = timestamp;
    this.timestampEnd = timestampEnd;
  }
}
