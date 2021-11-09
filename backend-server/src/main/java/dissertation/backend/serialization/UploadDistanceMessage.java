package dissertation.backend.serialization;

public class UploadDistanceMessage {
  private String id, latitudeCos, latitudeSin, longitudeCos, longitudeSin, timestamp;

  public String getLongitudeSin() {
    return longitudeSin;
  }

  public String getLongitudeCos() {
    return longitudeCos;
  }

  public String getLatitudeSin() {
    return latitudeSin;
  }

  public String getLatitudeCos() {
    return latitudeCos;
  }

  public String getId() {
    return id;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
