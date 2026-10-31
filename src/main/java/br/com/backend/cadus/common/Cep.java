package br.com.backend.cadus.common;

import br.com.backend.cadus.common.exception.ApiException;

public final class Cep {

  private Cep() {}

  public static String digits(String value) {
    return Cpf.digits(value);
  }

  public static String requireEight(String value) {
    String digits = digits(value);
    if (digits.length() != 8) {
      throw ApiException.badRequest("CEP deve ter 8 dígitos");
    }
    return digits;
  }
}
