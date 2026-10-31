package br.com.backend.cadus.infra.cep;

import br.com.backend.cadus.common.exception.ApiException;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RepublicaVirtualClient {

  private final RestClient http;

  public RepublicaVirtualClient() {
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
    factory.setReadTimeout(Duration.ofSeconds(5));
    this.http =
        RestClient.builder()
            .baseUrl("http://cep.republicavirtual.com.br")
            .requestFactory(factory)
            .build();
  }

  public RepublicaCepResponse fetch(String digits) {
    try {
      return http.get()
          .uri("/web_cep.php?cep={cep}&formato=json", digits)
          .retrieve()
          .body(RepublicaCepResponse.class);
    } catch (RestClientException exception) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Consulta de CEP indisponível");
    }
  }
}
