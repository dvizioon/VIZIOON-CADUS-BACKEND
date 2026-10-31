package br.com.backend.cadus.modules.people.mapper;

import br.com.backend.cadus.modules.people.dto.AttachmentDto;
import br.com.backend.cadus.modules.people.dto.AttachmentsDto;
import br.com.backend.cadus.modules.people.dto.PersonRequest;
import br.com.backend.cadus.modules.people.dto.PersonResponse;
import br.com.backend.cadus.modules.people.entity.PersonEntity;
import br.com.backend.cadus.modules.people.entity.StoredFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

  private final String publicUrl;

  public PersonMapper(@Value("${app.public-url:http://localhost:8080}") String publicUrl) {
    this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
  }

  public PersonEntity newEntity(String id, PersonRequest request) {
    PersonEntity entity = new PersonEntity();
    entity.setId(id == null ? UUID.randomUUID().toString() : id);
    copy(entity, request);
    return entity;
  }

  public void copy(PersonEntity entity, PersonRequest request) {
    entity.setCpfDigits(request.cpfDigits());
    entity.setCpf(request.cpf());
    entity.setName(request.name().trim());
    entity.setCargo(request.cargo());
    entity.setSalary(request.salary() == null ? BigDecimal.ZERO : request.salary());
    entity.setContractStart(LocalDate.parse(request.contractStart()));
    entity.setContractEnd(
        request.contractEnd() == null || request.contractEnd().isBlank()
            ? null
            : LocalDate.parse(request.contractEnd()));
    entity.setEmail(request.email().trim());
    entity.setPhone(blankToEmpty(request.phone()));
    entity.setAddress(request.address().trim());
    entity.setNoNumber(request.noNumber());
    entity.setNumber(request.noNumber() ? "--" : blankToEmpty(request.number()).trim());
    entity.setUf(request.uf().toUpperCase());
    entity.setCity(request.city());
    entity.setCep(request.cep());
    entity.setNotes(blankToEmpty(request.notes()));
    entity.setActive(request.isActive());
  }

  public PersonResponse toResponse(PersonEntity entity) {
    boolean active = entity.isActive();
    return new PersonResponse(
        entity.getId(),
        entity.getCpf(),
        entity.getName(),
        entity.getCargo(),
        entity.getSalary(),
        entity.getContractStart().toString(),
        entity.getContractEnd() == null ? "" : entity.getContractEnd().toString(),
        active ? "active" : "inactive",
        photoUrl(entity),
        entity.getEmail(),
        blankToEmpty(entity.getPhone()),
        entity.getAddress(),
        entity.getNumber(),
        entity.isNoNumber(),
        entity.getUf(),
        entity.getCity(),
        entity.getCep(),
        blankToEmpty(entity.getNotes()),
        active,
        entity.isEmailConfirmed(),
        new AttachmentsDto(
            toDto(entity, "rg", entity.getRg()),
            toDto(entity, "cpfDoc", entity.getCpfDoc()),
            toDto(entity, "residence", entity.getResidence())));
  }

  public static boolean looksStored(String photoUrl) {
    if (photoUrl == null) {
      return false;
    }
    return photoUrl.contains("X-Amz-")
        || photoUrl.contains("/cadus/")
        || photoUrl.contains("/api/people/");
  }

  public String photoUrl(PersonEntity entity) {
    if (hasPhoto(entity)) {
      return publicUrl + "/api/people/" + entity.getId() + "/photo";
    }
    return null;
  }

  private static boolean hasPhoto(PersonEntity entity) {
    return (entity.getPhotoKey() != null && !entity.getPhotoKey().isBlank())
        || (entity.getPhotoUrl() != null && !entity.getPhotoUrl().isBlank());
  }

  private AttachmentDto toDto(PersonEntity entity, String kind, StoredFile file) {
    StoredFile value = file == null ? new StoredFile() : file;
    String url =
        value.getObjectKey() == null || value.getObjectKey().isBlank()
            ? null
            : publicUrl + "/api/people/" + entity.getId() + "/attachments/" + kind;
    return new AttachmentDto(value.getFileName(), blankToEmpty(value.getDescription()), url, null);
  }

  private static String blankToEmpty(String value) {
    return value == null ? "" : value;
  }
}
