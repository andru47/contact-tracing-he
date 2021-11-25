package dissertation.backend.serialization;

public class ContactMessage {
  private String userId, infectedUserId;
  private long timestamp, timestampEnd;

  public String getUserId() {
    return userId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getTimestampEnd() {
    return timestampEnd;
  }

  public String getInfectedUserId() {
    return infectedUserId;
  }
}
