
package tradepilot_api.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private Authentication getAuthentication() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt)) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        return authentication;
    }

    public Long getCurrentUserId() {
        Jwt jwt = (Jwt) getAuthentication().getPrincipal();

        Number userId = jwt.getClaim("userId");

        if (userId == null) {
            throw new IllegalStateException(
                    "Authenticated token has no userId"
            );
        }

        return userId.longValue();
    }

    public String getCurrentUserEmail() {
        Jwt jwt = (Jwt) getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    public boolean isAdmin() {
        return getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );
    }
}
