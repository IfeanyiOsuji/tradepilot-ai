
package tradepilot_api.dto;

import tradepilot_api.entity.Sale;
import tradepilot_api.entity.SaleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleResponse(
        Long id,
        String itemName,
        SaleType saleType,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String currency,
        BigDecimal totalAmount,
        LocalDateTime soldAt
) {
    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getItemName(),
                sale.getSaleType(),
                sale.getQuantity(),
                sale.getUnitPrice(),
                sale.getCurrency(),
                sale.getTotalAmount(),
                sale.getSoldAt()
        );
    }
}
