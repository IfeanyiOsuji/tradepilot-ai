
        package tradepilot_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tradepilot_api.entity.Sale;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findBySoldAtGreaterThanEqualAndSoldAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT
            i.id AS inventoryItemId,
            i.productName AS productName,
            i.sku AS sku,
            s.currency AS currency,
            SUM(s.quantity) AS totalQuantitySold,
            SUM(s.totalAmount) AS totalRevenue,
            SUM(s.totalAmount) / SUM(s.quantity) AS averageSellingPrice,
            MAX(s.soldAt) AS lastSoldAt
        FROM Sale s
        JOIN s.inventoryItem i
        WHERE s.saleType = tradepilot_api.entity.SaleType.PRODUCT
        GROUP BY i.id, i.productName, i.sku, s.currency
        ORDER BY SUM(s.totalAmount) DESC
        """)
    List<ProductPerformanceProjection> findAllProductPerformance();

    @Query("""
        SELECT
            i.id AS inventoryItemId,
            i.productName AS productName,
            i.sku AS sku,
            s.currency AS currency,
            SUM(s.quantity) AS totalQuantitySold,
            SUM(s.totalAmount) AS totalRevenue,
            SUM(s.totalAmount) / SUM(s.quantity) AS averageSellingPrice,
            MAX(s.soldAt) AS lastSoldAt
        FROM Sale s
        JOIN s.inventoryItem i
        WHERE s.saleType = tradepilot_api.entity.SaleType.PRODUCT
          AND s.soldAt >= :start
          AND s.soldAt < :end
        GROUP BY i.id, i.productName, i.sku, s.currency
        ORDER BY SUM(s.totalAmount) DESC
        """)
    List<ProductPerformanceProjection> findProductPerformanceBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
