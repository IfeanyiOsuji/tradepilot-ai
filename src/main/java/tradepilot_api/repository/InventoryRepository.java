package tradepilot_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tradepilot_api.entity.InventoryItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByProductNameContainingIgnoreCase(
            String productName
    );

    List<InventoryItem> findByQuantityInStockLessThanEqual(
            java.math.BigDecimal threshold
    );
}
