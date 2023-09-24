package hello.board.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserForm {
    @Getter
    @Setter
    @EqualsAndHashCode
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

        public boolean passwordsDoNotMatch() {
            return !password.equals(passwordCheck);
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static final class Login {

        private String email;
        private String password;

        public static Login empty() {
            return new Login();
        }
    }
}
