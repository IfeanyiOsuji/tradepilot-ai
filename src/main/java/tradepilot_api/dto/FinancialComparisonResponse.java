
        package tradepilot_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialComparisonResponse(
        String currency,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd,
        LocalDate previousPeriodStart,
        LocalDate previousPeriodEnd,
        BigDecimal currentRevenue,
        BigDecimal previousRevenue,
        BigDecimal revenueChange,
        BigDecimal revenueChangePercentage,
        BigDecimal currentExpenses,
        BigDecimal previousExpenses,
        BigDecimal expenseChange,
        BigDecimal expenseChangePercentage,
        BigDecimal currentNetCashFlow,
        BigDecimal previousNetCashFlow,
        BigDecimal netCashFlowChange,
        BigDecimal netCashFlowChangePercentage
) {
}
