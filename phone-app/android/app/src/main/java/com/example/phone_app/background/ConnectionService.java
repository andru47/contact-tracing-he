package com.example.phone_app.background;

import android.util.Log;

import com.example.phone_app.background.serialization.LocationUploadMessage;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ConnectionService {
  private final static String URL = "http://10.0.2.2:8080/upload-location";
  private final static Gson gson = new Gson();

  protected static void sendLocation(LocationUploadMessage bodyObject) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url(URL)
            .post(RequestBody.create(gson.toJson(bodyObject), MediaType.get("application/json")))
            .build();
    try {
      client.newCall(request).execute();
      Log.i("Http request sender", "Finished sending the request to the server");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
