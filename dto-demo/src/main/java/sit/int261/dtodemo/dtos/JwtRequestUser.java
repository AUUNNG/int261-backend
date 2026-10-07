package sit.int261.dtodemo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JwtRequestUser {
    private String username;
    private String email;
    private String password;
}
