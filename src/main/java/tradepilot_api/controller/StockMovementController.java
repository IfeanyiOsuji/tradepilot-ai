package tradepilot_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tradepilot_api.dto.StockMovementRequest;
import tradepilot_api.dto.StockMovementResponse;
import tradepilot_api.entity.StockMovement;
import tradepilot_api.entity.StockMovementType;
import tradepilot_api.service.StockMovementService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stock-movements")
public class StockMovementController {
    private final StockMovementService stockMovementService;

    @PostMapping("/receive")
    public StockMovementResponse receiveStock(
            @Valid @RequestBody StockMovementRequest request) {

        return stockMovementService.receiveStock(
                request.inventoryItemId(),
                request.quantity(),
                request.description()
        );
    }

    @PostMapping("/sale")
    public StockMovementResponse recordSale(
            @Valid @RequestBody StockMovementRequest request) {

        return stockMovementService.recordSale(
                request.inventoryItemId(),
                request.quantity(),
                request.description()
        );
    }

    @PostMapping("/adjust")
    public StockMovementResponse adjustStock(
            @RequestParam StockMovementType movementType,
            @Valid @RequestBody StockMovementRequest request) {

        return stockMovementService.adjustStock(
                request.inventoryItemId(),
                request.quantity(),
                movementType,
                request.description()
        );
    }

    @GetMapping("/inventory/{inventoryItemId}")
    public List<StockMovementResponse> getMovementHistory(
            @PathVariable Long inventoryItemId) {

        return stockMovementService.getMovementHistory(inventoryItemId);
    }
}
