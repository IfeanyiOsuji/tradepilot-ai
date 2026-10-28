package tradepilot_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventorySaleRequest(
        @NotNull
        @Positive
        Long inventoryItemId,

        @NotNull
        @Positive
        BigDecimal quantity,

        @NotNull
        @PositiveOrZero
        BigDecimal unitPrice,

        @NotNull
        LocalDateTime soldAt
) {
}
