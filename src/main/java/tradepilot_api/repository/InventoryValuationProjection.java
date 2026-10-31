package tradepilot_api.repository;

import java.math.BigDecimal;

public interface InventoryValuationProjection {
    String getCurrency();

    BigDecimal getTotalCostValue();

    BigDecimal getTotalRetailValue();

    Long getProductCount();

    BigDecimal getTotalQuantityInStock();
}
