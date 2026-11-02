
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
    ORDER BY SUM(s.quantity) DESC
    """)
    List<ProductPerformanceProjection> findBestSellingProducts();

    @Query("""
    SELECT
        CAST(s.soldAt AS LocalDate) AS trendDate,
        s.currency AS currency,
        SUM(s.totalAmount) AS totalAmount,
        COUNT(s.id) AS transactionCount
    FROM Sale s
    WHERE s.soldAt >= :start
      AND s.soldAt < :end
    GROUP BY CAST(s.soldAt AS LocalDate), s.currency
    ORDER BY CAST(s.soldAt AS LocalDate)
    """)
    List<DailyFinancialTrendProjection> findDailySalesTrend(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Sale> findAllByOwnerId(Long ownerId);

    List<Sale> findByOwnerIdAndSoldAtGreaterThanEqualAndSoldAtLessThan(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT
        CAST(s.soldAt AS LocalDate) AS trendDate,
        s.currency AS currency,
        SUM(s.totalAmount) AS totalAmount,
        COUNT(s.id) AS transactionCount
    FROM Sale s
    WHERE s.soldAt >= :start
      AND s.soldAt < :end
      AND s.owner.id = :ownerId
    GROUP BY CAST(s.soldAt AS LocalDate), s.currency
    ORDER BY CAST(s.soldAt AS LocalDate)
    """)
    List<DailyFinancialTrendProjection> findDailySalesTrendByOwner(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("ownerId") Long ownerId
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
      AND s.owner.id = :ownerId
      AND i.owner.id = :ownerId
    GROUP BY i.id, i.productName, i.sku, s.currency
    ORDER BY SUM(s.totalAmount) DESC
    """)
    List<ProductPerformanceProjection> findAllProductPerformanceByOwner(
            @Param("ownerId") Long ownerId
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
      AND s.owner.id = :ownerId
      AND i.owner.id = :ownerId
      AND s.soldAt >= :start
      AND s.soldAt < :end
    GROUP BY i.id, i.productName, i.sku, s.currency
    ORDER BY SUM(s.totalAmount) DESC
    """)
    List<ProductPerformanceProjection> findProductPerformanceBetweenByOwner(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("ownerId") Long ownerId
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
      AND s.owner.id = :ownerId
      AND i.owner.id = :ownerId
    GROUP BY i.id, i.productName, i.sku, s.currency
    ORDER BY SUM(s.quantity) DESC
    """)
    List<ProductPerformanceProjection> findBestSellingProductsByOwner(
            @Param("ownerId") Long ownerId
    );
}
