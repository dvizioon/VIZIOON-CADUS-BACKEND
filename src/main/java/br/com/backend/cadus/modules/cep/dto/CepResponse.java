package br.com.backend.cadus.modules.cep.dto;

public record CepResponse(String cep, String address, String district, String city, String uf) {}
