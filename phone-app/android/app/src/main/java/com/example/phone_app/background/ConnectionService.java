package com.example.phone_app.background;

import android.util.Log;

import com.example.phone_app.background.serialization.NewDistanceMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ConnectionService {
  private final static String URL = "http://10.0.2.2:8080/";
  private final static Gson gson = new Gson();
  private final static OkHttpClient client = new OkHttpClient();
  private static final Type distanceListType = new TypeToken<ArrayList<NewDistanceMessage>>(){}.getType();

  private static void sendJson(String json, String endpoint) {
    Request request = new Request.Builder()
            .url(URL + endpoint)
            .post(RequestBody.create(json, MediaType.get("application/json")))
            .build();
    try {
      client.newCall(request).execute();
      Log.d(ConnectionService.class.getName(), "Finished sending the request to the server");
    } catch (IOException e) {
      Log.e(ConnectionService.class.getName(), e.toString());
    }
  }

  protected static List<NewDistanceMessage> getDistances(String userId) {
    Request request = new Request.Builder()
            .url(URL + "get-computed-distances/" + userId)
            .get()
            .build();
    Log.d(ConnectionService.class.getName(), "Url is " + request.url().toString());
    try {
      String jsonResponse = client.newCall(request).execute().body().string();
      Log.d(ConnectionService.class.getName(), "Received " + jsonResponse + " from server for new distances");
      return gson.fromJson(jsonResponse, distanceListType);
    } catch (IOException e) {
      Log.e(ConnectionService.class.getName(), e.toString());
    }
    return new ArrayList<>();
  }

  protected static void sendObject(Object givenObject, String endpoint) {
    sendJson(gson.toJson(givenObject), endpoint);
  }
}
