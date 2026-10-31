package tradepilot_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tradepilot_api.dto.InventoryRequest;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping
    public InventoryItem createInventoryItem(
            @Valid @RequestBody InventoryRequest request) {

        return inventoryService.createInventoryItem(
                request.productName(),
                request.sku(),
                request.quantityInStock(),
                request.unitCost(),
                request.sellingPrice(),
                request.currency(),
                request.lowStockThreshold(),
                request.targetStockLevel()
        );
    }

    @GetMapping
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryService.getAllInventoryItems();
    }

    @GetMapping("/sku/{sku}")
    public InventoryItem getInventoryItemBySku(
            @PathVariable String sku) {
        return inventoryService.getInventoryItemBySku(sku);
    }

    @GetMapping("/search")
    public List<InventoryItem> searchInventoryItems(
            @RequestParam String productName) {
        return inventoryService.searchInventoryItems(productName);
    }

    @GetMapping("/low-stock")
    public List<InventoryItem> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }
}
