package br.com.backend.cadus.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ApplicationPropertiesSecretsTest {

  @Test
  void secretsArePlaceholders() throws Exception {
    Properties props = new Properties();
    try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
      assertNotNull(in);
      props.load(in);
    }
    for (String key :
        new String[] {
          "spring.mail.username",
          "spring.mail.password",
          "app.mail.from",
          "app.minio.access-key",
          "app.minio.secret-key"
        }) {
      String value = props.getProperty(key);
      assertTrue(value != null && value.startsWith("${"), key);
    }
  }
}
