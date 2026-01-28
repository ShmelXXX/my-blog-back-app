package shm.yandex.practicum.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import shm.yandex.practicum.model.Post;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findBySearchWithPagination(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1) pageNumber = 1;
        if (pageSize < 1) pageSize = 10;

        String sql = """
            SELECT p.id, p.title, p.text, p.likes_count, 
                   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) as comments_count,
                   p.image_file_name
            FROM posts p
            WHERE p.title ILIKE ? OR p.text ILIKE ?
            ORDER BY p.likes_count DESC, p.id
            LIMIT ? OFFSET ?
            """;

        String searchPattern = "%" + search + "%";
        int offset = (pageNumber - 1) * pageSize;

        List<Post> posts = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String text = rs.getString("text");
            // Обрезаем текст до 128 символов, если он длиннее
            if (text != null && text.length() > 128) {
                text = text.substring(0, 128) + "…";
            }

            Post post = new Post(
                    rs.getLong("id"),
                    rs.getString("title"),
                    text, // Используем обрезанный текст
                    rs.getInt("likes_count"),
                    rs.getInt("comments_count"),
                    rs.getString("image_file_name")
            );
            post.setCommentsCount(rs.getInt("comments_count"));
            return post;
        }, searchPattern, searchPattern, pageSize, offset);

        // Загружаем теги для каждого поста
        for (Post post : posts) {
            loadTagsForPost(post);
        }

        return posts;
    }


    @Override
    public void save(Post post) {

        String sql = "insert into posts(title, text, likes_count, comments_count) values(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setInt(3, post.getLikesCount() != null ? post.getLikesCount() : 0);
            ps.setInt(4, post.getCommentsCount() != null ? post.getCommentsCount() : 0);
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            Number generatedId = (Number) keyHolder.getKeys().get("id");
            if (generatedId != null) {
                post.setId(generatedId.longValue());
            }
        }

        // Сохраняем теги после получения ID поста
        if (post.getId() != null && post.getTags() != null && !post.getTags().isEmpty()) {
            saveTags(post);
        }

    }

    @Override
    public void saveTags(Post post) {
        Long postId = post.getId();
        List<String> tags = post.getTags();

        if (postId == null || tags == null || tags.isEmpty()) {
            return;
        }

        // Удаляем старые теги для этого поста
        jdbcTemplate.update("DELETE FROM post_tags WHERE post_id = ?", postId);

        // Добавляем новые теги
        for (String tagName : tags) {
            if (tagName == null || tagName.trim().isEmpty()) {
                continue;
            }

            tagName = tagName.trim().toLowerCase();

            // Проверяем, существует ли тег
            String findTagSql = "SELECT id FROM tags WHERE name = ?";
            List<Long> tagIds = jdbcTemplate.queryForList(findTagSql, Long.class, tagName);

            Long tagId;
            if (tagIds.isEmpty()) {
                // Если тег не существует, создаем его
                String insertTagSql = "INSERT INTO tags(name) VALUES(?)";
                jdbcTemplate.update(insertTagSql, tagName);

                // ID созданного тега
                tagId = jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = ?", Long.class, tagName);
            } else {
                tagId = tagIds.get(0);
            }

            // Связываем тег с постом
            String insertPostTagSql = "INSERT INTO post_tags(post_id, tag_id) VALUES(?, ?)";
            jdbcTemplate.update(insertPostTagSql, postId, tagId);
        }
    }

    @Override
    public void deletePost(Long id) {
        jdbcTemplate.update("delete from posts where id = ?", id);
    }

    @Override
    public Post findById(Long id) {
        String sql = "select id, title, text, likes_count, comments_count, image_file_name from posts where id = ?";

        List<Post> posts = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count"),
                        rs.getString("image_file_name")
                ), id);

        if (posts.isEmpty()) {
            return null;
        }
        Post post = posts.getFirst();

        String tagsSql = """
                SELECT t.name 
                FROM tags t
                INNER JOIN post_tags pt ON t.id = pt.tag_id
                WHERE pt.post_id = ?
                ORDER BY t.name
                """;

        List<String> tags = jdbcTemplate.queryForList(tagsSql, String.class, id);
        post.setTags(tags);
        return post;
    }

    private void loadTagsForPost(Post post) {
        String tagsSql = """
                SELECT t.name 
                FROM tags t
                INNER JOIN post_tags pt ON t.id = pt.tag_id
                WHERE pt.post_id = ?
                ORDER BY t.name
                """;

        List<String> tags = jdbcTemplate.queryForList(tagsSql, String.class, post.getId());
        post.setTags(tags);
    }

    @Override
    public int countBySearch(String search) {
        // примитивный поиск чтобы было
        String sql = """
                SELECT COUNT(*)
                FROM posts p
                WHERE p.title ILIKE ? OR p.text ILIKE ?
                """;

        String searchPattern = "%" + search + "%";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, searchPattern, searchPattern);
        return count != null ? count : 0;

    }

    @Override
    public void update(Post post) {
        String sql = """
        UPDATE posts 
        SET title = ?, 
            text = ?, 
            likes_count = ?, 
            comments_count = ?, 
            image_file_name = ?            
        WHERE id = ?
        """;

        jdbcTemplate.update(sql,
                post.getTitle(),
                post.getText(),
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getImageFileName(),
                post.getId());

        if (post.getTags() != null) {
            saveTags(post);
        }
    }
}
