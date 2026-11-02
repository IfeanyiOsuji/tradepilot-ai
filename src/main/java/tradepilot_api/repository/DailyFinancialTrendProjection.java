package tradepilot_api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyFinancialTrendProjection {

    LocalDate getTrendDate();

    String getCurrency();

    BigDecimal getTotalAmount();

    Long getTransactionCount();
}