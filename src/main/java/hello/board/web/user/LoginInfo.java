package hello.board.web.user;

import lombok.Getter;

@Getter
public class LoginInfo {

    private final Long userId;
    private final String name;

    private LoginInfo(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public static LoginInfo of(Long userId, String name) {
        return new LoginInfo(userId, name);
    }
}
