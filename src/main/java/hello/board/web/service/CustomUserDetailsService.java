package hello.board.web.service;

import hello.board.domain.User;
import hello.board.service.query.UserQueryService;
import hello.board.web.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserQueryService userQueryService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userQueryService.findByEmail(email);
        return UserDetailsImpl.create(user.getId(), user.getName(),  user.getEmail(), user.getPassword());
    }
}
