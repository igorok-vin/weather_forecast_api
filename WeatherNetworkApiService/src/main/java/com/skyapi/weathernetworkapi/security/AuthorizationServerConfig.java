package com.skyapi.weathernetworkapi.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.skyapi.weathernetworkapi.clientapp.ClientAppRepository;
import com.skyapi.weathernetworkapi.common.clientmanager.ClientApp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Configuration
@Profile("production")
public class AuthorizationServerConfig {

    @Value("${app.security.jwt.issuer}")
    private String issuerName;

    @Value("${app.security.jwt.access-token}")
    private int accessTokenExpirationTime;

    private final RsaKeyProperties rsaKeyProperties;

    public AuthorizationServerConfig(RsaKeyProperties rsaKeyProperties) {
        this.rsaKeyProperties = rsaKeyProperties;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeyProperties.publicKey()).privateKey(rsaKeyProperties.privateKey()).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(OAuth2AuthorizationServerConfigurer.authorizationServer(), Customizer.withDefaults());
        http.securityMatcher("/oauth2/**", "/.well-known/**");
        return http.build();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository(ClientAppRepository clientAppRepository) {
        return new RegisteredClientRepository() {
            @Override
            public void save(RegisteredClient registeredClient) {}

            @Override
            public RegisteredClient findById(String id) {
                return null;
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                Optional<ClientApp> byClientId = clientAppRepository.findByClientId(clientId);
                if(byClientId.isEmpty()) {return null;}

                ClientApp clientApp = byClientId.get();

                return RegisteredClient.withId(clientApp.getId().toString())
                        .clientName(clientApp.getName())
                        .clientId(clientApp.getClientId())
                        .clientSecret(clientApp.getClientSecret())
                        .scope(clientApp.getRole().toString())
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                        .build();
            }
        };
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return( context -> {
            if(OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                RegisteredClient client = context.getRegisteredClient();

                JwtClaimsSet.Builder builder = context.getClaims();

                builder.issuer(issuerName);
                builder.expiresAt(Instant.now().plus(accessTokenExpirationTime, ChronoUnit.MINUTES));

                builder.claims(claim -> {
                    claim.put("scope", client.getScopes());
                    claim.put("name", client.getClientName());
                    claim.remove("aud");
                });
            }
        });
    }

}
