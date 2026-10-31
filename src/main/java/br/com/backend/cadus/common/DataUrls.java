package br.com.backend.cadus.common;

import br.com.backend.cadus.common.exception.ApiException;
import java.util.Base64;
import java.util.Optional;

public final class DataUrls {

  private static final int MAX_BYTES = 8 * 1024 * 1024;

  public record Parsed(byte[] bytes, String contentType, String extension) {}

  private DataUrls() {}

  public static boolean isDataUrl(String value) {
    return value != null && value.startsWith("data:");
  }

  public static Optional<Parsed> parse(String value) {
    if (!isDataUrl(value)) {
      return Optional.empty();
    }
    int comma = value.indexOf(',');
    if (comma < 0) {
      throw ApiException.badRequest("Arquivo inválido");
    }
    String header = value.substring(5, comma);
    String payload = value.substring(comma + 1);
    String contentType = header.split(";")[0];
    if (contentType.isBlank()) {
      contentType = "application/octet-stream";
    }
    byte[] bytes;
    try {
      bytes = Base64.getDecoder().decode(payload);
    } catch (IllegalArgumentException exception) {
      throw ApiException.badRequest("Arquivo inválido");
    }
    if (bytes.length > MAX_BYTES) {
      throw ApiException.badRequest("Arquivo maior que 8 MB");
    }
    return Optional.of(new Parsed(bytes, contentType, extensionFor(contentType)));
  }

  public static String extensionFor(String contentType) {
    return switch (contentType) {
      case "image/jpeg", "image/jpg" -> "jpg";
      case "image/png" -> "png";
      case "image/webp" -> "webp";
      case "image/gif" -> "gif";
      case "application/pdf" -> "pdf";
      default -> "bin";
    };
  }
}
