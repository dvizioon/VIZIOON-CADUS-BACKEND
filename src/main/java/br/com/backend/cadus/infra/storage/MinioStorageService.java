package br.com.backend.cadus.infra.storage;

import br.com.backend.cadus.common.exception.ApiException;
import br.com.backend.cadus.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class MinioStorageService implements StorageService {

  private final S3Client s3;
  private final S3Presigner presigner;
  private final StorageProperties properties;

  public MinioStorageService(S3Client s3, S3Presigner presigner, StorageProperties properties) {
    this.s3 = s3;
    this.presigner = presigner;
    this.properties = properties;
  }

  @PostConstruct
  void ensureBucket() {
    String bucket = properties.bucket();
    try {
      s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
    } catch (NoSuchBucketException exception) {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
    } catch (S3Exception exception) {
      if (exception.statusCode() == 404) {
        s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        return;
      }
      throw new ApiException(HttpStatus.BAD_GATEWAY, "MinIO indisponível");
    }
  }

  @Override
  public String put(String key, byte[] bytes, String contentType) {
    try {
      s3.putObject(
          PutObjectRequest.builder()
              .bucket(properties.bucket())
              .key(key)
              .contentType(contentType)
              .build(),
          RequestBody.fromBytes(bytes));
      return key;
    } catch (S3Exception exception) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Falha ao gravar arquivo no MinIO");
    }
  }

  @Override
  public ObjectData get(String key) {
    try {
      ResponseBytes<GetObjectResponse> object =
          s3.getObjectAsBytes(
              GetObjectRequest.builder().bucket(properties.bucket()).key(key).build());
      String contentType = object.response().contentType();
      return new ObjectData(
          object.asByteArray(),
          contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
    } catch (NoSuchKeyException exception) {
      throw ApiException.notFound("Arquivo não encontrado");
    } catch (S3Exception exception) {
      if (exception.statusCode() == 404) {
        throw ApiException.notFound("Arquivo não encontrado");
      }
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Falha ao ler arquivo no MinIO");
    }
  }

  @Override
  public void delete(String key) {
    if (key == null || key.isBlank()) {
      return;
    }
    try {
      s3.deleteObject(
          DeleteObjectRequest.builder().bucket(properties.bucket()).key(key).build());
    } catch (S3Exception exception) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Falha ao remover arquivo do MinIO");
    }
  }

  @Override
  public String url(String key) {
    if (key == null || key.isBlank()) {
      return null;
    }
    GetObjectPresignRequest request =
        GetObjectPresignRequest.builder()
            .signatureDuration(properties.urlTtl())
            .getObjectRequest(get -> get.bucket(properties.bucket()).key(key))
            .build();
    return presigner.presignGetObject(request).url().toString();
  }
}
