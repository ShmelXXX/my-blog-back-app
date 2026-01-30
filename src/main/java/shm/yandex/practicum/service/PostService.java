package shm.yandex.practicum.service;

import org.springframework.stereotype.Service;
import shm.yandex.practicum.model.Post;
import shm.yandex.practicum.model.PostsPage;
import shm.yandex.practicum.repository.PostRepository;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostsPage findPostsBySearchWithPagination(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1) pageNumber = 1;
        if (pageSize < 1) pageSize = 10;

        // Получаем посты для текущей страницы
        List<Post> posts = postRepository.findBySearchWithPagination(search, pageNumber, pageSize);

        // Считаем общее количество постов для поиска
        int totalPosts = postRepository.countBySearch(search);

        // Рассчитываем информацию о пагинации
        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        return new PostsPage(posts, hasPrev, hasNext, lastPage);
    }


    public void save(Post post) {
        postRepository.save(post);
    }
    public void deletePost(Long id) {
        postRepository.deletePost(id);
    }
    public Post findById(Long id) { return postRepository.findById(id); }

    public void update(Post post) {
        postRepository.update(post);
    }


}
