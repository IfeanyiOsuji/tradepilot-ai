
package tradepilot_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductPerformanceResponse(
        Long inventoryItemId,
        String productName,
        String sku,
        String currency,
        BigDecimal totalQuantitySold,
        BigDecimal totalRevenue,
        BigDecimal averageSellingPrice,
        LocalDateTime lastSoldAt
) {}
