package sit.int261.authserver.authentication;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class AuthUserDetail extends org.springframework.security.core.userdetails.User {
    private Long id;
    public AuthUserDetail(Long id, String username, String password) {
        this(id, username, password, new ArrayList<GrantedAuthority>());
    }

    public AuthUserDetail(Long id, String username, String password
            , Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}