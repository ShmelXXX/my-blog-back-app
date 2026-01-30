package shm.yandex.practicum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shm.yandex.practicum.model.Comment;
import shm.yandex.practicum.service.CommentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@CrossOrigin(origins = "http://localhost", allowCredentials = "true")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // GET /api/posts/{postId}/comments - получение всех комментариев поста
    @GetMapping
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable("postId") String postIdString) {
        try {

            // Заглушка для "undefined". Проблема Frontend
            if ("undefined".equalsIgnoreCase(postIdString)) {
                System.err.println("Received 'undefined' as post ID. Check frontend.");
                List<Comment> testComments = new ArrayList<>();
                return ResponseEntity.ok(testComments);
            }

            long postId;
            try {
                postId = Long.parseLong(postIdString);
            } catch (NumberFormatException e) {
                System.err.println("Invalid post ID format: " + postIdString);
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            List<Comment> comments = commentService.getCommentsByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            System.err.println("Error getting comments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // POST /api/posts/{postId}/comments - создание нового комментария
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable("postId") Long postId,
            @RequestBody Comment comment) {
        try {
            if (comment.getText() == null || comment.getText().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            comment.setPostId(postId);
            Comment savedComment = commentService.addComment(comment);

            return ResponseEntity.ok(savedComment);

        } catch (Exception e) {
            System.err.println("Error adding comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody Comment updatedComment) {
        try {
            // Сначала проверяем существование комментария
            Comment existingComment = commentService.getCommentById(commentId);
            if (existingComment == null) {
                return ResponseEntity.notFound().build();
            }

            // Проверяем, что комментарий принадлежит нужному посту
            // Отключено до корректировки Frontend. Вместо id-поста дублируется id комментария
//            if (!existingComment.getPostId().equals(postId)) {
//                return ResponseEntity.badRequest().build();
//            }

            existingComment.setText(updatedComment.getText());

            Comment savedComment = commentService.updateComment(commentId, existingComment);
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            System.err.println("Error updating comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE /api/posts/{postId}/comments/{commentId} - удаление комментария
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
