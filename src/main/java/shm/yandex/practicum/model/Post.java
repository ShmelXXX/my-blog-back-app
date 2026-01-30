package shm.yandex.practicum.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private Long id;
    private String title;
    private String text; // текст в формате markdown
    private List<String> tags = new ArrayList<>();
    private Integer likesCount;
    private Integer commentsCount;
    private String imageFileName;

    public Post() {
    }


    public Post(Long id, String title, String text, Integer likesCount, Integer commentsCount, String imageFileName) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.imageFileName = imageFileName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}
