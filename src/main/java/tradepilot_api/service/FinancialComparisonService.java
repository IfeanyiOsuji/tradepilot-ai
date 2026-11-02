package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.FinancialComparisonResponse;

import java.util.List;

@Service
public interface FinancialComparisonService {
    List<FinancialComparisonResponse> compareWeeks();

    List<FinancialComparisonResponse> compareMonths();
}
