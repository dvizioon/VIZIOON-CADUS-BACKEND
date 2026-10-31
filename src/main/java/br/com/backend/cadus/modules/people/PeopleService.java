package br.com.backend.cadus.modules.people;

import br.com.backend.cadus.common.Cpf;
import br.com.backend.cadus.common.DataUrls;
import br.com.backend.cadus.common.PersonCargos;
import br.com.backend.cadus.common.exception.ApiException;
import br.com.backend.cadus.infra.mail.ConfirmationMailService;
import br.com.backend.cadus.infra.storage.StorageService;
import br.com.backend.cadus.infra.storage.StorageService.ObjectData;
import br.com.backend.cadus.modules.people.dto.AttachmentDto;
import br.com.backend.cadus.modules.people.dto.AttachmentsDto;
import br.com.backend.cadus.modules.people.dto.PersonRequest;
import br.com.backend.cadus.modules.people.dto.PersonResponse;
import br.com.backend.cadus.modules.people.entity.PersonEntity;
import br.com.backend.cadus.modules.people.entity.StoredFile;
import br.com.backend.cadus.modules.people.mapper.PersonMapper;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PeopleService {

  private static final Set<String> PHOTO_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
  private static final Set<String> PDF_TYPE = Set.of("application/pdf");

  private final PeopleRepository people;
  private final PersonMapper mapper;
  private final StorageService storage;
  private final ConfirmationMailService confirmationMail;

  public PeopleService(
      PeopleRepository people,
      PersonMapper mapper,
      StorageService storage,
      ConfirmationMailService confirmationMail) {
    this.people = people;
    this.mapper = mapper;
    this.storage = storage;
    this.confirmationMail = confirmationMail;
  }

  public List<String> cargos() {
    return PersonCargos.ALL;
  }

  @Transactional(readOnly = true)
  public List<PersonResponse> list(
      String cpf,
      String name,
      BigDecimal salaryMin,
      BigDecimal salaryMax,
      String contractStart,
      String contractEnd) {
    String cpfDigits = blankToNull(Cpf.digits(cpf));
    String nameFilter = blankToNull(name);
    BigDecimal max = salaryMax != null && salaryMax.signum() == 0 ? null : salaryMax;
    LocalDate start = parseDate(contractStart);
    LocalDate end = parseDate(contractEnd);

    Specification<PersonEntity> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (cpfDigits != null) {
            predicates.add(cb.like(root.get("cpfDigits"), "%" + cpfDigits + "%"));
          }
          if (nameFilter != null) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + nameFilter.toLowerCase(Locale.ROOT) + "%"));
          }
          if (salaryMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), salaryMin));
          }
          if (max != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("salary"), max));
          }
          if (start != null) {
            predicates.add(
                cb.or(
                    cb.isNull(root.get("contractEnd")),
                    cb.greaterThanOrEqualTo(root.get("contractEnd"), start)));
          }
          if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("contractStart"), end));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    return people.findAll(spec, Sort.by("name")).stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PersonResponse get(String id) {
    return mapper.toResponse(require(id));
  }

  @Transactional
  public PersonResponse create(PersonRequest request) {
    validate(request);
    String digits = Cpf.digits(request.cpf());
    if (people.existsByCpfDigits(digits)) {
      throw ApiException.conflict("CPF já cadastrado");
    }
    PersonEntity entity = mapper.newEntity(null, request);
    entity.setEmailConfirmed(false);
    entity.setActive(false);
    entity.setConfirmationToken(UUID.randomUUID().toString());
    applyPhoto(entity, request.photoUrl());
    applyAttachmentMeta(entity, request.attachments());
    return mapper.toResponse(people.save(entity));
  }

  @Async
  public void sendConfirmation(String id) {
    PersonEntity entity = require(id);
    if (entity.isEmailConfirmed()) {
      return;
    }
    try {
      confirmationMail.send(entity);
    } catch (MailException ignored) {
      // same response either way: do not leak whether the mailbox exists
    }
  }

  @Transactional
  public void resendConfirmation(String id) {
    PersonEntity entity = require(id);
    if (entity.isEmailConfirmed()) {
      return;
    }
    if (entity.getConfirmationToken() == null || entity.getConfirmationToken().isBlank()) {
      entity.setConfirmationToken(UUID.randomUUID().toString());
      people.save(entity);
    }
  }

  @Transactional
  public String confirm(String token) {
    PersonEntity entity =
        people
            .findByConfirmationToken(token)
            .orElseThrow(() -> ApiException.notFound("Link inválido ou já usado"));
    entity.setEmailConfirmed(true);
    entity.setActive(true);
    entity.setConfirmationToken(null);
    people.save(entity);
    return entity.getName();
  }

  @Transactional
  public PersonResponse update(String id, PersonRequest request) {
    validate(request);
    PersonEntity entity = require(id);
    String digits = Cpf.digits(request.cpf());
    if (people.existsByCpfDigitsAndIdNot(digits, id)) {
      throw ApiException.conflict("CPF já cadastrado");
    }
    mapper.copy(entity, request);
    applyPhoto(entity, request.photoUrl());
    applyAttachmentMeta(entity, request.attachments());
    return mapper.toResponse(people.save(entity));
  }

  @Transactional
  public PersonResponse toggle(String id, boolean isActive) {
    PersonEntity entity = require(id);
    entity.setActive(isActive);
    return mapper.toResponse(people.save(entity));
  }

  @Transactional
  public void delete(String id) {
    PersonEntity entity = require(id);
    storage.delete(entity.getPhotoKey());
    deleteFile(entity.getRg());
    deleteFile(entity.getCpfDoc());
    deleteFile(entity.getResidence());
    people.delete(entity);
  }

  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> photo(String id) {
    PersonEntity entity = require(id);
    if (entity.getPhotoKey() != null && !entity.getPhotoKey().isBlank()) {
      ObjectData file = storage.get(entity.getPhotoKey());
      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(file.contentType()))
          .cacheControl(CacheControl.noStore())
          .header("Cross-Origin-Resource-Policy", "cross-origin")
          .body(file.bytes());
    }
    if (entity.getPhotoUrl() != null && !entity.getPhotoUrl().isBlank()) {
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(entity.getPhotoUrl())).build();
    }
    throw ApiException.notFound("Foto não encontrada");
  }

  @Transactional
  public PersonResponse uploadPhoto(String id, MultipartFile file) {
    PersonEntity entity = require(id);
    putPhoto(entity, bytesOf(file, PHOTO_TYPES, "Envie uma imagem JPG, PNG ou WEBP"), contentType(file));
    return mapper.toResponse(people.save(entity));
  }

  @Transactional
  public PersonResponse uploadAttachment(String id, String kind, MultipartFile file, String description) {
    PersonEntity entity = require(id);
    StoredFile stored = fileOf(entity, kind);
    replaceFile(
        stored,
        "people/%s/%s.pdf".formatted(entity.getId(), kind),
        bytesOf(file, PDF_TYPE, "Envie um PDF"),
        "application/pdf",
        file.getOriginalFilename(),
        description);
    return mapper.toResponse(people.save(entity));
  }

  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> attachment(String id, String kind) {
    StoredFile stored = fileOf(require(id), kind);
    if (stored.getObjectKey() == null || stored.getObjectKey().isBlank()) {
      throw ApiException.notFound("Anexo não encontrado");
    }
    ObjectData file = storage.get(stored.getObjectKey());
    String fileName = stored.getFileName() == null ? kind + ".pdf" : stored.getFileName().replace("\"", "");
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
        .cacheControl(CacheControl.noStore())
        .header("Cross-Origin-Resource-Policy", "cross-origin")
        .body(file.bytes());
  }

  private void validate(PersonRequest request) {
    if (!Cpf.isValid(request.cpf())) {
      throw ApiException.badRequest("CPF inválido");
    }
    if (!PersonCargos.ALL.contains(request.cargo())) {
      throw ApiException.badRequest("Selecione o cargo");
    }
    if (Cpf.digits(request.cep()).length() != 8) {
      throw ApiException.badRequest("CEP deve ter 8 dígitos");
    }
    String phone = Cpf.digits(request.phone());
    if (!phone.isEmpty() && phone.length() != 10 && phone.length() != 11) {
      throw ApiException.badRequest("Telefone deve ter 10 ou 11 dígitos");
    }
    if (!request.noNumber()) {
      String number = request.number() == null ? "" : request.number().trim();
      if (!number.matches("\\d+")) {
        throw ApiException.badRequest("Informe um número válido");
      }
    }
    LocalDate start = parseRequiredDate(request.contractStart(), "Informe a data inicial");
    if (request.contractEnd() != null && !request.contractEnd().isBlank()) {
      LocalDate end = parseRequiredDate(request.contractEnd(), "Informe a data final");
      if (end.isBefore(start)) {
        throw ApiException.badRequest("A data final deve ser igual ou posterior à inicial.");
      }
    }
  }

  private void applyPhoto(PersonEntity entity, String photoUrl) {
    if (DataUrls.isDataUrl(photoUrl)) {
      DataUrls.Parsed parsed =
          DataUrls.parse(photoUrl).orElseThrow(() -> ApiException.badRequest("Arquivo inválido"));
      if (!PHOTO_TYPES.contains(parsed.contentType())) {
        throw ApiException.badRequest("Envie uma imagem JPG, PNG ou WEBP");
      }
      putPhoto(entity, parsed.bytes(), parsed.contentType());
      return;
    }
    if (photoUrl == null) {
      storage.delete(entity.getPhotoKey());
      entity.setPhotoKey(null);
      entity.setPhotoUrl(null);
      return;
    }
    if (PersonMapper.looksStored(photoUrl) || entity.getPhotoKey() != null) {
      return;
    }
    entity.setPhotoUrl(photoUrl);
    entity.setPhotoKey(null);
  }

  private void putPhoto(PersonEntity entity, byte[] bytes, String contentType) {
    String extension = DataUrls.extensionFor(contentType);
    String key = "people/%s/photo.%s".formatted(entity.getId(), extension);
    storage.delete(entity.getPhotoKey());
    entity.setPhotoKey(storage.put(key, bytes, contentType));
    entity.setPhotoUrl(null);
  }

  private void applyAttachmentMeta(PersonEntity entity, AttachmentsDto attachments) {
    if (attachments == null) {
      return;
    }
    applyMeta(entity, entity.getRg(), "rg", attachments.rg());
    applyMeta(entity, entity.getCpfDoc(), "cpfDoc", attachments.cpfDoc());
    applyMeta(entity, entity.getResidence(), "residence", attachments.residence());
  }

  private void applyMeta(PersonEntity entity, StoredFile stored, String kind, AttachmentDto incoming) {
    if (incoming == null) {
      return;
    }
    stored.setDescription(incoming.description() == null ? "" : incoming.description());
    if (DataUrls.isDataUrl(incoming.dataUrl())) {
      DataUrls.Parsed parsed =
          DataUrls.parse(incoming.dataUrl()).orElseThrow(() -> ApiException.badRequest("PDF inválido"));
      if (!PDF_TYPE.contains(parsed.contentType())) {
        throw ApiException.badRequest("Envie um PDF");
      }
      replaceFile(
          stored,
          "people/%s/%s.pdf".formatted(entity.getId(), kind),
          parsed.bytes(),
          "application/pdf",
          incoming.fileName(),
          incoming.description());
      return;
    }
    if (incoming.fileName() == null) {
      storage.delete(stored.getObjectKey());
      stored.setObjectKey(null);
      stored.setFileName(null);
      return;
    }
    stored.setFileName(incoming.fileName());
  }

  private void replaceFile(
      StoredFile stored,
      String key,
      byte[] bytes,
      String contentType,
      String fileName,
      String description) {
    storage.delete(stored.getObjectKey());
    stored.setObjectKey(storage.put(key, bytes, contentType));
    stored.setFileName(fileName);
    if (description != null) {
      stored.setDescription(description);
    }
  }

  private void deleteFile(StoredFile file) {
    if (file != null) {
      storage.delete(file.getObjectKey());
    }
  }

  private StoredFile fileOf(PersonEntity entity, String kind) {
    return switch (kind) {
      case "rg" -> entity.getRg();
      case "cpfDoc" -> entity.getCpfDoc();
      case "residence" -> entity.getResidence();
      default -> throw ApiException.badRequest("Anexo inválido");
    };
  }

  private PersonEntity require(String id) {
    return people.findById(id).orElseThrow(() -> ApiException.notFound("Cadastro não encontrado"));
  }

  private static byte[] bytesOf(MultipartFile file, Set<String> allowed, String message) {
    if (file == null || file.isEmpty()) {
      throw ApiException.badRequest(message);
    }
    String type = contentType(file);
    if (!allowed.contains(type)) {
      throw ApiException.badRequest(message);
    }
    try {
      return file.getBytes();
    } catch (Exception exception) {
      throw ApiException.badRequest("Falha ao ler o arquivo");
    }
  }

  private static String contentType(MultipartFile file) {
    String type = file.getContentType();
    return type == null ? "" : type.toLowerCase(Locale.ROOT);
  }

  private static LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return parseRequiredDate(value, "Data inválida");
  }

  private static LocalDate parseRequiredDate(String value, String message) {
    try {
      return LocalDate.parse(value);
    } catch (DateTimeException | NullPointerException exception) {
      throw ApiException.badRequest(message);
    }
  }

  private static String blankToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
