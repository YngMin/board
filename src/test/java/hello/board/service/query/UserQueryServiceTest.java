package hello.board.service.query;

import hello.board.domain.User;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserQueryServiceTest {

    @Autowired
    UserQueryService userQueryService;
    
    @Autowired
    UserRepository userRepository;
    
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

    @Test
    @DisplayName("findById 성공")
    void findById() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final Long id = user.getId();

        //when
        User findUser = userQueryService.findById(id);

        // then
        assertThat(findUser)
                .as("사용자")
                .isEqualTo(user);
    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

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
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        //when
        User findUser = userQueryService.findByEmail("user@board.com");

        // then
        assertThat(findUser)
                .as("사용자")
                .isEqualTo(user);
    }

    @Test
    @DisplayName("이메일로 조회 실패")
    void findByEmail_fail() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        final String WRONG_EMAIL = "WRONG_EMAIL";

        //when & then
        assertThatThrownBy(() -> userQueryService.findByEmail(WRONG_EMAIL))
                .as("존재하지 않는 사용자 이메일")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("이메일로 존재 여부 확인 - 존재하는 경우")
    void existsByEmail_exists() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        //when
        boolean exists = userQueryService.existsByEmail("user@board.com");

        // then
        assertThat(exists)
                .as("존재하는 이메일")
                .isTrue();
    }

    @Test
    @DisplayName("이메일로 존재 여부 확인 - 존재하지 않는 경우")
    void existsByEmail_doNotExist() {
        //given
        User user = User.create("user", "user@board.com", passwordEncoder.encode("password"));
        userRepository.save(user);

        //when
        boolean exists = userQueryService.existsByEmail("WRONG_EMAIL");

        // then
        assertThat(exists)
                .as("존재하지 않는 이메일")
                .isFalse();
    }
}