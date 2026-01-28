package shm.yandex.practicum.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import shm.yandex.practicum.model.Comment;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void update(Comment comment) {
        String sql = """
                UPDATE comments 
                SET text = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                comment.getText(),
                comment.getId());
    }


    public List<Comment> findByPostId(Long postId) {
        String sql = """
                SELECT id, text, post_id 
                FROM comments 
                WHERE post_id = ? 
                ORDER BY post_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Comment(
                rs.getLong("id"),
                rs.getString("text"),
                rs.getLong("post_id")
        ), postId);
    }

    public Comment save(Comment comment) {
        String sql = """
                INSERT INTO comments (text, post_id) 
                VALUES (?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());

            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            // Попробуем несколько способов получить ID
            Object idObj = keyHolder.getKeys().get("id");
            if (idObj == null) {
                idObj = keyHolder.getKeys().get("ID");
            }
            if (idObj == null && !keyHolder.getKeys().isEmpty()) {
                idObj = keyHolder.getKeys().values().iterator().next();
            }

            if (idObj instanceof Number) {
                comment.setId(((Number) idObj).longValue());
            }
        }

        return comment;
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM comments WHERE id = ?", id);
    }

    public Comment findById(Long commentId) {
        String sql = """
                SELECT id, text, post_id 
                FROM comments 
                WHERE id = ?
                """;

        List<Comment> comments = jdbcTemplate.query(sql, (rs, rowNum) -> new Comment(
                rs.getLong("id"),
                rs.getString("text"),
                rs.getLong("post_id")
        ), commentId);

        return comments.isEmpty() ? null : comments.getFirst();
    }
}