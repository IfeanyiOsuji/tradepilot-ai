package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.BusinessSummary;

import java.util.List;

@Service
public interface BusinessSummaryService {
    public List<BusinessSummary> getSummary();
}
