package shm.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostsPage {
    private List<Post> posts;
    private Boolean hasPrev;
    private Boolean hasNext;
    private Integer lastPage;
}
