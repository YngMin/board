package hello.board.domain;

import hello.board.security.oauth2.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users")
@Entity
@Getter
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

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Builder
    private User(String username, String email, AuthProvider authProvider) {
        this.username = username;
        this.email = email;
        this.authProvider = authProvider;
    }

    public User update(String username, String email) {
        if (username != null) {
            this.username = username;
        }

        if (email != null) {
            this.email = email;
        }
        return this;
    }
}
