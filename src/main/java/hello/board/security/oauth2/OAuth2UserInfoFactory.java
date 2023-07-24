package hello.board.security.oauth2;

import hello.board.security.oauth2.enums.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(AuthProvider authProvider, Map<String, Object> attributes) {
        return switch (authProvider) {
            case GOOGLE -> new GoogleOAuth2User(attributes);
            case KAKAO -> null;
            case NAVER -> null;
        };
    }
}
