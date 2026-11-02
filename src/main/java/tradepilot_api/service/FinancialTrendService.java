package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.DailyFinancialTrendResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public interface FinancialTrendService {
    List<DailyFinancialTrendResponse> getDailyTrends(
            LocalDate startDate,
            LocalDate endDate
    );
}
