package shm.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import shm.yandex.practicum.model.Post;
import shm.yandex.practicum.model.Comment;
import shm.yandex.practicum.model.PostsPage;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTests {

    private Post post;
    private Comment comment;
    private PostsPage postsPage;

    @BeforeEach
    void setUp() {
        post = new Post();
        comment = new Comment();
        postsPage = new PostsPage();
    }

    // Тесты для модели Post
    @Test
    void testPostDefaultConstructor() {
        assertNotNull(post);
        assertNull(post.getId());
        assertNull(post.getTitle());
        assertNull(post.getText());
        assertNull(post.getLikesCount());
        assertNull(post.getCommentsCount());
        assertNull(post.getImageFileName());
    }

    @Test
    void testPostParameterizedConstructor() {
        Post parameterizedPost = new Post(1L, "Test Title", "Test Text", 10, 5, "image.jpg");

        assertEquals(1L, parameterizedPost.getId());
        assertEquals("Test Title", parameterizedPost.getTitle());
        assertEquals("Test Text", parameterizedPost.getText());
        assertEquals(10, parameterizedPost.getLikesCount());
        assertEquals(5, parameterizedPost.getCommentsCount());
        assertEquals("image.jpg", parameterizedPost.getImageFileName());
        assertNotNull(parameterizedPost.getTags());
        assertTrue(parameterizedPost.getTags().isEmpty());
    }

    @Test
    void testPostSettersAndGetters() {
        post.setId(1L);
        post.setTitle("Test Title");
        post.setText("Test Text");
        post.setLikesCount(100);
        post.setCommentsCount(50);
        post.setImageFileName("test.jpg");

        List<String> tags = Arrays.asList("tag1", "tag2", "tag3");
        post.setTags(tags);

        assertEquals(1L, post.getId());
        assertEquals("Test Title", post.getTitle());
        assertEquals("Test Text", post.getText());
        assertEquals(100, post.getLikesCount());
        assertEquals(50, post.getCommentsCount());
        assertEquals("test.jpg", post.getImageFileName());
        assertEquals(3, post.getTags().size());
        assertEquals("tag1", post.getTags().get(0));
        assertEquals("tag2", post.getTags().get(1));
        assertEquals("tag3", post.getTags().get(2));
    }


    // Тесты для модели Comment
    @Test
    void testCommentDefaultConstructor() {
        assertNotNull(comment);
        assertNull(comment.getId());
        assertNull(comment.getText());
        assertNull(comment.getPostId());
    }

    @Test
    void testCommentParameterizedConstructor() {
        Comment parameterizedComment = new Comment(1L, "Test Comment", 10L);

        assertEquals(1L, parameterizedComment.getId());
        assertEquals("Test Comment", parameterizedComment.getText());
        assertEquals(10L, parameterizedComment.getPostId());
    }

    @Test
    void testCommentSettersAndGetters() {
        comment.setId(1L);
        comment.setText("This is a test comment");
        comment.setPostId(100L);

        assertEquals(1L, comment.getId());
        assertEquals("This is a test comment", comment.getText());
        assertEquals(100L, comment.getPostId());
    }

    // Тесты для модели PostsPage
    @Test
    void testPostsPageDefaultConstructor() {
        assertNotNull(postsPage);
        assertNull(postsPage.getPosts());
        assertNull(postsPage.getHasPrev());
        assertNull(postsPage.getHasNext());
        assertNull(postsPage.getLastPage());
    }

    @Test
    void testPostsPageParameterizedConstructor() {
        List<Post> postList = Arrays.asList(
                new Post(1L, "Title 1", "Text 1", 10, 2, "img1.jpg"),
                new Post(2L, "Title 2", "Text 2", 20, 3, "img2.jpg")
        );

        PostsPage page = new PostsPage(postList, true, false, 5);

        assertEquals(2, page.getPosts().size());
        assertTrue(page.getHasPrev());
        assertFalse(page.getHasNext());
        assertEquals(5, page.getLastPage());
    }

    @Test
    void testPostsPageSettersAndGetters() {
        List<Post> postList = List.of(
                new Post(1L, "Test", "Content", 5, 1, "test.jpg")
        );

        postsPage.setPosts(postList);
        postsPage.setHasPrev(true);
        postsPage.setHasNext(false);
        postsPage.setLastPage(10);

        assertEquals(1, postsPage.getPosts().size());
        assertEquals("Test", postsPage.getPosts().getFirst().getTitle());
        assertTrue(postsPage.getHasPrev());
        assertFalse(postsPage.getHasNext());
        assertEquals(10, postsPage.getLastPage());
    }

    @Test
    void testPostWithEmptyTags() {
        post.setTags(null);
        assertNull(post.getTags());

        post.setTags(List.of());
        assertNotNull(post.getTags());
        assertTrue(post.getTags().isEmpty());
    }
}
