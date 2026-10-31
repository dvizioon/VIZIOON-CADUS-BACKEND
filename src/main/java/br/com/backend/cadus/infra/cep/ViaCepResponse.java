package br.com.backend.cadus.infra.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
    String cep,
    String logradouro,
    String bairro,
    String localidade,
    String uf,
    String erro) {

  public boolean notFound() {
    return "true".equalsIgnoreCase(erro);
  }
}
