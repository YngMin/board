package hello.board.dto.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public abstract class TokenDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank
        private String requestToken;
    }

    @Getter
    public static class CreateResponse {

        private final String accessToken;

        private CreateResponse(String accessToken) {
            this.accessToken = accessToken;
        }

        public static CreateResponse create(String accessToken) {
            return new CreateResponse(accessToken);
        }
    }
}
