package br.com.backend.cadus.modules.cep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.backend.cadus.infra.cep.RepublicaCepResponse;
import br.com.backend.cadus.infra.cep.RepublicaVirtualClient;
import br.com.backend.cadus.infra.cep.ViaCepClient;
import br.com.backend.cadus.infra.cep.ViaCepResponse;
import br.com.backend.cadus.modules.cep.dto.CepResponse;
import org.junit.jupiter.api.Test;

class CepServiceTest {

  @Test
  void fallsBackWhenViaCepMisses() {
    ViaCepClient viaCep = mock(ViaCepClient.class);
    RepublicaVirtualClient republica = mock(RepublicaVirtualClient.class);
    when(viaCep.fetch("65085065"))
        .thenReturn(new ViaCepResponse(null, null, null, null, null, "true"));
    when(republica.fetch("65085065"))
        .thenReturn(
            new RepublicaCepResponse(
                "1", "MA", "São Luís", "Anjo da Guarda", "Vila", "Mauro Fecury I"));

    CepResponse found = new CepService(viaCep, republica).lookup("65085-065");

    assertEquals("65085-065", found.cep());
    assertEquals("Vila Mauro Fecury I", found.address());
    assertEquals("Anjo da Guarda", found.district());
    assertEquals("São Luís", found.city());
    assertEquals("MA", found.uf());
  }
}
