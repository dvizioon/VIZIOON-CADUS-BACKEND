package br.com.backend.cadus.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DataUrlsTest {

  @Test
  void parsesJpegDataUrl() {
    var parsed = DataUrls.parse("data:image/jpeg;base64,QQ==").orElseThrow();
    assertEquals("image/jpeg", parsed.contentType());
    assertEquals("jpg", parsed.extension());
    assertEquals(1, parsed.bytes().length);
  }

  @Test
  void parsesPdfDataUrl() {
    var parsed = DataUrls.parse("data:application/pdf;base64,QQ==").orElseThrow();
    assertEquals("application/pdf", parsed.contentType());
    assertEquals("pdf", parsed.extension());
  }

  @Test
  void detectsStoredPhotoUrl() {
    assertTrue(
        br.com.backend.cadus.modules.people.mapper.PersonMapper.looksStored(
            "http://localhost:9000/cadus/people/1/photo.jpg?X-Amz-Signature=abc"));
    assertTrue(
        br.com.backend.cadus.modules.people.mapper.PersonMapper.looksStored(
            "http://localhost:8080/api/people/marina-costa/photo"));
  }
}
