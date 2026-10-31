package tradepilot_api.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tradepilot_api.entity.InventoryItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByProductNameContainingIgnoreCase(
            String productName
    );

    List<InventoryItem> findByQuantityInStockLessThanEqual(
            java.math.BigDecimal threshold
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :id")
    Optional<InventoryItem> findByIdForUpdate(@Param("id") Long id);



    @Query("""
    SELECT
        i.id AS inventoryItemId,
        i.productName AS productName,
        i.sku AS sku,
        i.currency AS currency,
        i.quantityInStock AS quantityInStock,
        i.lowStockThreshold AS lowStockThreshold,
        MAX(s.soldAt) AS lastSoldAt
    FROM InventoryItem i
    LEFT JOIN Sale s
        ON s.inventoryItem = i
        AND s.saleType = tradepilot_api.entity.SaleType.PRODUCT
    GROUP BY
        i.id,
        i.productName,
        i.sku,
        i.currency,
        i.quantityInStock,
        i.lowStockThreshold
    ORDER BY i.productName
""")
    List<InactiveProductProjection> findInventoryProductsWithLastSaleDate();

    @Query("""
    SELECT
        i.currency AS currency,
        SUM(i.quantityInStock * i.unitCost) AS totalCostValue,
        SUM(i.quantityInStock * i.sellingPrice) AS totalRetailValue,
        COUNT(i.id) AS productCount,
        SUM(i.quantityInStock) AS totalQuantityInStock
    FROM InventoryItem i
    GROUP BY i.currency
    ORDER BY i.currency
""")
    List<InventoryValuationProjection> calculateInventoryValuation();
}
