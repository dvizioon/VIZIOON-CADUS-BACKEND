package br.com.backend.cadus.modules.cep;

import br.com.backend.cadus.modules.cep.dto.CepResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cep")
public class CepController {

  private final CepService cep;

  public CepController(CepService cep) {
    this.cep = cep;
  }

  @GetMapping("/{cep}")
  public CepResponse get(@PathVariable String cep) {
    return this.cep.lookup(cep);
  }
}
