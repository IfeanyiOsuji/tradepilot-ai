package tradepilot_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StockMovementRequest(
        @NotNull(message = "Inventory item ID is required")
        @Positive(message = "Inventory item ID must be positive")
        Long inventoryItemId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description
) {
}
