package dissertation.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {
  @Autowired
  private ServerConfiguration configuration;

  @Autowired
  private JNIBridge jniBridge;

  @GetMapping("/hello/{name}")
  public String returnSimpleMessage(@PathVariable String name) {
    return jniBridge.getGreetingMessage(name);
  }

}
