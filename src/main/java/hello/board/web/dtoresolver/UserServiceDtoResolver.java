package hello.board.web.dtoresolver;

import hello.board.dto.form.UserForm;
import hello.board.dto.service.UserServiceDto;

public class UserServiceDtoResolver {

    public UserServiceDto.Save toSaveDto(UserForm.Save saveForm) {
        return UserServiceDto.Save.create(saveForm.getName(), saveForm.getEmail(), saveForm.getPassword());
    }
}
