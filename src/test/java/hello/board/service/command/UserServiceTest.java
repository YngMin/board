package hello.board.service.command;

import hello.board.domain.User;
import hello.board.dto.service.UserServiceDto.Save;
import hello.board.dto.service.UserServiceDto.Update;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserServiceTest {

    @Autowired
    UserService userService;
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class Config {

        @Bean
        UserService userService(UserRepository userRepository) {
            return new UserService(userRepository, passwordEncoder());
        }
        
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Test
    @DisplayName("사용자 저장 성공")
    void save() {
        //given
        Save param = Save.create("user", "user@board.com", "password");

        //when
        Long id = userService.save(param);

        //then
        User findUser = userRepository.findById(id).orElseThrow();

        assertThat(findUser.getName())
                .as("사용자 이름")
                .isEqualTo("user");

        assertThat(findUser.getEmail())
                .as("사용자 이름")
                .isEqualTo("user@board.com");

        assertThat(passwordEncoder.matches("password", findUser.getPassword()))
                .as("사용자 패스워드")
                .isTrue();

    }

    @Test
    @DisplayName("사용자 저장 실패")
    void save_fail() {
        //given
        User user = User.create("user1", "user@board.com", passwordEncoder.encode("password1"));
        userRepository.save(user);

        Save param = Save.create("user2", "user@board.com", "password2");

        //when & then
        Assertions.assertThatThrownBy(() -> userService.save(param))
                .as("이미 존재하는 이메일")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 이름과 패스워드")
    void update_both() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final Long id = user.getId();

        //when
        Update param = Update.create("newName", "newPassword");
        userService.update(id, param);

        //then
        User findUser = userRepository.findById(id).orElseThrow();

        assertThat(findUser.getName())
                .as("수정된 이름")
                .isEqualTo("newName");

        assertThat(findUser.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("user@board.com");

        assertThat(passwordEncoder.matches("newPassword", findUser.getPassword()))
                .as("수정된 패스워드")
                .isTrue();
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 이름")
    void update_name() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final Long id = user.getId();

        //when
        Update param = Update.create("newName", null);
        userService.update(id, param);

        //then
        User findUser = userRepository.findById(id).orElseThrow();

        assertThat(findUser.getName())
                .as("수정된 이름")
                .isEqualTo("newName");

        assertThat(findUser.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("user@board.com");

        assertThat(passwordEncoder.matches("password", findUser.getPassword()))
                .as("수정되지 않은 패스워드")
                .isTrue();
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 패스워드")
    void update_password() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final Long id = user.getId();

        //when
        Update param = Update.create(null, "newPassword");
        userService.update(id, param);

        //then
        User findUser = userRepository.findById(id).orElseThrow();

        assertThat(findUser.getName())
                .as("수정되지 않은 이름")
                .isEqualTo("user");

        assertThat(findUser.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("user@board.com");

        assertThat(passwordEncoder.matches("newPassword", findUser.getPassword()))
                .as("수정된 패스워드")
                .isTrue();
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 빈 UpdateDto")
    void update_empty() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final Long id = user.getId();

        //when
        Update param = Update.create(null, null);
        userService.update(id, param);

        //then
        User findUser = userRepository.findById(id).orElseThrow();

        assertThat(findUser.getName())
                .as("수정되지 않은 이름")
                .isEqualTo("user");

        assertThat(findUser.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("user@board.com");

        assertThat(passwordEncoder.matches("password", findUser.getPassword()))
                .as("수정되지 않은 패스워드")
                .isTrue();
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - null")
    void update_null() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final Long id = user.getId();

        //when
        userService.update(id, null);

        //then
        User findUser = userRepository.findById(id).orElseThrow();

        assertThat(findUser.getName())
                .as("수정되지 않은 이름")
                .isEqualTo("user");

        assertThat(findUser.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("user@board.com");

        assertThat(passwordEncoder.matches("password", findUser.getPassword()))
                .as("수정되지 않은 패스워드")
                .isTrue();
    }

    @Test
    @DisplayName("사용자 정보 수정 실패")
    void update_fail() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final Long WRONG_ID = 4444L;

        //when & then
        Update param = Update.create("newName", "newPassword");
        assertThatThrownBy(() -> userService.update(WRONG_ID, param))
                .as("존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);

    }
}