package shm.yandex.practicum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shm.yandex.practicum.model.Post;
import shm.yandex.practicum.model.PostsPage;
import shm.yandex.practicum.service.PostService;

@RestController
@RequestMapping("/api/posts")

@CrossOrigin(origins = "http://localhost", allowCredentials = "true")

public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // GET /api/posts - получение списка постов
@GetMapping
    public PostsPage getPosts(
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        return postService.findPostsBySearchWithPagination(search, pageNumber, pageSize);
    }

    // GET /api/posts/{id} - получение поста по id
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable("id") Long id) {
        return postService.findById(id);
    }


    // DELETE /api/posts/{id} - удаление поста по id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        try {
            Post post = postService.findById(id);
            if (post == null) {
                System.err.println("Post not found with id: " + id);
                return ResponseEntity.notFound().build();
            }

            postService.deletePost(id);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error deleting post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/posts - создание поста из JSON
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        try {
            if (post.getLikesCount() == null) {
                post.setLikesCount(0);
            }
            if (post.getCommentsCount() == null) {
                post.setCommentsCount(0);
            }

            postService.save(post);

//            System.out.println("Post created successfully with ID: " + post.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(post);

        } catch (Exception e) {
            System.err.println("Error creating post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/posts/{id}  - обновление поста
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable("id") Long id,
            @RequestBody Post updatedPost) {

        try {
            // Проверяем существование поста
            Post existingPost = postService.findById(id);
            if (existingPost == null) {
                return ResponseEntity.notFound().build();
            }

            // Обновляем поля
            existingPost.setTitle(updatedPost.getTitle());
            existingPost.setText(updatedPost.getText());
            existingPost.setTags(updatedPost.getTags());

            postService.update(existingPost);

            return ResponseEntity.ok(existingPost);

        } catch (Exception e) {
            System.err.println("Error updating post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/posts/{id}/likes"} - изменение количества лайков
    @PostMapping("/{id}/likes")
    public ResponseEntity<Post> likePost(@PathVariable("id") Long id) {
        try {
            Post post = postService.findById(id);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }

            Integer currentLikes = post.getLikesCount();
            if (currentLikes == null) {
                currentLikes = 0;
            }
            post.setLikesCount(currentLikes + 1);

            postService.update(post);

            return ResponseEntity.ok(post);
        } catch (Exception e) {
            System.err.println("Error liking post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
