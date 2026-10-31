package br.com.backend.cadus.infra.storage;

public interface StorageService {

  record ObjectData(byte[] bytes, String contentType) {}

  String put(String key, byte[] bytes, String contentType);

  ObjectData get(String key);

  void delete(String key);

  String url(String key);
}
