package shm.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import shm.yandex.practicum.configuration.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
public class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Инициализируем MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        // Полностью очищаем и пересоздаем схему
        jdbcTemplate.execute("DROP ALL OBJECTS");

        // Создаем таблицы заново
        jdbcTemplate.execute("""
                    create table if not exists posts(
                        id bigserial primary key,
                        title varchar(256) not null,
                        text varchar(256) not null,
                        likes_count integer not null,
                        comments_count integer not null,
                        image_file_name varchar(255)
                    )
                """);

        jdbcTemplate.execute("""
                    create table if not exists tags(
                        id bigserial primary key,
                        name varchar(50) not null unique
                    )
                """);

        jdbcTemplate.execute("""
                    create table if not exists comments(
                        id bigserial primary key,
                        text text not null,
                        post_id bigint not null,
                        foreign key (post_id) references posts(id) on delete cascade
                    )
                """);

        jdbcTemplate.execute("""
                    create table if not exists post_tags(
                        post_id bigint not null,
                        tag_id bigint not null,
                        primary key (post_id, tag_id),
                        foreign key (post_id) references posts(id) on delete cascade,
                        foreign key (tag_id) references tags(id) on delete cascade
                    )
                """);

        // Создаем теги
        jdbcTemplate.execute("INSERT INTO tags(name) VALUES ('tag1'), ('tag2'), ('tag3')");

        // Добавляем тестовые посты
        jdbcTemplate.execute("""
                    INSERT INTO posts (id, title, text, likes_count, comments_count, image_file_name)
                    VALUES 
                        (1,'Заголовок 1','текст 1',10, 1, 'file1.jpg'),
                        (2,'Заголовок 2','текст 2',20, 0, 'file2.jpg'),
                        (3,'Заголовок 3','текст 3',15, 2, 'file3.jpg')
                """);

        jdbcTemplate.execute("INSERT INTO post_tags(post_id, tag_id) VALUES (1, 1)");
        jdbcTemplate.execute("INSERT INTO post_tags(post_id, tag_id) VALUES (2, 2)");
        jdbcTemplate.execute("INSERT INTO post_tags(post_id, tag_id) VALUES (3, 3)");

        jdbcTemplate.execute("INSERT INTO comments(text, post_id) VALUES ('Комментарий 1', 1)");
        jdbcTemplate.execute("INSERT INTO comments(text, post_id) VALUES ('Комментарий 2', 3)");
        jdbcTemplate.execute("INSERT INTO comments(text, post_id) VALUES ('Комментарий 3', 3)");
    }

    @Test
    void getPosts_returnsPostsPage() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(3))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void getPostById_returnsCorrectPost() throws Exception {
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок 1"))
                .andExpect(jsonPath("$.text").value("текст 1"))
                .andExpect(jsonPath("$.likesCount").value(10))
                .andExpect(jsonPath("$.commentsCount").value(1))
                .andExpect(jsonPath("$.imageFileName").value("file1.jpg"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(1))
                .andExpect(jsonPath("$.tags[0]").value("tag1"));
    }

    @Test
    void getPostById_notFound() throws Exception {
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isOk())  // Возвращает null, не 404
                .andExpect(content().string(""));  // Пустое тело ответа
    }

    @Test
    void getPosts_withPagination() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void getPosts_withSearch() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "Заголовок 1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.posts[0].title").value("Заголовок 1"));
    }
}