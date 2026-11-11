package sit.int261.authserver.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/bulk_register")
    public ResponseEntity<List<User>> createUsers(
            @RequestBody List<User> users) {
        return ResponseEntity.status(201).body(authService.createUsers(users));
    }

    @PostMapping("/register")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(201).body(authService.createUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequestUser user) {
        return ResponseEntity.ok(authService.login(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> login(@RequestBody JwtRequestRefreshToekn refreshToken) {
        return ResponseEntity.ok(authService.getNewToken(refreshToken));
    }

    @GetMapping("/jwks")
    public ResponseEntity<?> getPublicKey(@RequestHeader(required = false) String kid) {
        if(kid == null)
            kid = "K-1001";
        System.out.println("kid: " + kid);
        Map<String, Object> publicKey = Map.of("keys", List.of(authService.getPublicKey(kid).toJSONObject()));
//        return ResponseEntity.ok(authService.getPublicKey(kid).toJSONObject());
        return ResponseEntity.ok(publicKey);
    }
}