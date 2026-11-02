
package tradepilot_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long tokenLifetimeSeconds;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer:tradepilot-api}") String issuer,
            @Value("${app.jwt.access-token-expiration-minutes:15}")
            long tokenLifetimeMinutes
    ) {
        if (tokenLifetimeMinutes <= 0) {
            throw new IllegalArgumentException(
                    "JWT lifetime must be greater than zero"
            );
        }

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.tokenLifetimeSeconds =
                Math.multiplyExact(tokenLifetimeMinutes, 60);
    }

    public String generateToken(
            Long userId,
            String email,
            String fullName,
            String role
    ) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(email)
                .issuedAt(now)
                .expiresAt(
                        now.plus(
                                tokenLifetimeSeconds,
                                ChronoUnit.SECONDS
                        )
                )
                .claim("userId", userId)
                .claim("name", fullName)
                .claim("roles", List.of(role))
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    public long getTokenLifetimeSeconds() {
        return tokenLifetimeSeconds;
    }
}
