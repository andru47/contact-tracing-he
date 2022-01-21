package com.example.phone_app.background.serialization;


public class NewSavedLocations {
  private Double latitude1, latitude2, longitude1, longitude2;
  private Integer rowNr;

  public Double getLatitude1() {
    return latitude1;
  }

  public Double getLatitude2() {
    return latitude2;
  }

  public Double getLongitude1() {
    return longitude1;
  }

  public Double getLongitude2() {
    return longitude2;
  }

  public Integer getRowNr() {
    return rowNr;
  }

  public void setRowNr(Integer rowNr) {
    this.rowNr = rowNr;
  }
}
