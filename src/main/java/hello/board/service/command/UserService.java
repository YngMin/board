package hello.board.service.command;

import hello.board.exception.FailToFindEntityException;
import hello.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hello.board.dto.service.UserServiceDto.Save;
import static hello.board.dto.service.UserServiceDto.Update;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Long save(Save param) {
        return userRepository.save(
                param.toEntity(passwordEncoder))
                .getId();
    }

    public void update(Long id, Update param) {
        if (param != null) {
            userRepository.findById(id)
                    .orElseThrow(() -> FailToFindEntityException.of("User"))
                    .updateName(param.getName())
                    .updatePassword(encodeRawPassword(param.getPassword()));
        }
    }

    private String encodeRawPassword(String rawPassword) {
        return rawPassword == null ? null : passwordEncoder.encode(rawPassword);
    }
}
