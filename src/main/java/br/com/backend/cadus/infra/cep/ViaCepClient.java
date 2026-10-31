package br.com.backend.cadus.infra.cep;

import br.com.backend.cadus.common.exception.ApiException;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ViaCepClient {

  private final RestClient http;

  public ViaCepClient() {
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
    factory.setReadTimeout(Duration.ofSeconds(5));
    this.http =
        RestClient.builder()
            .baseUrl("https://viacep.com.br")
            .requestFactory(factory)
            .build();
  }

  public ViaCepResponse fetch(String digits) {
    try {
      return http.get()
          .uri("/ws/{cep}/json/", digits)
          .retrieve()
          .onStatus(
              status -> status.value() == 400,
              (request, response) -> {
                throw ApiException.badRequest("CEP inválido");
              })
          .body(ViaCepResponse.class);
    } catch (ApiException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "ViaCEP indisponível");
    }
  }
}
