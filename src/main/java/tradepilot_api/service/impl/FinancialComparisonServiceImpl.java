
package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tradepilot_api.dto.BusinessSummary;
import tradepilot_api.dto.FinancialComparisonResponse;
import tradepilot_api.service.BusinessSummaryService;
import tradepilot_api.service.FinancialComparisonService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialComparisonServiceImpl
        implements FinancialComparisonService {

    private final BusinessSummaryService businessSummaryService;

    @Override
    public List<FinancialComparisonResponse> compareWeeks() {

        LocalDate today = LocalDate.now();

        LocalDate currentStart =
                today.with(DayOfWeek.MONDAY);

        LocalDate currentEnd = currentStart.plusDays(6);

        LocalDate previousStart = currentStart.minusWeeks(1);
        LocalDate previousEnd = currentStart.minusDays(1);

        return comparePeriods(
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );
    }

    @Override
    public List<FinancialComparisonResponse> compareMonths() {

        LocalDate today = LocalDate.now();

        LocalDate currentStart =
                today.withDayOfMonth(1);

        LocalDate currentEnd =
                today.withDayOfMonth(today.lengthOfMonth());

        LocalDate previousStart =
                currentStart.minusMonths(1);

        LocalDate previousEnd =
                currentStart.minusDays(1);

        return comparePeriods(
                currentStart,
                currentEnd,
                previousStart,
                previousEnd
        );
    }

    private List<FinancialComparisonResponse> comparePeriods(
            LocalDate currentStart,
            LocalDate currentEnd,
            LocalDate previousStart,
            LocalDate previousEnd) {

        Map<String, BusinessSummary> currentSummaries =
                toCurrencyMap(
                        businessSummaryService.getSummary(
                                currentStart,
                                currentEnd
                        )
                );

        Map<String, BusinessSummary> previousSummaries =
                toCurrencyMap(
                        businessSummaryService.getSummary(
                                previousStart,
                                previousEnd
                        )
                );

        Set<String> currencies = new TreeSet<>();

        currencies.addAll(currentSummaries.keySet());
        currencies.addAll(previousSummaries.keySet());

        return currencies.stream()
                .map(currency -> {

                    BusinessSummary current =
                            currentSummaries.get(currency);

                    BusinessSummary previous =
                            previousSummaries.get(currency);

                    BigDecimal currentRevenue =
                            current == null
                                    ? BigDecimal.ZERO
                                    : zeroIfNull(current.totalRevenue());

                    BigDecimal previousRevenue =
                            previous == null
                                    ? BigDecimal.ZERO
                                    : zeroIfNull(previous.totalRevenue());

                    BigDecimal currentExpenses =
                            current == null
                                    ? BigDecimal.ZERO
                                    : zeroIfNull(current.totalExpenses());

                    BigDecimal previousExpenses =
                            previous == null
                                    ? BigDecimal.ZERO
                                    : zeroIfNull(previous.totalExpenses());

                    BigDecimal currentCashFlow =
                            current == null
                                    ? BigDecimal.ZERO
                                    : zeroIfNull(current.netCashFlow());

                    BigDecimal previousCashFlow =
                            previous == null
                                    ? BigDecimal.ZERO
                                    : zeroIfNull(previous.netCashFlow());

                    return new FinancialComparisonResponse(
                            currency,
                            currentStart,
                            currentEnd,
                            previousStart,
                            previousEnd,
                            currentRevenue,
                            previousRevenue,
                            currentRevenue.subtract(previousRevenue),
                            percentageChange(
                                    currentRevenue,
                                    previousRevenue
                            ),
                            currentExpenses,
                            previousExpenses,
                            currentExpenses.subtract(previousExpenses),
                            percentageChange(
                                    currentExpenses,
                                    previousExpenses
                            ),
                            currentCashFlow,
                            previousCashFlow,
                            currentCashFlow.subtract(previousCashFlow),
                            percentageChange(
                                    currentCashFlow,
                                    previousCashFlow
                            )
                    );
                })
                .toList();
    }

    private Map<String, BusinessSummary> toCurrencyMap(
            List<BusinessSummary> summaries) {

        return summaries.stream()
                .collect(Collectors.toMap(
                        summary -> summary.currency()
                                .trim()
                                .toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Multiple summaries exist for currency "
                                            + first.currency()
                            );
                        }
                ));
    }

    private BigDecimal percentageChange(
            BigDecimal current,
            BigDecimal previous) {

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            // Percentage change from zero is mathematically undefined.
            return null;
        }

        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        previous.abs(),
                        2,
                        RoundingMode.HALF_UP
                );
    }
    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
