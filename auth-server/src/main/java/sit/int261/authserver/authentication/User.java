package sit.int261.authserver.authentication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 30)
    private String username;
    @Column(nullable = false, length = 200)
    private String password;
    @Column(length = 60)
    private String email;
    @Column(length = 100)
    private String roles; //comma separate roles
}
