package dissertation.backend;

import com.google.gson.Gson;
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
  public Gson gson() {
    return new Gson();
  }
}
