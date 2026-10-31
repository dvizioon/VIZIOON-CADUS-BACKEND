package br.com.backend.cadus.modules.people.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PersonRequest(
    @NotBlank String cpf,
    @NotBlank @Size(min = 3, max = 80) String name,
    @NotBlank String cargo,
    @NotNull @DecimalMin("0") BigDecimal salary,
    @NotBlank String contractStart,
    String contractEnd,
    @NotBlank @Email String email,
    String phone,
    @NotBlank String address,
    String number,
    boolean noNumber,
    @NotBlank @Size(min = 2, max = 2) String uf,
    @NotBlank String city,
    @NotBlank String cep,
    @Size(max = 500) String notes,
    @JsonProperty("isActive") boolean isActive,
    String photoUrl,
    AttachmentsDto attachments) {

  public String cpfDigits() {
    return cpf == null ? "" : cpf.replaceAll("\\D", "");
  }
}
