package tradepilot_api.dto;

import java.math.BigDecimal;

public record BusinessSummary (
        String currency,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netCashFlow
) {
}
