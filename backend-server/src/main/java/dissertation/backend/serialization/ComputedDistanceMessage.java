package dissertation.backend.serialization;

public class ComputedDistanceMessage {
  private final String ciphertext, contactUserId;
  private final long timestamp, timestampEnd;

  public ComputedDistanceMessage(String ciphertext, String contactUserId, long timestamp, long timestampEnd) {
    this.ciphertext = ciphertext;
    this.contactUserId = contactUserId;
    this.timestamp = timestamp;
    this.timestampEnd = timestampEnd;
  }
}
