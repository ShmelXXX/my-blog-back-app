package shm.yandex.practicum.repository;

import shm.yandex.practicum.model.Post;

import java.util.List;

public interface PostRepository {

    void save(Post post);

    void saveTags(Post post);

    void deletePost(Long id);

    Post findById(Long id);

    List<Post> findBySearchWithPagination(String search, int pageNumber, int pageSize);

    int countBySearch(String search);

    void update(Post post);
}
