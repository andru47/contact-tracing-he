package dissertation.backend.serialization;

public class LocationHistoryMessage {
  private Double latitude, longitude;
  private Integer locationTimestamp;

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public Integer getLocationTimestamp() {
    return locationTimestamp;
  }
}
