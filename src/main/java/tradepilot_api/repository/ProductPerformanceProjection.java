
package tradepilot_api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductPerformanceProjection {

    Long getInventoryItemId();

    String getProductName();

    String getSku();

    String getCurrency();

    BigDecimal getTotalQuantitySold();

    BigDecimal getTotalRevenue();

    BigDecimal getAverageSellingPrice();

    LocalDateTime getLastSoldAt();
}
