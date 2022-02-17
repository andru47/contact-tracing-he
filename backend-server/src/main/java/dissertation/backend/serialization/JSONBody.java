package dissertation.backend.serialization;

public class JSONBody {
  private final String latitudeCos1, latitudeSin1, longitudeCos1, longitudeSin1;
  private final String latitudeCos2, latitudeSin2, longitudeCos2, longitudeSin2;
  private final String rlk;

  public JSONBody(String latitudeCos1, String latitudeSin1, String longitudeCos1, String longitudeSin1,
                  String latitudeCos2, String latitudeSin2, String longitudeCos2, String longitudeSin2, String rlk) {
    this.latitudeCos1 = latitudeCos1;
    this.latitudeSin1 = latitudeSin1;
    this.longitudeCos1 = longitudeCos1;
    this.longitudeSin1 = longitudeSin1;
    this.latitudeCos2 = latitudeCos2;
    this.latitudeSin2 = latitudeSin2;
    this.longitudeCos2 = longitudeCos2;
    this.longitudeSin2 = longitudeSin2;
    this.rlk = rlk;
  }

  public String getLatitudeCos1() {
    return latitudeCos1;
  }

  public String getLatitudeSin1() {
    return latitudeSin1;
  }

  public String getLongitudeCos1() {
    return longitudeCos1;
  }

  public String getLongitudeSin1() {
    return longitudeSin1;
  }

  public String getLatitudeCos2() {
    return latitudeCos2;
  }

  public String getLatitudeSin2() {
    return latitudeSin2;
  }

  public String getLongitudeCos2() {
    return longitudeCos2;
  }

  public String getLongitudeSin2() {
    return longitudeSin2;
  }

  public String getRlk() {
    return rlk;
  }
}
