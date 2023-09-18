package hello.board.web.dtoresolver;

import hello.board.dto.api.ArticleApiDto;
import hello.board.dto.service.ArticleServiceDto;
import hello.board.dto.service.search.ArticleSearchCond;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleServiceDtoResolver {

    public static ArticleServiceDto.Save toSaveDto(ArticleApiDto.SaveRequest saveRequest) {
        return ArticleServiceDto.Save.create(saveRequest.getTitle(), saveRequest.getContent());
    }

    public static ArticleServiceDto.Update toUpdateDto(ArticleApiDto.UpdateRequest updateRequest) {
        return ArticleServiceDto.Update.create(updateRequest.getTitle(), updateRequest.getContent());
    }

    public static Pageable toPageable(ArticleApiDto.FindRequest findRequest) {
        return PageRequest.of(findRequest.getPage() - 1, findRequest.getSize());
    }

    public static ArticleSearchCond toSearchCond(ArticleApiDto.FindRequest findRequest) {
        return ArticleSearchCond.create(findRequest.getKeyword(), findRequest.getType());
    }

}
