package hello.board.web.controller.api;

import hello.board.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static hello.board.dto.api.TokenDto.CreateRequest;
import static hello.board.dto.api.TokenDto.CreateResponse;

@RestController
@RequiredArgsConstructor
public class TokenApiController {

    private final TokenService tokenService;

    @PostMapping("/api/token")
    public ResponseEntity<CreateResponse> createNewAccessToken(@RequestBody CreateRequest request) {
        String newAccessToken = tokenService.createNewAccessToken(request.getRequestToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateResponse.create(newAccessToken));
    }


}
