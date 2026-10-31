package tradepilot_api.dto;

import java.math.BigDecimal;

public record InventoryValuationResponse(
        String currency,
        BigDecimal totalCostValue,
        BigDecimal totalRetailValue,
        BigDecimal potentialGrossMargin,
        Long productCount,
        BigDecimal totalQuantityInStock
) {
}
