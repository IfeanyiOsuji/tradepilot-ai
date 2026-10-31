package tradepilot_api.dto;

import java.math.BigDecimal;

public record RestockRecommendationResponse(
        Long inventoryItemId,
        String productName,
        String sku,
        String currency,
        BigDecimal quantityInStock,
        BigDecimal lowStockThreshold,
        BigDecimal targetStockLevel,
        BigDecimal recommendedRestockQuantity,
        BigDecimal unitCost,
        BigDecimal estimatedRestockCost
) {
}
