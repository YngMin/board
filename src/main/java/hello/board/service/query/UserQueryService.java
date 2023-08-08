package hello.board.service.query;

import hello.board.domain.User;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> FailToFindEntityException.of("User"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> FailToFindEntityException.of("User"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
