package tradepilot_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tradepilot_api.entity.Expense;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByIncurredAtGreaterThanEqualAndIncurredAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT
        CAST(e.incurredAt AS LocalDate) AS trendDate,
        e.currency AS currency,
        SUM(e.amount) AS totalAmount,
        COUNT(e.id) AS transactionCount
    FROM Expense e
    WHERE e.incurredAt >= :start
      AND e.incurredAt < :end
    GROUP BY CAST(e.incurredAt AS LocalDate), e.currency
    ORDER BY CAST(e.incurredAt AS LocalDate)
    """)
    List<DailyFinancialTrendProjection> findDailyExpenseTrend(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Expense> findAllByOwnerId(Long ownerId);

    List<Expense> findByOwnerIdAndIncurredAtGreaterThanEqualAndIncurredAtLessThan(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT
        CAST(e.incurredAt AS LocalDate) AS trendDate,
        e.currency AS currency,
        SUM(e.amount) AS totalAmount,
        COUNT(e.id) AS transactionCount
    FROM Expense e
    WHERE e.incurredAt >= :start
      AND e.incurredAt < :end
      AND e.owner.id = :ownerId
    GROUP BY CAST(e.incurredAt AS LocalDate), e.currency
    ORDER BY CAST(e.incurredAt AS LocalDate)
    """)
    List<DailyFinancialTrendProjection> findDailyExpenseTrendByOwner(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("ownerId") Long ownerId
    );

}
