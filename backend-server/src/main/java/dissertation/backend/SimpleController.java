package dissertation.backend;

import com.google.gson.Gson;
import dissertation.backend.database.ContactTracingHelper;
import dissertation.backend.database.Controller;
import dissertation.backend.serialization.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class SimpleController {
  @Autowired
  private ServerConfiguration configuration;

  @Autowired
  private JNIBridge jniBridge;

  @Autowired
  private Gson gson;

  @PostMapping("/distance-calculator")
  public String returnDistance(@RequestBody String jsonBody) {
    JSONBody body = gson.fromJson(jsonBody, JSONBody.class);
    //String[] fromDb = controller.getElement();
    return new String(jniBridge.getDistance(new char[][]{
        body.getLatitudeCos1().toCharArray(),
        body.getLatitudeSin1().toCharArray(),
        body.getLongitudeCos1().toCharArray(),
        body.getLongitudeSin1().toCharArray()
        //fromDb[0].toCharArray(),
        //fromDb[1].toCharArray(),
        //fromDb[2].toCharArray(),
        //fromDb[3].toCharArray()
    }, new char[][]{
        body.getLatitudeCos2().toCharArray(),
        body.getLatitudeSin2().toCharArray(),
        body.getLongitudeCos2().toCharArray(),
        body.getLongitudeSin2().toCharArray()
    }));
  }

  @PostMapping("/upload-location")
  public String uploadLocation(@RequestBody String jsonBody) {
    UploadDistanceMessage message = gson.fromJson(jsonBody, UploadDistanceMessage.class);
    Controller.uploadNewLocation(message);
    return "Location uploaded successfully";
  }

  @PostMapping("/upload-fcm-token")
  public String uploadFCMToken(@RequestBody String jsonBody) {
    NewTokenMessage tokenMessage = gson.fromJson(jsonBody, NewTokenMessage.class);
    Controller.updateTokenForUser(tokenMessage.getUserId(), tokenMessage.getToken());

    return "Token uploaded successfully";
  }

  @PostMapping("/report-positive-case")
  public String reportPositiveCase(@RequestBody String jsonInfectedDetails) {
    NewCaseMessage message = gson.fromJson(jsonInfectedDetails, NewCaseMessage.class);
    ContactTracingHelper.addNewCovidCase(message);
    return "SUCCESS";
  }

  @GetMapping("/get-computed-distances/{userId}")
  public String getDistances(@PathVariable String userId) {
    List<ComputedDistanceMessage> givenCiphertexts = ContactTracingHelper.getComputedDistancesForUser(userId);

    return gson.toJson(givenCiphertexts);
  }

  @PostMapping("/new-contact")
  public String reportNewContact(@RequestBody String jsonContactMessage) {
    ContactMessage message = gson.fromJson(jsonContactMessage, ContactMessage.class);
    Controller.addNewContact(message);
    return "SUCCESS";
  }

  private void saveKey(String name, String contents) {
    try {
      new FileWriter(name).write(contents);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @PostMapping("/new-keys")
  public String newKeys(@RequestBody String keys) throws IOException {
    Keys givenKeys = gson.fromJson(keys, Keys.class);
    Path pth = Paths.get("assets/lastpub.bin");
    byte[] bytes = givenKeys.getPubKey().getBytes(StandardCharsets.UTF_8);
    Files.write(pth, bytes);
    pth = Paths.get("assets/lastpriv.bin");
    bytes = givenKeys.getPrivateKey().getBytes(StandardCharsets.UTF_8);
    Files.write(pth, bytes);
    jniBridge.getAltitudeDifference(givenKeys.getRelinKey().toCharArray(), givenKeys.getPrivateKey().toCharArray());
    return "SUCCESS";
  }
}
