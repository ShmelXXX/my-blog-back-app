package shm.yandex.practicum.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shm.yandex.practicum.model.Post;
import shm.yandex.practicum.service.MinioService;
import shm.yandex.practicum.service.PostService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/posts/{id}/image")
@CrossOrigin(origins = "http://localhost", allowCredentials = "true")
public class ImageController {

    private final PostService postService;
    private final MinioService minioService;
    private final Random random = new Random();

    public ImageController(PostService postService, MinioService minioService) {
        this.postService = postService;
        this.minioService = minioService;
    }

    // /api/posts/{id}/image - загрузка изображения поста
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @PathVariable("id") Long postId,
            @RequestParam("image") MultipartFile file) {
        try {

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No file found"));
            }

            Post post = postService.findById(postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }

            String fileName = minioService.generateFileName(
                    file.getOriginalFilename(),
                    postId
            );

            String savedFileName = minioService.uploadFile(file, fileName);

            // Удаляем старое изображение если есть
            if (post.getImageFileName() != null && !post.getImageFileName().isEmpty()) {
                try {
                    minioService.deleteFile(post.getImageFileName());
                } catch (Exception e) {
                    System.err.println("Warning: Could not delete old file: " + e.getMessage());
                }
            }

            post.setImageFileName(savedFileName);
            postService.update(post);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("fileName", savedFileName);
            response.put("postId", postId);

            try {
                String publicUrl = minioService.getPublicUrl(savedFileName);
                response.put("url", publicUrl);
            } catch (Exception e) {
                response.put("url", "Error generating URL: " + e.getMessage());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error uploading image: " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Upload failed");
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    // GET  /api/posts/{id}/image - получение изображения поста
    @GetMapping
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long postId) {
        try {
            Post post = postService.findById(postId);
            if (post == null || post.getImageFileName() == null) {
                return generateFallbackImage(postId);
            }

            try (InputStream inputStream = minioService.getFile(post.getImageFileName())) {
                byte[] bytes = inputStream.readAllBytes();
                String contentType = determineContentType(post.getImageFileName());

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + post.getImageFileName() + "\"")
                        .body(bytes);
            }

        } catch (Exception e) {
            System.err.println("Error getting image: " + e.getMessage());
            return generateFallbackImage(postId);
        }
    }

    // DELETE /api/posts/{id}/image - удаление изображения поста
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable("id") Long postId) {
        try {
            Post post = postService.findById(postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();

            if (post.getImageFileName() != null && !post.getImageFileName().isEmpty()) {
                try {
                    minioService.deleteFile(post.getImageFileName());
                    response.put("message", "Image deleted successfully");
                } catch (Exception e) {
                    response.put("warning", "File not found in MinIO: " + e.getMessage());
                }
                post.setImageFileName(null);
                postService.update(post);
            } else {
                response.put("message", "No image to delete");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error deleting image: " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Delete failed");
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    // генерация изображения по умолчанию
    private ResponseEntity<byte[]> generateFallbackImage(Long postId) {
        try {
            String svg = String.format(
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"400\" height=\"200\">" +
                            "<rect width=\"400\" height=\"200\" fill=\"#%06x\"/>" +
                            "<text x=\"50%%\" y=\"50%%\" text-anchor=\"middle\" dy=\".3em\" " +
                            "font-family=\"Arial\" font-size=\"24\" fill=\"white\">" +
                            "Post %d" +
                            "</text>" +
                            "</svg>",
                    random.nextInt(0xFFFFFF),
                    postId
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"post-" + postId + ".svg\"")
                    .body(svg.getBytes());

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Формируем content type по расширению файла
    private String determineContentType(String fileName) {
        if (fileName == null) {
            return "image/jpeg";
        }

        fileName = fileName.toLowerCase();

        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".bmp")) return "image/bmp";

        return "image/jpeg";
    }
}