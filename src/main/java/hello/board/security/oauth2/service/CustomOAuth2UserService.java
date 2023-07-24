package hello.board.security.oauth2.service;

import hello.board.domain.User;
import hello.board.repository.UserRepository;
import hello.board.security.oauth2.enums.AuthProvider;
import hello.board.security.oauth2.OAuth2UserInfo;
import hello.board.security.oauth2.OAuth2UserInfoFactory;
import hello.board.security.oauth2.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        AuthProvider authProvider = AuthProvider.valueOf(getAuthProvider(userRequest));
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authProvider, oAuth2User.getAttributes());

        emailCheck(oAuth2UserInfo.getEmail());

        User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .map(findUser -> updateUser(oAuth2UserInfo, authProvider, findUser))
                .orElse(saveUser(oAuth2UserInfo, authProvider));

        return UserPrincipal.from(user, oAuth2User.getAttributes());
    }

    private static User updateUser(OAuth2UserInfo oAuth2UserInfo, AuthProvider authProvider, User user) {
        authProviderCheck(authProvider, user);
        return user.update(oAuth2UserInfo.getUsername(), oAuth2UserInfo.getEmail());
    }

    private static void authProviderCheck(AuthProvider authProvider, User user) {
        if (user.getAuthProvider() != authProvider) {
            throw new IllegalStateException("Email Already exists");
        }
    }

    private User saveUser(OAuth2UserInfo oAuth2UserInfo, AuthProvider authProvider) {
        User user = User.builder()
                        .username(oAuth2UserInfo.getUsername())
                        .email(oAuth2UserInfo.getEmail())
                        .authProvider(authProvider)
                        .build();

        return userRepository.save(user);
    }

    private void emailCheck(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email Not Found From OAuth2 provider");
        }
    }

    private static String getAuthProvider(OAuth2UserRequest userRequest) {
        return userRequest.getClientRegistration().getRegistrationId().toUpperCase();
    }
}
