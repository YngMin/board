package hello.board.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Entity
@Getter
@ToString(of = {"id", "name", "email"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(updatable = false, nullable = false, unique = true)
    private String email;

    private String password;

    private User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static User create(String name, String email, String password) {
        return new User(name, email, password);
    }

    public User updateName(String name) {
        if (name != null) {
            this.name = name;
        }
        return this;
    }

    public void updatePassword(String password) {
        if (password != null) {
            this.password = password;
        }
    }
}
