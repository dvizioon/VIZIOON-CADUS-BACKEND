package br.com.backend.cadus.modules.people;

import br.com.backend.cadus.common.exception.ApiException;
import br.com.backend.cadus.modules.people.dto.PersonRequest;
import br.com.backend.cadus.modules.people.dto.PersonResponse;
import br.com.backend.cadus.modules.people.dto.ToggleActiveRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/people")
public class PeopleController {

  private final PeopleService people;

  public PeopleController(PeopleService people) {
    this.people = people;
  }

  @GetMapping
  public List<PersonResponse> list(
      @RequestParam(required = false) String cpf,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) BigDecimal salaryMin,
      @RequestParam(required = false) BigDecimal salaryMax,
      @RequestParam(required = false) String contractStart,
      @RequestParam(required = false) String contractEnd) {
    return people.list(cpf, name, salaryMin, salaryMax, contractStart, contractEnd);
  }

  @GetMapping("/cargos")
  public List<String> cargos() {
    return people.cargos();
  }

  @GetMapping("/confirm/{token}")
  public ResponseEntity<String> confirm(@PathVariable String token) {
    try {
      String name = people.confirm(token);
      return html(HttpStatus.OK, "E-mail confirmado. O cadastro de " + name + " está ativo.");
    } catch (ApiException exception) {
      return html(exception.getStatus(), exception.getMessage());
    }
  }

  @GetMapping("/{id}")
  public PersonResponse get(@PathVariable String id) {
    return people.get(id);
  }

  @GetMapping("/{id}/photo")
  public ResponseEntity<byte[]> photo(@PathVariable String id) {
    return people.photo(id);
  }

  @GetMapping("/{id}/attachments/{kind}")
  public ResponseEntity<byte[]> attachment(@PathVariable String id, @PathVariable String kind) {
    return people.attachment(id, kind);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PersonResponse create(@Valid @RequestBody PersonRequest request) {
    PersonResponse created = people.create(request);
    people.sendConfirmation(created.id());
    return created;
  }

  @PutMapping("/{id}")
  public PersonResponse update(@PathVariable String id, @Valid @RequestBody PersonRequest request) {
    return people.update(id, request);
  }

  @PatchMapping("/{id}/active")
  public PersonResponse toggle(@PathVariable String id, @RequestBody ToggleActiveRequest request) {
    return people.toggle(id, request.isActive());
  }

  @PostMapping("/{id}/confirm-email")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resendConfirmation(@PathVariable String id) {
    people.resendConfirmation(id);
    people.sendConfirmation(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    people.delete(id);
  }

  @PostMapping("/{id}/photo")
  public PersonResponse uploadPhoto(
      @PathVariable String id, @RequestPart("file") MultipartFile file) {
    return people.uploadPhoto(id, file);
  }

  @PostMapping("/{id}/attachments/{kind}")
  public PersonResponse uploadAttachment(
      @PathVariable String id,
      @PathVariable String kind,
      @RequestPart("file") MultipartFile file,
      @RequestParam(required = false, defaultValue = "") String description) {
    return people.uploadAttachment(id, kind, file, description);
  }

  private static ResponseEntity<String> html(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
        .body(
            "<!doctype html><html lang=\"pt-BR\"><body style=\"font-family:sans-serif;padding:2rem\"><p>"
                + message
                + "</p></body></html>");
  }
}
