package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.StockMovementResponse;
import tradepilot_api.entity.StockMovement;
import tradepilot_api.entity.StockMovementType;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface StockMovementService {
    StockMovementResponse receiveStock(
            Long inventoryItemId,
            BigDecimal quantity,
            String description);

    StockMovementResponse recordSale(
            Long inventoryItemId,
            BigDecimal quantity,
            String description);

    StockMovementResponse adjustStock(
            Long inventoryItemId,
            BigDecimal quantity,
            StockMovementType movementType,
            String description);

    List<StockMovementResponse> getMovementHistory(Long inventoryItemId);
}
