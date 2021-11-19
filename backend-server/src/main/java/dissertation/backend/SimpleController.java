package dissertation.backend;

import com.google.gson.Gson;
import dissertation.backend.database.ContactTracingHelper;
import dissertation.backend.database.Controller;
import dissertation.backend.serialization.JSONBody;
import dissertation.backend.serialization.NewCaseMessage;
import dissertation.backend.serialization.NewTokenMessage;
import dissertation.backend.serialization.UploadDistanceMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    List<String> givenCiphertexts = ContactTracingHelper.getComputedDistancesForUser(userId);

    return gson.toJson(givenCiphertexts);
  }
}
