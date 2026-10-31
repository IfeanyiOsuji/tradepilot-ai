package tradepilot_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tradepilot_api.dto.InventorySaleRequest;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.repository.InventoryRepository;
import tradepilot_api.repository.SaleRepository;
import tradepilot_api.repository.StockMovementRepository;
import tradepilot_api.service.SaleService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InventorySaleTransactionTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Test
    void shouldRollbackSaleWhenStockIsInsufficient() {

        // Arrange: create a test product with only 5 units.
        InventoryItem item = new InventoryItem(
                "Rollback Test Rice",
                "ROLLBACK-RICE-001",
                new BigDecimal("5"),
                new BigDecimal("20000"),
                new BigDecimal("25000"),
                "NGN",
                new BigDecimal("2")
        );

        InventoryItem savedItem = inventoryRepository.save(item);

        long salesBefore = saleRepository.count();

        int movementsBefore = stockMovementRepository
                .findByInventoryItemIdOrderByCreatedAtDesc(
                        savedItem.getId()
                )
                .size();

        // Act & Assert: attempt to sell 6 when only 5 exist.
        InventorySaleRequest request = new InventorySaleRequest(
                savedItem.getId(),
                new BigDecimal("6"),
                new BigDecimal("25000"),
                LocalDateTime.now(),
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleService.recordInventorySale(request)
        );

        // Assert: no sale was persisted.
        assertEquals(salesBefore, saleRepository.count());

        // Assert: inventory quantity is still 5.
        InventoryItem reloadedItem = inventoryRepository
                .findById(savedItem.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("5")
                        .compareTo(reloadedItem.getQuantityInStock()),
                "Inventory quantity must remain unchanged"
        );

        // Assert: no stock movement was created.
        int movementsAfter = stockMovementRepository
                .findByInventoryItemIdOrderByCreatedAtDesc(
                        savedItem.getId()
                )
                .size();

        assertEquals(
                movementsBefore,
                movementsAfter,
                "No stock movement should be recorded"
        );
    }

    @Test
    void shouldSaveSaleAndReduceInventoryWhenStockIsSufficient() {
        InventoryItem item = new InventoryItem(
                "Successful Sale Test Rice",
                "SUCCESS-RICE-001",
                new BigDecimal("10"),
                new BigDecimal("20000"),
                new BigDecimal("25000"),
                "NGN",
                new BigDecimal("2")
        );

        InventoryItem savedItem = inventoryRepository.save(item);

        long salesBefore = saleRepository.count();

        InventorySaleRequest request = new InventorySaleRequest(
                savedItem.getId(),
                new BigDecimal("3"),
                new BigDecimal("25000"),
                LocalDateTime.now()
        );

        var savedSale = saleService.recordInventorySale(request);

        assertNotNull(savedSale.getId());
        assertEquals(salesBefore + 1, saleRepository.count());

        InventoryItem reloadedItem = inventoryRepository
                .findById(savedItem.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("7").compareTo(
                        reloadedItem.getQuantityInStock()
                ),
                "Inventory should decrease from 10 to 7"
        );

        var movements = stockMovementRepository
                .findByInventoryItemIdOrderByCreatedAtDesc(savedItem.getId());

        assertEquals(1, movements.size());
        assertEquals(
                0,
                new BigDecimal("10").compareTo(
                        movements.get(0).getQuantityBefore()
                )
        );
        assertEquals(
                0,
                new BigDecimal("7").compareTo(
                        movements.get(0).getQuantityAfter()
                )
        );
    }
}
