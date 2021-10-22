package dissertation.backend;

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

  @PostMapping("/compute-simple")
  public String returnSimpleMessage(@RequestBody String cipher) {
    String computedCipher = new String(jniBridge.computeSimplePoly(cipher.toCharArray()));
    return computedCipher;
  }

}
