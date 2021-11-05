package com.example.phone_app;

public class CiphertextWrapper {
  private char[] latitudeCos, latitudeSin, longitudeCos, longitudeSin;

  public void setLatitudeCos(char[] latitudeCos) {
    this.latitudeCos = latitudeCos;
  }

  public void setLatitudeSin(char[] latitudeSin) {
    this.latitudeSin = latitudeSin;
  }

  public void setLongitudeCos(char[] longitudeCos) {
    this.longitudeCos = longitudeCos;
  }

  public void setLongitudeSin(char[] longitudeSin) {
    this.longitudeSin = longitudeSin;
  }

  public String getLatitudeCos() {
    return new String(latitudeCos);
  }

  public String getLatitudeSin() {
    return new String(latitudeSin);
  }

  public String getLongitudeCos() {
    return new String(longitudeCos);
  }

  public String getLongitudeSin() {
    return new String(longitudeSin);
  }
}
