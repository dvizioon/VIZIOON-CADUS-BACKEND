package br.com.backend.cadus.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfTest {

  @Test
  void acceptsKnownCpf() {
    assertTrue(Cpf.isValid("529.982.247-25"));
  }

  @Test
  void rejectsRepeatedDigits() {
    assertFalse(Cpf.isValid("000.000.000-00"));
  }
}
