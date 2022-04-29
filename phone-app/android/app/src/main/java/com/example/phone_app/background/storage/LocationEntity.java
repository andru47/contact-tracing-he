package com.example.phone_app.background.storage;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class LocationEntity {
  @Id
  private long id = 0;
  private Double latitude = 0.0, longitude = 0.0;
  private Integer locationTimestamp = 0;

  LocationEntity(Double latitude, Double longitude, Integer locationTimestamp) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationTimestamp = locationTimestamp;
  }

  LocationEntity(long id, Double latitude, Double longitude, Integer locationTimestamp) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationTimestamp = locationTimestamp;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Integer getLocationTimestamp() {
    return locationTimestamp;
  }

  public void setLocationTimestamp(Integer locationTimestamp) {
    this.locationTimestamp = locationTimestamp;
  }
}
