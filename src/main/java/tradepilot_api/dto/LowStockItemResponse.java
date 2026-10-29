package tradepilot_api.dto;

import java.math.BigDecimal;

public record LowStockItemResponse(
        Long id,
        String productName,
        String sku,
        BigDecimal quantityInStock,
        BigDecimal lowStockThreshold
) {
}
