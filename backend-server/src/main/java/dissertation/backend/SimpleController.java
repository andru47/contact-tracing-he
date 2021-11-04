package dissertation.backend;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {
  @Autowired
  private ServerConfiguration configuration;

  @Autowired
  private JNIBridge jniBridge;

  @PostMapping("/distance-calculator")
  public String returnSimpleMessage(@RequestBody String jsonBody) {
    Gson gson = new Gson();
    JSONBody body = gson.fromJson(jsonBody, JSONBody.class);
    return new String(jniBridge.getDistance(body.getLatitudeCos().toCharArray(),
                                 body.getLatitudeSin().toCharArray(),
                                 body.getLongitudeCos().toCharArray(),
                                 body.getLongitudeSin().toCharArray(),
                                 body.getRelin().toCharArray(),
                                 body.getPrivateKey().toCharArray()));
  }
}
