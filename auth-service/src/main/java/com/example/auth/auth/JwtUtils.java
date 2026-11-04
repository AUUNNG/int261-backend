package com.example.auth-service.auth;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${app.security.jwt.token-max-interval}")
    private Duration MAX_TOKEN_INTERVAL;
    @Value("${app.security.jwt.key-id}")
    private String KEY_ID;
    private RSAKey rsaPrivateJWK;
    private RSAKey rsaPublicJWK;

    public RSAKey getRsaPublicJWK() {
        return this.rsaPublicJWK;
    }

    public JwtUtils() {
        try {
            rsaPrivateJWK = new RSAKeyGenerator(2048)
                    .keyID(KEY_ID).generate();
            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();
            System.out.println(rsaPublicJWK.toJSONString());
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(User user, Duration maxTokenInterval) {
        try {
            String type = "REFRESH_TOKEN";
            if (maxTokenInterval == null) {
                maxTokenInterval = MAX_TOKEN_INTERVAL;
                type = "ACCESS_TOKEN";
            }
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("https://int261.sit.kmutt.ac.th")
                    .expirationTime(new Date(new Date().getTime() + maxTokenInterval.toMillis()))
                    .issueTime(new Date(new Date().getTime()))
//                    .claim("authorities", user.getAuthorities())
//                    .claim("uid", ((AuthUserDetail) user).getId())
                    .claim("roles", user.getRoles())
                    .claim("uid", user.getId())
                    .claim("type", type)
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaPrivateJWK.getKeyID()).build(), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(User user) {
        return generateToken(user, null);
    }
}