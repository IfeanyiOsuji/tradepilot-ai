package tradepilot_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tradepilot_api.entity.StockMovement;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByInventoryItemIdOrderByCreatedAtDesc(
            Long inventoryItemId
    );
}
