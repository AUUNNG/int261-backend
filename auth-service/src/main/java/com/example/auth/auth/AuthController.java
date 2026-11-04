package com.example.auth-service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(201).body(userService.createUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequestUser user) {
        Map<String,String> token = userService.login(user);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/bulk-register")
    public ResponseEntity<List<User>> createUsers(
            @RequestBody List<User> users) {
        return ResponseEntity.status(201)
                .body(userService.createUsers(users));
    }

}
