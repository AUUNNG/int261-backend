package sit.int261.authserver.authentication;

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
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${app.security.jwt.token-max-interval}")
    private Duration MAX_TOKEN_INTERVAL;

    private String KEY_ID;

    private RSAKey rsaPrivateJWK;
    private RSAKey rsaPublicJWK;
    Map<String, RSAKey> sharedStore = new HashMap<>();

    public JwtUtils(@Value("${app.security.jwt.key-id}") String keyId) {
        this.KEY_ID = keyId;
        try {
            rsaPrivateJWK = new RSAKeyGenerator(2048)
                    .keyID(KEY_ID).generate();
            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();
            sharedStore.put(KEY_ID, rsaPublicJWK);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(UserDetails user) {
        return generateToken(user, null);
    }

    public String generateToken(UserDetails user, Duration maxTokenInterval) {
        try {
            String type = "REFRESH_TOKEN";
            if (maxTokenInterval == null) {
                maxTokenInterval = MAX_TOKEN_INTERVAL;
                type = "ACCESS_TOKEN";
            }
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("http://localhost:9091")
                    .expirationTime(new Date(new Date().getTime() + maxTokenInterval.toMillis()))
                    .issueTime(new Date(new Date().getTime()))
                    .claim("authorities", user.getAuthorities())
                    .claim("kid", KEY_ID)
                    .claim("type", type)
                    .claim("aud", "int261-api")
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaPrivateJWK.getKeyID()).build(), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // 1. ยอมรับเฉพาะ RS256
            if (!JWSAlgorithm.RS256.equals(
                    signedJWT.getHeader().getAlgorithm())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "INVALID TOKEN"
                );
            }
            // 2. เลือก public key จาก store ที่เราเชื่อถือ
            String kid = signedJWT.getHeader().getKeyID();

            if (kid == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "INVALID TOKEN"
                );
            }

            RSAKey publicKey = sharedStore.get(kid);

            if (publicKey == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "INVALID TOKEN"
                );
            }
            boolean validSignature = signedJWT.verify(
                    new RSASSAVerifier(publicKey.toRSAPublicKey())
            );
            if (!validSignature) {
                System.out.println("Invalid Signature");
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "INVALID TOKEN"
                );
            }
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (ParseException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT (Can't parsed)", ex);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> validateRefreshToken(String token) {
        Map<String, Object> claims = validateToken(token);
        if (!claims.get("type").equals("REFRESH_TOKEN")) {
            System.out.println("Token type is not REFRESH_TOKEN");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "INVALID REFRESH TOKEN");
        }
        return claims;
    }
}