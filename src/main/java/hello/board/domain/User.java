package hello.board.domain;

import hello.board.security.oauth2.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Entity
@Getter
@ToString(of = {"id", "username", "email"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Builder
    private User(String username, String email, String password, AuthProvider authProvider) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.authProvider = authProvider;
    }

    public User updateAll(String username, String email, String password) {
        return updateUsername(username)
                .updateEmail(email)
                .updatePassword(password);

    }

    public User updateUsername(String username) {
        if (username != null) {
            this.username = username;
        }
        return this;
    }

    public User updateEmail(String email) {
        if (email != null) {
            this.email = email;
        }
        return this;
    }

    public User updatePassword(String password) {
        if (password != null) {
            this.password = password;
        }
        return this;
    }
}
