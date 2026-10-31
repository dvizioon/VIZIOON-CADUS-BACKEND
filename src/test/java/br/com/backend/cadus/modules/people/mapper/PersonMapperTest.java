package br.com.backend.cadus.modules.people.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.com.backend.cadus.modules.people.dto.PersonRequest;
import br.com.backend.cadus.modules.people.entity.PersonEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PersonMapperTest {

  private final PersonMapper mapper = new PersonMapper("http://localhost:8080");

  @Test
  void blankContractEndIsIndefinite() {
    PersonEntity entity = mapper.newEntity("id", request(""));
    assertNull(entity.getContractEnd());
    assertEquals("", mapper.toResponse(entity).contractEnd());
    assertEquals("52998224725", entity.getCpfDigits());
  }

  @Test
  void filledContractEndIsKept() {
    PersonEntity entity = mapper.newEntity("id", request("2004-01-13"));
    assertEquals("2004-01-13", entity.getContractEnd().toString());
  }

  private static PersonRequest request(String contractEnd) {
    return new PersonRequest(
        "529.982.247-25",
        "Marina Costa",
        "Operadora de terminal",
        BigDecimal.ZERO,
        "1990-02-13",
        contractEnd,
        "marina@example.com",
        "",
        "Rua do Porto",
        "--",
        true,
        "CE",
        "Fortaleza",
        "60000-000",
        "",
        false,
        null,
        null);
  }
}
