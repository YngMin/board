package hello.board.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

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

    public void modifyName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public void modifyPassword(String password) {
        if (password != null) {
            this.password = password;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId())
                && Objects.equals(getName(), user.getName())
                && Objects.equals(getEmail(), user.getEmail())
                && Objects.equals(getPassword(), user.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getEmail(), getPassword());
    }
}
