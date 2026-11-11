package sit.int261.authserver.authentication;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepo;
    private Argon2PasswordEncoder passwordEncoder =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    private void checkDuplication(User user) {
        if (userRepo.existsUserByUsernameOrEmail(
                user.getUsername(), user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User name or Email already exist !!! ("
                            + user.getUsername() + ", " + user.getEmail() + ')');
        }
    }

    public User createUser(User user) {
        checkDuplication(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public List<User> createUsers(List<User> users) {
        for (User user : users) {
            checkDuplication(user);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepo.saveAll(users);
    }

    public Map<String, String> login(JwtRequestUser user) {
        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                user.getUsername(), user.getPassword()
                        )
                );
        // จะมาถึงตรงนี้ได้เมื่อ username/password และสถานะบัญชีผ่าน
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateToken(userDetails, Duration.ofDays(7));
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public RSAKey getPublicKey(String kid) {
        if (jwtUtils.sharedStore.isEmpty()) {
            System.out.println("Empty Map");
        } else {
            System.out.println(jwtUtils.sharedStore);
        }
        return jwtUtils.sharedStore.get(kid);
    }

    public Map<String, String> getNewToken(JwtRequestRefreshToekn refreshToken) {
        Map<String, Object> claims = jwtUtils.validateRefreshToken(
                refreshToken.getRefreshToken());
        String username = (String) claims.get("sub");
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtUtils.generateToken(userDetails);
        String newRefreshToken = jwtUtils.generateToken(userDetails, Duration.ofDays(7));
        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }
}