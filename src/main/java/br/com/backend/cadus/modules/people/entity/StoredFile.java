package br.com.backend.cadus.modules.people.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class StoredFile {

  @Column(length = 255)
  private String fileName;

  @Column(length = 512)
  private String objectKey;

  @Column(length = 500)
  private String description;
}
