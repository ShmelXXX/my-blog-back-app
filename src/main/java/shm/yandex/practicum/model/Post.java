package shm.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Post {
    private Long id;
    private String title;
    private String text; // текст в формате markdown
    private List<String> tags = new ArrayList<>();
    private Integer likesCount;
    private Integer commentsCount;
    private String imageFileName;

    public Post(Long id, String title, String text, Integer likesCount, Integer commentsCount, String imageFileName) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.imageFileName = imageFileName;
    }
}
