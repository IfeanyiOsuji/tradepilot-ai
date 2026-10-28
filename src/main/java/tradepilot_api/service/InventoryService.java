package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.entity.InventoryItem;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface InventoryService {
    InventoryItem createInventoryItem(
            String productName,
            String sku,
            BigDecimal quantityInStock,
            BigDecimal unitCost,
            BigDecimal sellingPrice,
            String currency,
            BigDecimal lowStockThreshold);

    List<InventoryItem> getAllInventoryItems();
    InventoryItem getInventoryItemBySku(String sku);
    List<InventoryItem> searchInventoryItems(String productName);
    List<InventoryItem> getLowStockItems();

}
