package shm.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shm.yandex.practicum.model.Comment;
import shm.yandex.practicum.repository.CommentRepository;
import shm.yandex.practicum.service.CommentService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private List<Comment> testComments;

    @BeforeEach
    void setUp() {
        testComment = new Comment(1L, "Test comment", 10L);
        testComments = Arrays.asList(
                new Comment(1L, "First comment", 10L),
                new Comment(2L, "Second comment", 10L),
                new Comment(3L, "Third comment", 10L)
        );
    }

    @Test
    void testGetCommentsByPostId() {
        // Given
        Long postId = 10L;
        when(commentRepository.findByPostId(postId)).thenReturn(testComments);

        // When
        List<Comment> result = commentService.getCommentsByPostId(postId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("First comment", result.get(0).getText());
        assertEquals(10L, result.get(0).getPostId());
        verify(commentRepository, times(1)).findByPostId(postId);
    }

    @Test
    void testGetCommentsByPostId_EmptyList() {
        // Given
        Long postId = 999L;
        when(commentRepository.findByPostId(postId)).thenReturn(Arrays.asList());

        // When
        List<Comment> result = commentService.getCommentsByPostId(postId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1)).findByPostId(postId);
    }

    @Test
    void testAddComment() {
        // Given
        Comment newComment = new Comment(null, "New comment", 5L);

        // Настраиваем mock: сохраняем переданный комментарий и возвращаем его же
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);  // Устанавливаем ID в переданный объект
            return comment;
        });

        // When
        Comment result = commentService.addComment(newComment);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New comment", result.getText());
        assertEquals(5L, result.getPostId());
        verify(commentRepository, times(1)).save(newComment);
    }

    @Test
    void testDeleteComment() {
        // Given
        Long commentId = 1L;
        doNothing().when(commentRepository).delete(commentId);

        // When
        commentService.deleteComment(commentId);

        // Then
        verify(commentRepository, times(1)).delete(commentId);
    }

    @Test
    void testGetCommentById() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(testComment);

        // When
        Comment result = commentService.getCommentById(commentId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test comment", result.getText());
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void testGetCommentById_NotFound() {
        // Given
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(null);

        // When
        Comment result = commentService.getCommentById(commentId);

        // Then
        assertNull(result);
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void testUpdateComment() {
        // Given
        Long commentId = 1L;
        Comment updatedComment = new Comment(1L, "Updated text", 10L);

        when(commentRepository.findById(commentId)).thenReturn(testComment);
        doNothing().when(commentRepository).update(any(Comment.class));

        // When
        Comment result = commentService.updateComment(commentId, updatedComment);

        // Then
        assertNotNull(result);
        assertEquals("Updated text", result.getText());
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).update(testComment);
    }

    @Test
    void testUpdateComment_NotFound() {
        // Given
        Long commentId = 999L;
        Comment updatedComment = new Comment(999L, "Updated text", 10L);

        when(commentRepository.findById(commentId)).thenReturn(null);

        // When
        Comment result = commentService.updateComment(commentId, updatedComment);

        // Then
        assertNull(result);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).update(any(Comment.class));
    }
}