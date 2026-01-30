package shm.yandex.practicum.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket-name}")
    public String bucketName;

    private final MinioClient minioClient;
    private volatile boolean bucketInitialized = false;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() {
        System.out.println("=== Initializing MinioService ===");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Bucket: " + bucketName);
        System.out.println("================================");

        try {
            ensureBucketExists();
            System.out.println("✓ MinIO bucket '" + bucketName + "' is ready");
        } catch (Exception e) {
            System.err.println("✗ Warning: Could not initialize MinIO bucket: " + e.getMessage());
        }
    }

    /**
     * Создает bucket если он не существует
     */
    private synchronized void ensureBucketExists() {
        if (bucketInitialized) {
            return;
        }

        try {
            System.out.println("Checking MinIO bucket: " + bucketName);

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                System.out.println("Creating bucket: " + bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                System.out.println("✓ Bucket created: " + bucketName);
            } else {
                System.out.println("✓ Bucket already exists: " + bucketName);
            }

            bucketInitialized = true;

        } catch (Exception e) {
            System.err.println("✗ ERROR: Failed to initialize MinIO bucket: " + e.getMessage());
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }

    /**
     * Загружает файл в MinIO
     */
    public String uploadFile(MultipartFile file, String fileName) throws Exception {
        ensureBucketExists();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            System.out.println("✓ File uploaded to MinIO: " + fileName);
            return fileName;
        }
    }

    /**
     * Получает файл из MinIO
     */
    public InputStream getFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * Удаляет файл из MinIO
     */
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
        System.out.println("✓ File deleted from MinIO: " + fileName);
    }

    /**
     * Генерирует публичный URL для файла
     */
    public String getPublicUrl(String fileName) {
        try {
            // endpoint уже содержит полный URL (например, "http://localhost:9000")
            return String.format("%s/%s/%s", endpoint, bucketName, fileName);

        } catch (Exception e) {
            System.err.println("✗ Error generating public URL: " + e.getMessage());
            throw new RuntimeException("Failed to generate public URL", e);
        }
    }

    /**
     * Генерирует presigned URL (временную ссылку)
     */
    public String getPresignedUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS) // Срок действия 7 дней
                            .build()
            );
        } catch (Exception e) {
            System.err.println("✗ Error generating presigned URL: " + e.getMessage());
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Генерирует уникальное имя файла
     */
    public String generateFileName(String originalFilename, Long postId) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "image.jpg";
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        } else {
            extension = ".jpg";
        }

        // Создаем безопасное имя файла
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("post_%d_%s%s", postId, timestamp, extension);
    }

    /**
     * Проверяет, существует ли файл
     */
    public boolean fileExists(String fileName) throws Exception {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Получает метаданные файла
     */
    public Map<String, String> getFileMetadata(String fileName) throws Exception {
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );

        Map<String, String> metadata = new HashMap<>();
        metadata.put("contentType", stat.contentType());
        metadata.put("size", String.valueOf(stat.size()));
        metadata.put("lastModified", stat.lastModified() != null ? stat.lastModified().toString() : "");
        metadata.put("etag", stat.etag());

        return metadata;
    }

    /**
     * Проверяет подключение к MinIO
     */
    public boolean testConnection() {
        try {
            ensureBucketExists();
            return true;
        } catch (Exception e) {
            System.err.println("✗ MinIO connection test failed: " + e.getMessage());
            return false;
        }
    }
}