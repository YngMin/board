package hello.board.dto.service;

import hello.board.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserServiceDto {

    @Getter
    public static final class Save {
        private final String name;
        private final String email;
        private final String password;

        private Save(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }

        public static Save create(String name, String email, String password) {
            return new Save(name, email, password);
        }

        public User toEntity(PasswordEncoder passwordEncoder) {
            return User.create(name, email, passwordEncoder.encode(password));
        }
    }

    @Getter
    public static final class Update {
        private final String name;
        private final String password;

        private Update(String name, String password) {
            this.name = name;
            this.password = password;
        }

        public static Update create(String name, String password) {
            return new Update(name, password);
        }
    }
}
