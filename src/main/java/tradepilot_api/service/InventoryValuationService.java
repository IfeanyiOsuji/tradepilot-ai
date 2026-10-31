package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.InventoryValuationResponse;

import java.util.List;

@Service
public interface InventoryValuationService {
    List<InventoryValuationResponse> getInventoryValuation();
}
