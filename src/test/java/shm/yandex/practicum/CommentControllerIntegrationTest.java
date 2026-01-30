package shm.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import shm.yandex.practicum.configuration.DataSourceConfiguration;
import shm.yandex.practicum.configuration.MinioConfig;
import shm.yandex.practicum.configuration.RestConfiguration;
import shm.yandex.practicum.configuration.WebConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringJUnitWebConfig(classes = {
        WebConfiguration.class,
        DataSourceConfiguration.class,
        RestConfiguration.class,
        MinioConfig.class,
        WebConfig.class
})
@TestPropertySource(locations = "classpath:test-application.properties")
public class CommentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        // Очистка и инициализация БД
        jdbcTemplate.execute("DROP ALL OBJECTS");

        // Создание таблиц и тестовых данных
        jdbcTemplate.execute("""
                    create table posts(
                        id bigserial primary key,
                        title varchar(256) not null,
                        text varchar(256) not null,
                        likes_count integer not null,
                        comments_count integer not null,
                        image_file_name varchar(255)
                    )
                """);

        jdbcTemplate.execute("""
                    create table comments(
                        id bigserial primary key,
                        text text not null,
                        post_id bigint not null,
                        foreign key (post_id) references posts(id) on delete cascade
                    )
                """);

        jdbcTemplate.execute("""
                    INSERT INTO posts (id, title, text, likes_count, comments_count)
                    VALUES (1,'Тестовый пост','Текст поста',10, 2)
                """);

        jdbcTemplate.execute("""
                    INSERT INTO comments (text, post_id) 
                    VALUES ('First comment', 1), ('Second comment', 1)
                """);
    }

    @Test
    void getCommentsByPostId_returnsComments() throws Exception {
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].text").value("First comment"))
                .andExpect(jsonPath("$[0].text").value("Second comment"));
    }

    @Test
    void addComment_createsNewComment() throws Exception {
        String commentJson = "{\"text\": \"New comment\"}";

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType("application/json")
                        .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("New comment"))
                .andExpect(jsonPath("$.postId").value(1));
    }

    @Test
    void deleteComment_removesComment() throws Exception {
        mockMvc.perform(delete("/api/posts/1/comments/1"))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE id = 1",
                Integer.class
        );

        assertNotNull(count);
        assertEquals(0, count);
    }
}
