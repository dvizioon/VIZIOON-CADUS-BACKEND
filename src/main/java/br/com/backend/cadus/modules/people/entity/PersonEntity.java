package br.com.backend.cadus.modules.people.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "people")
public class PersonEntity {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, unique = true, length = 11)
  private String cpfDigits;

  @Column(nullable = false, length = 14)
  private String cpf;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(nullable = false, length = 80)
  private String cargo;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal salary;

  @Column(nullable = false)
  private LocalDate contractStart;

  private LocalDate contractEnd;

  @Column(length = 512)
  private String photoKey;

  @Column(length = 1024)
  private String photoUrl;

  @Column(nullable = false, length = 180)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(nullable = false, length = 180)
  private String address;

  @Column(nullable = false, length = 20)
  private String number;

  @Column(nullable = false)
  private boolean noNumber;

  @Column(nullable = false, length = 2)
  private String uf;

  @Column(nullable = false, length = 80)
  private String city;

  @Column(nullable = false, length = 9)
  private String cep;

  @Column(length = 500)
  private String notes;

  @Column(nullable = false)
  private boolean active;

  @Column(nullable = false, columnDefinition = "boolean not null default false")
  private boolean emailConfirmed;

  @Column(length = 36, unique = true)
  private String confirmationToken;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "fileName", column = @Column(name = "rg_file_name")),
    @AttributeOverride(name = "objectKey", column = @Column(name = "rg_object_key")),
    @AttributeOverride(name = "description", column = @Column(name = "rg_description", length = 500))
  })
  private StoredFile rg = new StoredFile();

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "fileName", column = @Column(name = "cpf_doc_file_name")),
    @AttributeOverride(name = "objectKey", column = @Column(name = "cpf_doc_object_key")),
    @AttributeOverride(
        name = "description",
        column = @Column(name = "cpf_doc_description", length = 500))
  })
  private StoredFile cpfDoc = new StoredFile();

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "fileName", column = @Column(name = "residence_file_name")),
    @AttributeOverride(name = "objectKey", column = @Column(name = "residence_object_key")),
    @AttributeOverride(
        name = "description",
        column = @Column(name = "residence_description", length = 500))
  })
  private StoredFile residence = new StoredFile();

  @PostLoad
  void embeddables() {
    if (rg == null) {
      rg = new StoredFile();
    }
    if (cpfDoc == null) {
      cpfDoc = new StoredFile();
    }
    if (residence == null) {
      residence = new StoredFile();
    }
  }

  public void setAttachmentDescriptions(String rgDescription, String cpfDocDescription, String residenceDescription) {
    embeddables();
    rg.setDescription(rgDescription);
    cpfDoc.setDescription(cpfDocDescription);
    residence.setDescription(residenceDescription);
  }
}
