package tradepilot_api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record InventoryRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name cannot exceed 150 characters")
        String productName,

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU cannot exceed 50 characters")
        String sku,

        @NotNull(message = "Quantity in stock is required")
        @PositiveOrZero(message = "Quantity in stock cannot be negative")
        BigDecimal quantityInStock,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.0", message = "Unit cost cannot be negative")
        BigDecimal unitCost,

        @NotNull(message = "Selling price is required")
        @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
        BigDecimal sellingPrice,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must contain 3 characters")
        String currency,

        @NotNull(message = "Low-stock threshold is required")
        @PositiveOrZero(message = "Low-stock threshold cannot be negative")
        BigDecimal lowStockThreshold
) {

}
