package com.example.auth-service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
class UserService {
    @Autowired
    private UserRepository userRepo;
    private Argon2PasswordEncoder passwordEncoder =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    @Autowired
    private JwtUtils jwtUtils;

    public User findUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }
    private void checkDuplication(User user) {
        if (userRepo.existsUserByUsernameOrEmail(
                user.getUsername(), user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User name or Email already exist !!! (" + user.getUsername() + ", " + user.getEmail() + ')');
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
        User actualUser = userRepo.findByUsernameOrEmail(user.getUsername()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Email not found")
        );
        String accessToken = jwtUtils.generateToken(actualUser);
        String refreshToken = jwtUtils.generateToken(actualUser, Duration.ofDays(7));
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }
}
