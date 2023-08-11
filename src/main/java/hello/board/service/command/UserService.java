package hello.board.service.command;

import hello.board.dto.service.UserServiceDto;
import hello.board.repository.UserRepository;
import hello.board.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserQueryService userQueryService;

    private final PasswordEncoder passwordEncoder;

    public Long save(UserServiceDto.Save param) {
        return userRepository.save(
                param.toEntity(passwordEncoder))
                .getId();
    }

    public void update(Long id, UserServiceDto.Update param) {
        userQueryService.findById(id)
                .updateName(param.getName())
                .updatePassword(param.getPassword());
    }

}
