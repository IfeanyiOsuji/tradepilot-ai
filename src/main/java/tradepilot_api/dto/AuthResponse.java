
package tradepilot_api.dto;

public record AuthResponse(
        Long userId,
        String fullName,
        String email,
        String message
) {
}
