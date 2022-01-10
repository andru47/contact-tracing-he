package com.example.phone_app.background.serialization;

public class JSONBody {
  private final String latitudeCos1, latitudeSin1, longitudeCos1, longitudeSin1;
  private final String latitudeCos2, latitudeSin2, longitudeCos2, longitudeSin2;

  public JSONBody(String latitudeCos1, String latitudeSin1, String longitudeCos1, String longitudeSin1,
                  String latitudeCos2, String latitudeSin2, String longitudeCos2, String longitudeSin2) {
    this.latitudeCos1 = latitudeCos1;
    this.latitudeSin1 = latitudeSin1;
    this.longitudeCos1 = longitudeCos1;
    this.longitudeSin1 = longitudeSin1;
    this.latitudeCos2 = latitudeCos2;
    this.latitudeSin2 = latitudeSin2;
    this.longitudeCos2 = longitudeCos2;
    this.longitudeSin2 = longitudeSin2;
  }
}
