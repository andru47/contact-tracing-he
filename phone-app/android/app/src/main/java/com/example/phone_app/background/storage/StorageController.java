package com.example.phone_app.background.storage;

import com.example.phone_app.background.ConnectionService;

import java.util.List;
import java.util.logging.Logger;

import io.objectbox.Box;

public class StorageController {
  private final Box<LocationEntity> box;
  Logger storageLogger = Logger.getLogger(StorageController.class.getName());

  public StorageController(Box<LocationEntity> givenBox) {
    this.box = givenBox;
  }

  public void sendLocationsToServer(long timestamp) {
    removeAllOldLocations(timestamp);
    ConnectionService.sendLocationHistory(box.getAll());
  }

  public void addLocation(Double latitude, Double longitude, Integer givenTimestamp) {
    LocationEntity object = new LocationEntity(latitude, longitude, givenTimestamp);
    box.put(object);
  }

  public List<LocationEntity> genAllOldLocations(Long givenTimestamp) {
    List<LocationEntity> returnedList = null;
    storageLogger.info("Timestamp is " + givenTimestamp.toString());
    returnedList = box.query().less(LocationEntity_.locationTimestamp, givenTimestamp + 1).build().find();

    return returnedList;
  }

  public void removeAllOldLocations(long givenTimestamp) {
    box.remove(genAllOldLocations(givenTimestamp - 7 * 24 * 60 * 60));
  }
}
