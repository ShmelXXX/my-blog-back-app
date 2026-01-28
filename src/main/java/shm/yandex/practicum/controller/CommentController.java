package shm.yandex.practicum.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{id}/comments")
@CrossOrigin(origins = "http://localhost", allowCredentials = "true")
public class CommentController {
    @GetMapping
    public String getComments(@PathVariable("id") Long postId) {
        return "Затычка для комментариев для поста " + postId;
    }
}