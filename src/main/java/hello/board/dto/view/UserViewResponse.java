package hello.board.dto.view;

import lombok.Getter;

@Getter
public final class UserViewResponse {

    private final Long id;
    private final String username;

    private UserViewResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public static UserViewResponse of(Long id, String username) {
        return new UserViewResponse(id, username);
    }

    public static UserViewResponse empty() {
        return new UserViewResponse(null, null);
    }
}
