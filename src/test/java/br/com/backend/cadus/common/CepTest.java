package br.com.backend.cadus.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.backend.cadus.common.exception.ApiException;
import org.junit.jupiter.api.Test;

class CepTest {

  @Test
  void acceptsEightDigits() {
    assertEquals("01001000", Cep.requireEight("01001-000"));
  }

  @Test
  void rejectsWrongLength() {
    assertThrows(ApiException.class, () -> Cep.requireEight("950100100"));
    assertThrows(ApiException.class, () -> Cep.requireEight("95010A10"));
  }
}
