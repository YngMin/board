package hello.board.dto.api.page;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
public class ArticlePageRequest {

    @Min(0)
    private int page = 1;

    @Min(1)
    private int size = 10;

    public Pageable toPageable() {
        return PageRequest.of(page-1, size);
    }

}
