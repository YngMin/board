package hello.board.security.oauth2;

import java.util.HashMap;
import java.util.Map;

public abstract class OAuth2UserInfo {

    private final Map<String, Object> attributes;


    protected OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = new HashMap<>(attributes);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    public abstract String getOAuth2Id();
    public abstract String getUsername();
    public abstract String getEmail();
}


