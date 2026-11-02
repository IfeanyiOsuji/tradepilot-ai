
package tradepilot_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.cert.Certificate;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.keystore-location:classpath:keys/tradepilot-jwt.p12}")
    private Resource keyStoreResource;

    @Value("${app.jwt.keystore-password}")
    private String keyStorePassword;

    @Value("${app.jwt.key-alias:tradepilot-jwt}")
    private String keyAlias;

    @Bean
    public RSAKey rsaKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream inputStream = keyStoreResource.getInputStream()) {
            keyStore.load(inputStream, keyStorePassword.toCharArray());
        }

        Certificate certificate = keyStore.getCertificate(keyAlias);

        RSAPublicKey publicKey =
                (RSAPublicKey) certificate.getPublicKey();

        RSAPrivateKey privateKey =
                (RSAPrivateKey) keyStore.getKey(
                        keyAlias,
                        keyStorePassword.toCharArray()
                );

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyAlias)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        JWKSource<SecurityContext> jwkSource =
                new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
        return NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .build();
    }
}
