package hello.board.repository;

import hello.board.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("이메일로 조회 성공")
    void findByEmail() {
        //given
        User user = User.create("user", "user@board.com", "password");
        em.persist(user);

        em.flush();
        em.clear();

        //when
        User findUser = userRepository.findByEmail("user@board.com").orElseThrow();

        //then
        assertThat(findUser)
                .as("사용자")
                .isEqualTo(user);
    }

    @Test
    @DisplayName("이메일로 조회 실패")
    void findByEmail_fail() {
        //given
        User user = User.create("user", "user@board.com", "password");
        em.persist(user);

        em.flush();
        em.clear();

        //when
        Optional<User> userOptional = userRepository.findByEmail("WRONG_EMAIL");

        //then
        assertThat(userOptional.isEmpty())
                .as("존재하지 않는 이메일")
                .isTrue();
    }

    @Test
    @DisplayName("이메일로 존재 여부 확인 - 존재하는 경우")
    void existsByEmail_exists() {
        //given
        User user = User.create("user", "user@board.com", "password");
        em.persist(user);

        em.flush();
        em.clear();

        //when
        boolean exists = userRepository.existsByEmail("user@board.com");

        //then
        assertThat(exists)
                .as("사용자 존재 여부")
                .isTrue();
    }

    @Test
    @DisplayName("이메일로 존재 여부 확인 - 존재하지 않는 경우")
    void existsByEmail_doNotExist() {
        //given
        User user = User.create("user", "user@board.com", "password");
        em.persist(user);

        em.flush();
        em.clear();

        //when
        boolean exists = userRepository.existsByEmail("XXX");

        //then
        assertThat(exists)
                .as("사용자 존재 여부")
                .isFalse();
    }
}