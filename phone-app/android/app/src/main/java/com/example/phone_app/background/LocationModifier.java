package com.example.phone_app.background;

import android.util.Log;
import android.util.Pair;

import com.example.phone_app.config.Config;

public class LocationModifier {
  private final static Double EARTH_RADIUS_METERS = 6371E3;

  public static Pair<Double, Double> perturbLocation(Pair<Double, Double> location) {
    double epsilon = Config.getEpsilon();

    double sampledBearing = Math.random() * 2 * Math.PI;
    double sampledProbability = Math.random();

    Log.i(LocationModifier.class.getName(), "I have sampled " + sampledProbability);

    double sampledLambert = approximateLambertW((sampledProbability - 1) / Math.E);
    Log.i(LocationModifier.class.getName(), "I have sampled " + sampledLambert);

    double radius = Math.abs((-1 / epsilon) * (sampledLambert + 1));
    Log.i(LocationModifier.class.getName(), "Sampled distance is " + radius);

    return addDistanceToLocationAndBearing(location, radius, sampledBearing);
  }

  private static Pair<Double, Double> addDistanceToLocationAndBearing(Pair<Double, Double> location, Double distance, Double bearing) {
    double latitude = Math.toRadians(location.first);
    double longitude = Math.toRadians(location.second);
    double angularDistance = distance / EARTH_RADIUS_METERS;
    double lastLatitude = latitude;


    latitude = Math.asin(Math.sin(latitude) * Math.cos(angularDistance) + Math.cos(latitude) * Math.sin(angularDistance) * Math.cos(bearing));
    longitude = longitude + Math.atan2(Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lastLatitude), Math.cos(angularDistance) - Math.sin(latitude) * Math.sin(lastLatitude));
    longitude = (longitude + 540) % 360 - 180;
    latitude = Math.toDegrees(latitude);
    longitude = Math.toDegrees(longitude);

    Log.i(LocationModifier.class.getName(), "New location is " + latitude + " " + longitude);

    return new Pair<>(latitude, longitude);
  }

  private static Double approximateLambertW(Double givenProbability) {
    if (givenProbability == -1 / Math.E) {
      return -1.0;
    }

    double last = 1.0;
    while (true) {
      double current = last - (last * Math.exp(last) - givenProbability) / ((last + 1) * Math.exp(last));
      if (Math.abs(current - last) < 1E-10) {
        return Math.round(current * 1E7) / 1E7;
      }
      last = current;
    }
  }
}
