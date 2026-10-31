package tradepilot_api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface InactiveProductProjection {
    Long getInventoryItemId();

    String getProductName();

    String getSku();

    String getCurrency();

    BigDecimal getQuantityInStock();

    BigDecimal getLowStockThreshold();

    LocalDateTime getLastSoldAt();
}
