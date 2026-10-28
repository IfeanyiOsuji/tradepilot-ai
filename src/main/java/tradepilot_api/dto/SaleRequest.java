package tradepilot_api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleRequest (
        @NotBlank
        @Size(max = 150)
        String itemName,

        @NotBlank
        String saleType,

        @NotNull
        @Positive
        BigDecimal quantity,

        @NotNull
        @PositiveOrZero
        BigDecimal unitPrice,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotNull
        LocalDateTime soldAt
){

}

