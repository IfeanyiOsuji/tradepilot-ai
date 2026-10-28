package tradepilot_api.dto;

import tradepilot_api.entity.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        Long inventoryItemId,
        String productName,
        String sku,
        StockMovementType movementType,
        BigDecimal quantity,
        BigDecimal quantityBefore,
        BigDecimal quantityAfter,
        String description,
        LocalDateTime createdAt
) {
}
