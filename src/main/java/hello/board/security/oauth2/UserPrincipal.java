package hello.board.security.oauth2;

import hello.board.domain.User;
import hello.board.security.oauth2.enums.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long id;
    private final String username;
    private final String email;

    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    @Builder
    private UserPrincipal(Long id, String username, String email, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.authorities = authorities;
        this.attributes = new HashMap<>(attributes);
    }

    public static UserPrincipal from(User user, Map<String, Object> attributes) {
        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .authorities(createAuthorities())
                .attributes(attributes)
                .build();
    }

    private static List<SimpleGrantedAuthority> createAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(Role.USER.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(authorities);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }


}
