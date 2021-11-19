package com.example.phone_app.background;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
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

  private static void sendJson(String json, String endpoint) {
    Request request = new Request.Builder()
            .url(URL + endpoint)
            .post(RequestBody.create(json, MediaType.get("application/json")))
            .build();
    try {
      client.newCall(request).execute();
      Log.i("Http request sender", "Finished sending the request to the server");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected static List<String> getDistances(String userId) {
    Request request = new Request.Builder()
            .url(URL + "get-computed-distances/" + userId)
            .get()
            .build();
    Log.i(ConnectionService.class.getName(), "Url is " + request.url().toString());
    try {
      String jsonResponse = client.newCall(request).execute().body().string();
      Log.i(ConnectionService.class.getName(), "Received " + jsonResponse + " from server for new distances");
      return gson.fromJson(jsonResponse, ArrayList.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  protected static void sendObject(Object givenObject, String endpoint) {
    sendJson(gson.toJson(givenObject), endpoint);
  }
}
