package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.DailyFinancialTrendResponse;
import tradepilot_api.repository.DailyFinancialTrendProjection;
import tradepilot_api.repository.ExpenseRepository;
import tradepilot_api.repository.SaleRepository;
import tradepilot_api.service.FinancialTrendService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialTrendServiceImpl implements FinancialTrendService {

    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public List<DailyFinancialTrendResponse> getDailyTrends(
            LocalDate startDate,
            LocalDate endDate) {

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

        var start = startDate.atStartOfDay();
        var end = endDate.plusDays(1).atStartOfDay();

        Map<String, TrendAccumulator> trends = new TreeMap<>();

        for (DailyFinancialTrendProjection item :
                saleRepository.findDailySalesTrend(start, end)) {

            String key = buildKey(item.getTrendDate(), item.getCurrency());

            TrendAccumulator accumulator = trends.computeIfAbsent(
                    key,
                    ignored -> new TrendAccumulator(
                            item.getTrendDate(),
                            item.getCurrency()
                    )
            );

            accumulator.sales = item.getTotalAmount();
            accumulator.salesCount = item.getTransactionCount();
        }

        for (DailyFinancialTrendProjection item :
                expenseRepository.findDailyExpenseTrend(start, end)) {

            String key = buildKey(item.getTrendDate(), item.getCurrency());

            TrendAccumulator accumulator = trends.computeIfAbsent(
                    key,
                    ignored -> new TrendAccumulator(
                            item.getTrendDate(),
                            item.getCurrency()
                    )
            );

            accumulator.expenses = item.getTotalAmount();
            accumulator.expenseCount = item.getTransactionCount();
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

    private String buildKey(LocalDate date, String currency) {
        return date + "|" + currency;
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