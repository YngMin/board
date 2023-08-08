package hello.board.dto.form;

import hello.board.dto.service.UserServiceDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserForm {
    @Getter
    @Setter
    public static final class Save {

        @NotBlank
        @Size(min = 2, max = 10)
        private String name;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 8, max = 20)
        private String password;
        private String passwordCheck;

        public static Save empty() {
            return new Save();
        }

        public UserServiceDto.Save toDto() {
            return UserServiceDto.Save.create(name, email, password);
        }

        public boolean passwordDoesNotMatch() {
            return !password.equals(passwordCheck);
        }
    }

    @Getter
    @Setter
    public static final class Login {

        private String email;
        private String password;

        public static Login empty() {
            return new Login();
        }
    }
}
