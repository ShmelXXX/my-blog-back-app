package shm.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shm.yandex.practicum.model.Post;
import shm.yandex.practicum.model.PostsPage;
import shm.yandex.practicum.repository.PostRepository;
import shm.yandex.practicum.service.PostService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private List<Post> testPosts;

    @BeforeEach
    void setUp() {
        testPost = new Post(1L, "Test Post", "Test Content", 10, 5, "test.jpg");
        testPost.setTags(Arrays.asList("java", "spring"));

        testPosts = Arrays.asList(
                new Post(1L, "First Post", "Content 1", 15, 3, "img1.jpg"),
                new Post(2L, "Second Post", "Content 2", 20, 5, "img2.jpg"),
                new Post(3L, "Third Post", "Content 3", 5, 1, "img3.jpg")
        );
    }

    @Test
    void testFindPostsBySearchWithPagination() {
        // Given
        String search = "test";
        int pageNumber = 1;
        int pageSize = 10;

        when(postRepository.findBySearchWithPagination(search, pageNumber, pageSize))
                .thenReturn(testPosts);
        when(postRepository.countBySearch(search)).thenReturn(3);

        // When
        PostsPage result = postService.findPostsBySearchWithPagination(search, pageNumber, pageSize);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getPosts().size());
        assertFalse(result.getHasPrev()); // Первая страница
        assertFalse(result.getHasNext()); // Все посты на одной странице
        assertEquals(1, result.getLastPage()); // Всего 1 страница

        verify(postRepository, times(1)).findBySearchWithPagination(search, pageNumber, pageSize);
        verify(postRepository, times(1)).countBySearch(search);
    }


    @Test
    void testSavePost() {
        // Given
        Post newPost = new Post(null, "New Post", "New Content", 0, 0, null);
        doNothing().when(postRepository).save(newPost);

        // When
        postService.save(newPost);

        // Then
        verify(postRepository, times(1)).save(newPost);
    }

    @Test
    void testDeletePost() {
        // Given
        Long postId = 1L;
        doNothing().when(postRepository).deletePost(postId);

        // When
        postService.deletePost(postId);

        // Then
        verify(postRepository, times(1)).deletePost(postId);
    }

    @Test
    void testFindById() {
        // Given
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(testPost);

        // When
        Post result = postService.findById(postId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Post", result.getTitle());
        assertEquals(10, result.getLikesCount());
        assertEquals(2, result.getTags().size());
        verify(postRepository, times(1)).findById(postId);
    }


    @Test
    void testUpdatePost() {
        // Given
        doNothing().when(postRepository).update(testPost);

        // When
        postService.update(testPost);

        // Then
        verify(postRepository, times(1)).update(testPost);
    }

    @Test
    void testFindPostsBySearchWithPagination_EmptySearch() {
        // Given
        String search = "";
        int pageNumber = 1;
        int pageSize = 10;

        when(postRepository.findBySearchWithPagination(search, pageNumber, pageSize))
                .thenReturn(testPosts);
        when(postRepository.countBySearch(search)).thenReturn(3);

        // When
        PostsPage result = postService.findPostsBySearchWithPagination(search, pageNumber, pageSize);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getPosts().size());
        // Проверяем, что поиск по пустой строке возвращает все посты
        verify(postRepository).findBySearchWithPagination(search, pageNumber, pageSize);
    }
}