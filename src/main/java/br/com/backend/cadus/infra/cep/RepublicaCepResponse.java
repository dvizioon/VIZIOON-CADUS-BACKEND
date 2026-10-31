package br.com.backend.cadus.infra.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RepublicaCepResponse(
    String resultado,
    String uf,
    String cidade,
    String bairro,
    @JsonProperty("tipo_logradouro") String tipoLogradouro,
    String logradouro) {

  public boolean found() {
    return "1".equals(resultado) || "2".equals(resultado);
  }
}
