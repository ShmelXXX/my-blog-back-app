package shm.yandex.practicum.service;

import org.springframework.stereotype.Service;
import shm.yandex.practicum.model.Comment;
import shm.yandex.practicum.repository.CommentRepository;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment addComment(Comment comment) {
        commentRepository.save(comment);
        return comment;
    }

    public void deleteComment(Long id) {
        commentRepository.delete(id);
    }


    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public Comment updateComment(Long commentId, Comment updatedComment) {
        Comment existingComment = commentRepository.findById(commentId);
        if (existingComment != null) {
            existingComment.setText(updatedComment.getText());
            commentRepository.update(existingComment);
        }
        return existingComment;
    }
}