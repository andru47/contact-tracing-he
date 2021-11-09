package dissertation.backend;

import com.google.gson.Gson;
import dissertation.backend.database.Controller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class ServerConfiguration {
  @Bean
  public JNIBridge jniBridge() {
    return new JNIBridge();
  }

  @Bean
  public Controller controller() {
    return new Controller();
  }

  @Bean
  public Gson gson() {
    return new Gson();
  }
}
