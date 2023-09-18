package hello.board.web.dtoresolver;

import hello.board.dto.api.CommentApiDto;
import hello.board.dto.service.CommentServiceDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentServiceDtoResolver {

    public static CommentServiceDto.Save toSaveDto(CommentApiDto.SaveRequest saveRequest) {
        return CommentServiceDto.Save.create(saveRequest.getContent());
    }

    public static CommentServiceDto.Update toUpdateDto(CommentApiDto.UpdateRequest updateRequest) {
        return CommentServiceDto.Update.create(updateRequest.getContent());
    }

    public static Pageable toPageable(CommentApiDto.PageRequest pageRequest) {
        return PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize());
    }
}
