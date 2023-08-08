package hello.board.dto.view;

import hello.board.domain.User;
import lombok.Getter;

@Getter
public final class UserViewResponse {

    private final Long id;
    private final String username;

    private UserViewResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public static UserViewResponse from(User user) {
        if (user == null) {
            return empty();
        }

        return new UserViewResponse(user.getId(), user.getName());
    }

    public static UserViewResponse empty() {
        return new UserViewResponse(null, null);
    }
}
