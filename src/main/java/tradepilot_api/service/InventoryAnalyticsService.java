package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.InactiveProductResponse;

import java.util.List;

@Service
public interface InventoryAnalyticsService {
    List<InactiveProductResponse> getInactiveProducts(int days);
}
