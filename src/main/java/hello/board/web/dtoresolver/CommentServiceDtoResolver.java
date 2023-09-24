package hello.board.web.dtoresolver;

import hello.board.dto.api.CommentApiDto;
import hello.board.dto.service.CommentServiceDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


public class CommentServiceDtoResolver {

    public CommentServiceDto.Save toSaveDto(CommentApiDto.SaveRequest saveRequest) {
        return CommentServiceDto.Save.create(saveRequest.getContent());
    }

    public CommentServiceDto.Update toUpdateDto(CommentApiDto.UpdateRequest updateRequest) {
        return CommentServiceDto.Update.create(updateRequest.getContent());
    }

    public Pageable toPageable(CommentApiDto.PageRequest pageRequest) {
        return PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize());
    }
}
