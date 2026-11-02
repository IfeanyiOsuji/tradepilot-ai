
package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.BusinessSummary;
import tradepilot_api.entity.Expense;
import tradepilot_api.entity.Sale;
import tradepilot_api.service.BusinessSummaryService;
import tradepilot_api.service.ExpenseService;
import tradepilot_api.service.SaleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessSummaryServiceImpl
        implements BusinessSummaryService {

    private final SaleService saleService;
    private final ExpenseService expenseService;

    @Override
    public List<BusinessSummary> getSummary() {

        List<Sale> sales = saleService.getAllSales();
        List<Expense> expenses = expenseService.getAllExpenses();

        return buildSummary(sales, expenses);
    }

    @Override
    public List<BusinessSummary> getSummary(
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
                    "startDate cannot be after endDate."
            );
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Sale> sales = saleService.getSalesBetween(start, end);
        List<Expense> expenses = expenseService.getExpensesBetween(start, end);

        return buildSummary(sales, expenses);
    }

    private List<BusinessSummary> buildSummary(
            List<Sale> sales,
            List<Expense> expenses
    ) {

        Map<String, BigDecimal> revenueByCurrency =
                sales.stream()
                        .collect(Collectors.groupingBy(
                                Sale::getCurrency,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Sale::getTotalAmount,
                                        BigDecimal::add
                                )
                        ));

        Map<String, BigDecimal> expensesByCurrency =
                expenses.stream()
                        .collect(Collectors.groupingBy(
                                Expense::getCurrency,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Expense::getAmount,
                                        BigDecimal::add
                                )
                        ));

        TreeSet<String> currencies = new TreeSet<>();

        currencies.addAll(revenueByCurrency.keySet());
        currencies.addAll(expensesByCurrency.keySet());

        return currencies.stream()
                .map(currency -> {

                    BigDecimal revenue =
                            revenueByCurrency.getOrDefault(
                                    currency,
                                    BigDecimal.ZERO
                            );

                    BigDecimal totalExpenses =
                            expensesByCurrency.getOrDefault(
                                    currency,
                                    BigDecimal.ZERO
                            );

                    BigDecimal netCashFlow =
                            revenue.subtract(totalExpenses);

                    return new BusinessSummary(
                            currency,
                            revenue,
                            totalExpenses,
                            netCashFlow
                    );
                })
                .toList();
    }
}
