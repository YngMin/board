package hello.board.service.command;

import hello.board.domain.User;
import hello.board.dto.service.UserServiceDto.Save;
import hello.board.dto.service.UserServiceDto.Update;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.UserRepository;
import hello.board.service.query.UserQueryService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    EntityManager em;

    @Autowired
    PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class Config {

        @Bean
        UserService userService(UserRepository userRepository) {
            return new UserService(userRepository, passwordEncoder());
        }

        @Bean
        UserQueryService userQueryService(UserRepository userRepository) {
            return new UserQueryService(userRepository);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @AfterEach
    void afterEach() {
        em.clear();
    }

    @Test
    @DisplayName("사용자 저장 성공")
    void save() {
        //given
        Save param = Save.create("user", "test@gmail.com", "1234");

        //when
        Long id = userService.save(param);

        em.flush();
        em.clear();

        //then
        User findUser = em.find(User.class, id);

        assertThat(findUser.getName())
                .as("사용자 이름")
                .isEqualTo("user");

        assertThat(findUser.getEmail())
                .as("사용자 이름")
                .isEqualTo("test@gmail.com");

        assertThat(passwordEncoder.matches("1234", findUser.getPassword()))
                .as("사용자 패스워드")
                .isTrue();

    }

    @Test
    @DisplayName("사용자 저장 실패")
    void save_fail() {
        //given
        Save param1 = Save.create("user1", "test@gmail.com", "1234");
        Save param2 = Save.create("user2", "test@gmail.com", "1234");

        userService.save(param1);

        //when & then
        Assertions.assertThatThrownBy(() -> userService.save(param2))
                .as("이미 존재하는 이메일")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void update() {
        //given
        User user = User.create("user", "test@gmail.com", passwordEncoder.encode("1234"));
        em.persist(user);

        final Long id = user.getId();

        em.flush();
        em.clear();

        //when 1
        Update param1 = Update.create("newUser", "newPassword");
        userService.update(id, param1);

        em.flush();
        em.clear();

        //then 1
        User findUser1 = em.find(User.class, id);

        assertThat(findUser1.getName())
                .as("수정된 이름")
                .isEqualTo("newUser");

        assertThat(findUser1.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("test@gmail.com");

        assertThat(passwordEncoder.matches("newPassword", findUser1.getPassword()))
                .as("수정된 패스워드")
                .isTrue();

        //when 2
        Update param2 = Update.create("newUser2", null);
        userService.update(id, param2);

        em.flush();
        em.clear();

        //then 2
        User findUser2 = em.find(User.class, id);
        assertThat(findUser2.getName())
                .as("수정된 이름")
                .isEqualTo("newUser2");

        assertThat(findUser2.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("test@gmail.com");

        assertThat(passwordEncoder.matches("newPassword", findUser2.getPassword()))
                .as("수정되지 않은 패스워드")
                .isTrue();

        //when 3
        Update param3 = Update.create(null, "newPassword2");
        userService.update(id, param3);

        em.flush();
        em.clear();

        //then 3
        User findUser3 = em.find(User.class, id);
        assertThat(findUser3.getName())
                .as("수정되지 않은 이름")
                .isEqualTo("newUser2");

        assertThat(findUser3.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("test@gmail.com");

        assertThat(passwordEncoder.matches("newPassword2", findUser3.getPassword()))
                .as("수정된 패스워드")
                .isTrue();

        //when 4
        userService.update(id, null);

        em.flush();
        em.clear();

        //then 4
        User findUser4 = em.find(User.class, id);
        assertThat(findUser4.getName())
                .as("수정되지 않은 이름")
                .isEqualTo("newUser2");

        assertThat(findUser4.getEmail())
                .as("수정되지 않은 이메일")
                .isEqualTo("test@gmail.com");

        assertThat(passwordEncoder.matches("newPassword2", findUser4.getPassword()))
                .as("수정되지 않은 패스워드")
                .isTrue();
    }

    @Test
    @DisplayName("사용자 정보 수정 실패")
    void update_fail() {
        //given
        User user = User.create("user", "test@gmail.com", passwordEncoder.encode("1234"));
        em.persist(user);

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        Update param = Update.create("newUser", "newPassword");
        assertThatThrownBy(() -> userService.update(WRONG_ID, param))
                .as("존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);

    }
}