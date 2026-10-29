package tradepilot_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantRequest(
        @NotBlank(message = "Please enter a business question.")
        @Size(max = 1000, message = "Question cannot exceed 1000 characters.")
        String message
) {
}
