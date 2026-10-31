package br.com.backend.cadus.modules.cep;

import br.com.backend.cadus.common.Cep;
import br.com.backend.cadus.common.exception.ApiException;
import br.com.backend.cadus.infra.cep.RepublicaCepResponse;
import br.com.backend.cadus.infra.cep.RepublicaVirtualClient;
import br.com.backend.cadus.infra.cep.ViaCepClient;
import br.com.backend.cadus.infra.cep.ViaCepResponse;
import br.com.backend.cadus.modules.cep.dto.CepResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CepService {

  private final ViaCepClient viaCep;
  private final RepublicaVirtualClient republica;

  public CepService(ViaCepClient viaCep, RepublicaVirtualClient republica) {
    this.viaCep = viaCep;
    this.republica = republica;
  }

  public CepResponse lookup(String cep) {
    String digits = Cep.requireEight(cep);
    CepResponse fromVia = fromViaCep(digits);
    if (fromVia != null) {
      return fromVia;
    }
    CepResponse fromRepublica = fromRepublica(digits);
    if (fromRepublica != null) {
      return fromRepublica;
    }
    throw ApiException.notFound("CEP não encontrado");
  }

  private CepResponse fromViaCep(String digits) {
    try {
      ViaCepResponse found = viaCep.fetch(digits);
      if (found == null || found.notFound() || blank(found.uf())) {
        return null;
      }
      return new CepResponse(
          blankTo(found.cep(), format(digits)),
          blankTo(found.logradouro(), ""),
          blankTo(found.bairro(), ""),
          blankTo(found.localidade(), ""),
          found.uf());
    } catch (ApiException exception) {
      if (exception.getStatus() == HttpStatus.BAD_REQUEST) {
        throw exception;
      }
      return null;
    }
  }

  private CepResponse fromRepublica(String digits) {
    try {
      RepublicaCepResponse found = republica.fetch(digits);
      if (found == null || !found.found() || blank(found.uf())) {
        return null;
      }
      String street = join(found.tipoLogradouro(), found.logradouro());
      return new CepResponse(
          format(digits), street, blankTo(found.bairro(), ""), blankTo(found.cidade(), ""), found.uf());
    } catch (ApiException exception) {
      return null;
    }
  }

  private static String join(String tipo, String logradouro) {
    String type = blankTo(tipo, "");
    String street = blankTo(logradouro, "");
    if (type.isEmpty()) {
      return street;
    }
    if (street.isEmpty()) {
      return type;
    }
    return type + " " + street;
  }

  private static String format(String digits) {
    return digits.substring(0, 5) + "-" + digits.substring(5);
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static String blankTo(String value, String fallback) {
    return blank(value) ? fallback : value;
  }
}
