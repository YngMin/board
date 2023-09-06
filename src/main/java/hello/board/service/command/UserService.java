package hello.board.service.command;

import hello.board.domain.User;
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
        User user = param.toEntity(passwordEncoder);
        return userRepository.save(user).getId();
    }

    public void update(Long id, Update param) {
        if (param != null) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> FailToFindEntityException.of("User"));
            updateUser(param, user);
        }
    }

    private void updateUser(Update param, User user) {
        user.modifyName(param.getName());
        user.modifyPassword(encodeRawPassword(param.getPassword()));
    }

    private String encodeRawPassword(String rawPassword) {
        return rawPassword == null ? null : passwordEncoder.encode(rawPassword);
    }
}
