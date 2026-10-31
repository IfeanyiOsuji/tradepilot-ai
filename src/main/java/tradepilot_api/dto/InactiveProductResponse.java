package tradepilot_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InactiveProductResponse(
        Long inventoryItemId,
        String productName,
        String sku,
        String currency,
        BigDecimal quantityInStock,
        BigDecimal lowStockThreshold,
        LocalDateTime lastSoldAt,
        boolean neverSold
) {
}
