package tradepilot_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyFinancialTrendResponse(
        LocalDate date,
        String currency,
        BigDecimal sales,
        Long salesCount,
        BigDecimal expenses,
        Long expenseCount,
        BigDecimal netCashFlow
) {
}