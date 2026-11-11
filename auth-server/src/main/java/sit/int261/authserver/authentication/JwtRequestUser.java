package sit.int261.authserver.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class JwtRequestUser {
    @NonNull
    @NotBlank
    public String username;
    @NonNull
    @Size(min = 8, max = 20)
    public String password;
}
