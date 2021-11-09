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
    return new String(jniBridge.getDistance(new char[][] {
            body.getLatitudeCos1().toCharArray(),
            body.getLatitudeSin1().toCharArray(),
            body.getLongitudeCos1().toCharArray(),
            body.getLongitudeSin1().toCharArray()
        }, new char[][] {
            body.getLatitudeCos2().toCharArray(),
            body.getLatitudeSin2().toCharArray(),
            body.getLongitudeCos2().toCharArray(),
            body.getLongitudeSin2().toCharArray()
        },
        body.getPrivateKey().toCharArray()));
  }
}
