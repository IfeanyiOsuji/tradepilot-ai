package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.StockMovementResponse;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.entity.StockMovement;
import tradepilot_api.entity.StockMovementType;
import tradepilot_api.repository.InventoryRepository;
import tradepilot_api.repository.StockMovementRepository;
import tradepilot_api.service.StockMovementService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    @Override
    @Transactional
    public StockMovementResponse receiveStock(Long inventoryItemId, BigDecimal quantity, String description) {
        validatePositiveQuantity(quantity);

        InventoryItem item = getInventoryItemForUpdate(inventoryItemId);

        BigDecimal quantityBefore = item.getQuantityInStock();
        BigDecimal quantityAfter = quantityBefore.add(quantity);

        item.updateQuantityInStock(quantityAfter);

        StockMovement movement = new StockMovement(
                item,
                StockMovementType.STOCK_RECEIVED,
                quantity,
                quantityBefore,
                quantityAfter,
                description
        );

        return  toResponse(stockMovementRepository.save(movement));
    }

    @Override
    @Transactional
    public StockMovementResponse recordSale(Long inventoryItemId, BigDecimal quantity, String description) {
        validatePositiveQuantity(quantity);

        InventoryItem item = getInventoryItemForUpdate(inventoryItemId);

        BigDecimal quantityBefore = item.getQuantityInStock();

        if (quantityBefore.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product " + item.getSku()
                            + ". Available: " + quantityBefore
                            + ", requested: " + quantity
            );
        }

        BigDecimal quantityAfter = quantityBefore.subtract(quantity);

        item.updateQuantityInStock(quantityAfter);

        StockMovement movement = new StockMovement(
                item,
                StockMovementType.SALE,
                quantity,
                quantityBefore,
                quantityAfter,
                description
        );

        return toResponse(stockMovementRepository.save(movement));
    }

    @Override
    @Transactional
    public StockMovementResponse adjustStock(Long inventoryItemId, BigDecimal quantity, StockMovementType movementType, String description) {
        validatePositiveQuantity(quantity);

        if (movementType != StockMovementType.ADJUSTMENT_IN
                && movementType != StockMovementType.ADJUSTMENT_OUT) {
            throw new IllegalArgumentException(
                    "Adjustment type must be ADJUSTMENT_IN or ADJUSTMENT_OUT."
            );
        }

        InventoryItem item = getInventoryItemForUpdate(inventoryItemId);

        BigDecimal quantityBefore = item.getQuantityInStock();
        BigDecimal quantityAfter;

        if (movementType == StockMovementType.ADJUSTMENT_IN) {
            quantityAfter = quantityBefore.add(quantity);
        } else {
            if (quantityBefore.compareTo(quantity) < 0) {
                throw new IllegalArgumentException(
                        "Cannot remove more stock than is available."
                );
            }

            quantityAfter = quantityBefore.subtract(quantity);
        }

        item.updateQuantityInStock(quantityAfter);

        StockMovement movement = new StockMovement(
                item,
                movementType,
                quantity,
                quantityBefore,
                quantityAfter,
                description
        );

        return toResponse(stockMovementRepository.save(movement));
    }

    @Override
    @Transactional
    public List<StockMovementResponse> getMovementHistory(Long inventoryItemId) {
        if (inventoryItemId == null || inventoryItemId <= 0) {
            throw new IllegalArgumentException(
                    "A valid inventory item ID is required."
            );
        }

        if (!inventoryRepository.existsById(inventoryItemId)) {
            throw new IllegalArgumentException(
                    "No inventory item found with ID: " + inventoryItemId
            );
        }

        return stockMovementRepository
                .findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InventoryItem getInventoryItemForUpdate(Long inventoryItemId) {

        if (inventoryItemId == null || inventoryItemId <= 0) {
            throw new IllegalArgumentException(
                    "A valid inventory item ID is required."
            );
        }

        return inventoryRepository.findByIdForUpdate(inventoryItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No inventory item found with ID: " + inventoryItemId
                ));
    }

    private void validatePositiveQuantity(BigDecimal quantity) {

        if (quantity == null
                || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        InventoryItem item = movement.getInventoryItem();

        return new StockMovementResponse(
                movement.getId(),
                item.getId(),
                item.getProductName(),
                item.getSku(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getDescription(),
                movement.getCreatedAt()
        );
    }


}
