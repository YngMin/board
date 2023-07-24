package hello.board.security.oauth2;

import java.util.Map;

public class GoogleOAuth2User extends OAuth2UserInfo {
    public GoogleOAuth2User(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getAttribute(String key) {
        return (String) super.getAttribute(key);
    }

    @Override
    public String getUsername() {
        return this.getAttribute("name");
    }

    @Override
    public String getEmail() {
        return this.getAttribute("email");
    }

    @Override
    public String getOAuth2Id() {
        return this.getAttribute("sub");
    }
}
