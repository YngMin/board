package hello.board.domain;

import hello.board.domain.util.EntityReflectionUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void create() {
        //when
        User user = User.create("user", "user@board.com", "password");

        //then
        assertThat(user.getName())
                .as("사용자 이름")
                .isEqualTo("user");

        assertThat(user.getEmail())
                .as("사용자 이메일")
                .isEqualTo("user@board.com");

        assertThat(user.getPassword())
                .as("사용자 패스워드")
                .isEqualTo("password");
    }

    @Test
    void modifyName() {
        //given
        User user = EntityReflectionUtils.createUserByReflection("user", "user@board.com", "password");

        //when
        user.modifyName("newName");

        //then
        assertThat(user.getName())
                .as("수정된 사용자 이름")
                .isEqualTo("newName");

        assertThat(user.getEmail())
                .as("사용자 이메일")
                .isEqualTo("user@board.com");

        assertThat(user.getPassword())
                .as("사용자 패스워드")
                .isEqualTo("password");
    }

    @Test
    void modifyName_null() {
        //given
        User user = EntityReflectionUtils.createUserByReflection("user", "user@board.com", "password");

        //when
        user.modifyName(null);

        //then
        assertThat(user.getName())
                .as("수정되지 않은 사용자 이름")
                .isEqualTo("user");

        assertThat(user.getEmail())
                .as("사용자 이메일")
                .isEqualTo("user@board.com");

        assertThat(user.getPassword())
                .as("사용자 패스워드")
                .isEqualTo("password");
    }

    @Test
    void modifyPassword() {
        //given
        User user = EntityReflectionUtils.createUserByReflection("user", "user@board.com", "password");

        //when
        user.modifyPassword("newPassword");

        //then
        assertThat(user.getName())
                .as("사용자 이름")
                .isEqualTo("user");

        assertThat(user.getEmail())
                .as("사용자 이메일")
                .isEqualTo("user@board.com");

        assertThat(user.getPassword())
                .as("수정된 사용자 패스워드")
                .isEqualTo("newPassword");
    }

    @Test
    void modifyPassword_null() {
        //given
        User user = EntityReflectionUtils.createUserByReflection("user", "user@board.com", "password");

        //when
        user.modifyPassword(null);

        //then
        assertThat(user.getName())
                .as("사용자 이름")
                .isEqualTo("user");

        assertThat(user.getEmail())
                .as("사용자 이메일")
                .isEqualTo("user@board.com");

        assertThat(user.getPassword())
                .as("수정되지 않은 사용자 패스워드")
                .isEqualTo("password");
    }
}