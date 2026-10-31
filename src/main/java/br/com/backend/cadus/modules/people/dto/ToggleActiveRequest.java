package br.com.backend.cadus.modules.people.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ToggleActiveRequest(@JsonProperty("isActive") boolean isActive) {}
