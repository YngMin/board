package hello.board.service.query;

import hello.board.domain.User;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
class UserQueryServiceTest {

    @Autowired
    UserQueryService userQueryService;

    @Autowired
    EntityManager em;

    @Autowired
    PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class Config {

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
    @DisplayName("findById 성공")
    void findById() {
        //given
        User user = User.create("user", "test@gmail.com", passwordEncoder.encode("1234"));
        em.persist(user);
        final Long id = user.getId();

        em.flush();
        em.clear();

        //when
        User findUser = userQueryService.findById(id);

        // then
        assertThat(findUser.getName())
                .as("사용자 이름")
                .isEqualTo("user");

        assertThat(findUser.getEmail())
                .as("사용자 이메일")
                .isEqualTo("test@gmail.com");

        assertThat(passwordEncoder.matches("1234", findUser.getPassword()))
                .as("사용자 비밀번호")
                .isTrue();

    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User user = User.create("user", "test@gmail.com", passwordEncoder.encode("1234"));
        em.persist(user);

        em.flush();
        em.clear();

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> userQueryService.findById(WRONG_ID))
                .as("존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("이메일로 조회 성공")
    void findByEmail() {
        //given
        User user = User.create("user", "test@gmail.com", passwordEncoder.encode("1234"));
        em.persist(user);

        em.flush();
        em.clear();

        //when
        User findUser = userQueryService.findByEmail("test@gmail.com");

        // then
        assertThat(findUser.getName())
                .as("사용자 이름")
                .isEqualTo("user");

        assertThat(passwordEncoder.matches("1234", findUser.getPassword()))
                .as("사용자 비밀번호")
                .isTrue();
    }

    @Test
    @DisplayName("이메일로 조회 실패")
    void findByEmail_fail() {
        //given
        User user = User.create("user", "test@gmail.com", passwordEncoder.encode("1234"));
        em.persist(user);

        em.flush();
        em.clear();

        final String WRONG_EMAIL = "none";

        //when & then
        assertThatThrownBy(() -> userQueryService.findByEmail(WRONG_EMAIL))
                .as("존재하지 않는 사용자 이메일")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("이메일로 존재 여부 확인")
    void existsByEmail() {
        //given
        User user = User.create("user", "test@gmail.com", passwordEncoder.encode("1234"));
        em.persist(user);

        em.flush();
        em.clear();

        //when
        boolean exists1 = userQueryService.existsByEmail("test@gmail.com");
        boolean exists2 = userQueryService.existsByEmail("none");

        // then
        assertThat(exists1)
                .as("존재하는 이메일")
                .isTrue();

        assertThat(exists2)
                .as("존재하지 않는 이메일")
                .isFalse();
    }
}