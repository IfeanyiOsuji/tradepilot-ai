package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tradepilot_api.dto.BusinessSummary;
import tradepilot_api.entity.Expense;
import tradepilot_api.entity.Sale;
import tradepilot_api.service.BusinessSummaryService;
import tradepilot_api.service.ExpenseService;
import tradepilot_api.service.SaleService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessSummaryServiceImpl implements BusinessSummaryService {
    private final SaleService saleService;
    private final ExpenseService expenseService;
    @Override
    public List<BusinessSummary> getSummary() {
        List<Sale> sales = saleService.getAllSales();
        List<Expense> expenses = expenseService.getAllExpenses();

        Map<String, BigDecimal> revenueByCurrency = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getCurrency,
                        Collectors.mapping(
                                Sale::getTotalAmount,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        BigDecimal::add))
                ));

        Map<String, BigDecimal> expensesByCurrency = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCurrency,
                        Collectors.mapping(
                                Expense::getAmount,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        BigDecimal::add))
                ));

        TreeSet<String> currencies = new TreeSet<>();
        currencies.addAll(revenueByCurrency.keySet());
        currencies.addAll(expensesByCurrency.keySet());

        return currencies.stream()
                .map(currency -> {
                    BigDecimal revenue = revenueByCurrency.getOrDefault(
                            currency, BigDecimal.ZERO);
                    BigDecimal totalExpenses = expensesByCurrency.getOrDefault(
                            currency, BigDecimal.ZERO);

                    return new BusinessSummary(
                            currency,
                            revenue,
                            totalExpenses,
                            revenue.subtract(totalExpenses)
                    );
                })
                .toList();
    }

}
