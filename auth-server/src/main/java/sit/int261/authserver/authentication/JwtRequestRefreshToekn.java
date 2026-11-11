package sit.int261.authserver.authentication;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
public class JwtRequestRefreshToekn {
    private String refreshToken;
}
