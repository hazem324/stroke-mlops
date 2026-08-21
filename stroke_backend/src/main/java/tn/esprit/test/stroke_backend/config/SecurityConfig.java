package tn.esprit.test.stroke_backend.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.security.Keys;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http) throws Exception {

    http
        .cors(cors -> {})
        .csrf(csrf -> csrf.disable())

        .authorizeHttpRequests(auth -> auth

            // CORS
            .requestMatchers(
                HttpMethod.OPTIONS,
                "/**"
            ).permitAll()

            .requestMatchers(
                "/api/auth/**"
            ).permitAll()

            .requestMatchers(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**"
            ).permitAll()

            // EVERYTHING ELSE
            .anyRequest()
            .authenticated()
        )

        .oauth2ResourceServer(oauth2 ->
    oauth2.jwt(jwt ->
        jwt.jwtAuthenticationConverter(
            jwtAuthenticationConverter()
        )
    )
);

    return http.build();
}

@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {

    JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

    converter.setJwtGrantedAuthoritiesConverter(jwt -> {

        String role = jwt.getClaimAsString("role");

        if (role == null || role.isBlank()) {
            return java.util.List.of();
        }

        return java.util.List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    });

    return converter;
}


@Bean
public JwtDecoder jwtDecoder(
        @Value("${jwt.secret}") String secret) {

    SecretKey key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
    );

    return NimbusJwtDecoder
            .withSecretKey(key)
            .build();
}

}