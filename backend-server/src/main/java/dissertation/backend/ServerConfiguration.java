package dissertation.backend;

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
}
