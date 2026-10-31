package br.com.backend.cadus.modules.people.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record PersonResponse(
    String id,
    String cpf,
    String name,
    String cargo,
    BigDecimal salary,
    String contractStart,
    String contractEnd,
    String status,
    String photoUrl,
    String email,
    String phone,
    String address,
    String number,
    boolean noNumber,
    String uf,
    String city,
    String cep,
    String notes,
    @JsonProperty("isActive") boolean isActive,
    boolean emailConfirmed,
    AttachmentsDto attachments) {}
