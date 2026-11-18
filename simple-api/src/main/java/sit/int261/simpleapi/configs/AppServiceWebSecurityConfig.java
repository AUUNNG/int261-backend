package sit.int261.simpleapi.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@EnableWebSecurity
@Configuration
public class AppServiceWebSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable()).sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*")
                        .hasAuthority("preduct:read")
                        .requestMatchers(HttpMethod.GET, "/api/customers", "/api/customers/*")
                        .hasAuthority("customer:read")
                        .requestMatchers("/api/brands/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(jwt -> jwt
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                ))
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<Map<String, Object>> entries =
                    jwt.getClaim("authorities");

            if (entries == null) {
                return List.<GrantedAuthority>of();
            }

            return entries.stream()
                    .map(entry -> (String) entry.get("role"))
                    .filter(Objects::nonNull)
                    .filter(value -> !value.isBlank())
                    .map(value ->
                            (GrantedAuthority) new SimpleGrantedAuthority(value))
                    .toList();
        });

        return converter;
    }
}
