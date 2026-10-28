package tradepilot_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseRequest(
        @NotBlank
        @Size(max = 150)
        String description,

        @NotBlank
        String category,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotNull
        LocalDateTime incurredAt
) {
}
