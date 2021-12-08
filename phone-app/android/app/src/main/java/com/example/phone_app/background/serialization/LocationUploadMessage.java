package com.example.phone_app.background.serialization;

public class LocationUploadMessage {
  String latitudeCos, latitudeSin, longitudeCos, longitudeSin, altitude, id, timestamp;

  public LocationUploadMessage(String latitudeCos, String latitudeSin, String longitudeCos, String longitudeSin, String altitude, String id, String timestamp) {
    this.latitudeCos = latitudeCos;
    this.latitudeSin = latitudeSin;
    this.longitudeCos = longitudeCos;
    this.longitudeSin = longitudeSin;
    this.altitude = altitude;
    this.id = id;
    this.timestamp = timestamp;
  }
}
