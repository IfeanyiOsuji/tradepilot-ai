
package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.DailyFinancialTrendResponse;
import tradepilot_api.repository.DailyFinancialTrendProjection;
import tradepilot_api.repository.ExpenseRepository;
import tradepilot_api.repository.SaleRepository;
import tradepilot_api.service.CurrentUserService;
import tradepilot_api.service.FinancialTrendService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialTrendServiceImpl implements FinancialTrendService {

    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<DailyFinancialTrendResponse> getDailyTrends(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Both startDate and endDate are required."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "startDate must not be after endDate."
            );
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        boolean isAdmin = currentUserService.isAdmin();

        Long ownerId = isAdmin
                ? null
                : currentUserService.getCurrentUserId();

        List<DailyFinancialTrendProjection> salesTrends =
                isAdmin
                        ? saleRepository.findDailySalesTrend(start, end)
                        : saleRepository.findDailySalesTrendByOwner(
                        start, end, ownerId
                );

        List<DailyFinancialTrendProjection> expenseTrends =
                isAdmin
                        ? expenseRepository.findDailyExpenseTrend(start, end)
                        : expenseRepository.findDailyExpenseTrendByOwner(
                        start, end, ownerId
                );

        Map<TrendKey, TrendAccumulator> trends = new TreeMap<>();

        for (DailyFinancialTrendProjection item : salesTrends) {

            TrendKey key = new TrendKey(
                    item.getTrendDate(),
                    item.getCurrency()
            );

            TrendAccumulator accumulator = trends.computeIfAbsent(
                    key,
                    ignored -> new TrendAccumulator(
                            item.getTrendDate(),
                            item.getCurrency()
                    )
            );

            accumulator.sales = zeroIfNull(item.getTotalAmount());
            accumulator.salesCount = zeroIfNull(item.getTransactionCount());
        }

        for (DailyFinancialTrendProjection item : expenseTrends) {

            TrendKey key = new TrendKey(
                    item.getTrendDate(),
                    item.getCurrency()
            );

            TrendAccumulator accumulator = trends.computeIfAbsent(
                    key,
                    ignored -> new TrendAccumulator(
                            item.getTrendDate(),
                            item.getCurrency()
                    )
            );

            accumulator.expenses = zeroIfNull(item.getTotalAmount());
            accumulator.expenseCount = zeroIfNull(item.getTransactionCount());
        }

        return trends.values()
                .stream()
                .map(item -> new DailyFinancialTrendResponse(
                        item.date,
                        item.currency,
                        item.sales,
                        item.salesCount,
                        item.expenses,
                        item.expenseCount,
                        item.sales.subtract(item.expenses)
                ))
                .toList();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }

    private record TrendKey(
            LocalDate date,
            String currency
    ) implements Comparable<TrendKey> {

        @Override
        public int compareTo(TrendKey other) {

            int dateComparison = date.compareTo(other.date);

            if (dateComparison != 0) {
                return dateComparison;
            }

            return currency.compareTo(other.currency);
        }
    }

    private static class TrendAccumulator {

        private final LocalDate date;
        private final String currency;

        private BigDecimal sales = BigDecimal.ZERO;
        private Long salesCount = 0L;

        private BigDecimal expenses = BigDecimal.ZERO;
        private Long expenseCount = 0L;

        private TrendAccumulator(LocalDate date, String currency) {
            this.date = date;
            this.currency = currency;
        }
    }
}
