
package tradepilot_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import tradepilot_api.dto.InventorySaleRequest;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.entity.User;
import tradepilot_api.repository.InventoryRepository;
import tradepilot_api.repository.SaleRepository;
import tradepilot_api.repository.StockMovementRepository;
import tradepilot_api.repository.UserRepository;
import tradepilot_api.service.CurrentUserService;
import tradepilot_api.service.SaleService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@WithMockUser(roles = "USER")
class InventorySaleTransactionTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private CurrentUserService currentUserService;

    private User testUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFullName("Inventory Test User");
        user.setEmail("inventory-" + UUID.randomUUID() + "@example.com");
        user.setPassword("test-password-hash");
        user.setRole("ROLE_USER");

        testUser = userRepository.save(user);

        when(currentUserService.getCurrentUserId())
                .thenReturn(testUser.getId());
    }

    private InventoryItem createInventory(
            String productName,
            BigDecimal quantity
    ) {
        InventoryItem item = new InventoryItem(
                productName,
                "TEST-" + UUID.randomUUID(),
                quantity,
                new BigDecimal("20000"),
                new BigDecimal("25000"),
                "NGN",
                new BigDecimal("2"),
                new BigDecimal("20")
        );

        item.setOwner(testUser);

        return inventoryRepository.save(item);
    }

    @Test
    void shouldRollbackSaleWhenStockIsInsufficient() {

        InventoryItem savedItem = createInventory(
                "Rollback Test Rice",
                new BigDecimal("5")
        );

        long salesBefore = saleRepository.count();

        int movementsBefore = stockMovementRepository
                .findByInventoryItemIdOrderByCreatedAtDesc(
                        savedItem.getId()
                )
                .size();

        InventorySaleRequest request = new InventorySaleRequest(
                savedItem.getId(),
                new BigDecimal("6"),
                new BigDecimal("25000"),
                LocalDateTime.now()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleService.recordInventorySale(request)
        );

        assertEquals(salesBefore, saleRepository.count());

        InventoryItem reloadedItem = inventoryRepository
                .findById(savedItem.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("5").compareTo(
                        reloadedItem.getQuantityInStock()
                ),
                "Inventory quantity must remain unchanged"
        );

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

        InventoryItem savedItem = createInventory(
                "Successful Sale Test Rice",
                new BigDecimal("10")
        );

        long salesBefore = saleRepository.count();

        InventorySaleRequest request = new InventorySaleRequest(
                savedItem.getId(),
                new BigDecimal("3"),
                new BigDecimal("25000"),
                LocalDateTime.now()
        );

        var savedSale = saleService.recordInventorySale(request);

        assertNotNull(savedSale.getId());
        assertEquals(testUser.getId(), savedSale.getOwner().getId());
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
                .findByInventoryItemIdOrderByCreatedAtDesc(
                        savedItem.getId()
                );

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

    @Test
    void shouldRejectSaleOfAnotherUsersInventory() {

        User anotherUser = new User();
        anotherUser.setFullName("Another Test User");
        anotherUser.setEmail(
                "another-" + UUID.randomUUID() + "@example.com"
        );
        anotherUser.setPassword("test-password-hash");
        anotherUser.setRole("ROLE_USER");

        anotherUser = userRepository.save(anotherUser);

        InventoryItem item = createInventory(
                "Other User Inventory",
                new BigDecimal("10")
        );

        item.setOwner(anotherUser);
        inventoryRepository.save(item);

        InventorySaleRequest request = new InventorySaleRequest(
                item.getId(),
                new BigDecimal("2"),
                new BigDecimal("25000"),
                LocalDateTime.now()
        );

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> saleService.recordInventorySale(request)
        );

        InventoryItem reloaded = inventoryRepository
                .findById(item.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("10").compareTo(
                        reloaded.getQuantityInStock()
                )
        );
    }
}
