package hello.board.dto.view;

import hello.board.domain.User;
import lombok.Getter;

@Getter
public class UserViewResponse {

    private final Long id;
    private final String username;

    private UserViewResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public static UserViewResponse from(User user) {
        return new UserViewResponse(user.getId(), user.getUsername());
    }

    public static UserViewResponse empty() {
        return new UserViewResponse(null, null);
    }
}
