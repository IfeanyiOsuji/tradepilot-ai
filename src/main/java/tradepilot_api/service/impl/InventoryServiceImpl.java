package tradepilot_api.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.entity.User;
import tradepilot_api.repository.InventoryRepository;
import tradepilot_api.repository.UserRepository;
import tradepilot_api.service.CurrentUserService;
import tradepilot_api.service.InventoryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static tradepilot_api.utils.TradeUtil.SUPPORTED_CURRENCIES;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    @Override
    public InventoryItem createInventoryItem(String productName, String sku, BigDecimal quantityInStock, BigDecimal unitCost, BigDecimal sellingPrice, String currency, BigDecimal lowStockThreshold, BigDecimal targetStockLevel) {


        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required."
            );
        }

        if (productName.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Product name cannot exceed 150 characters."
            );
        }

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        sku = sku.trim().toUpperCase(Locale.ROOT);

        if (sku.length() > 50) {
            throw new IllegalArgumentException(
                    "SKU cannot exceed 50 characters."
            );
        }

        Long ownerId = currentUserService.getCurrentUserId();

        if (inventoryRepository.findBySkuAndOwnerId(sku, ownerId).isPresent()) {
            throw new IllegalArgumentException(
                    "A product with SKU " + sku + " already exists in your inventory."
            );
        }

        if (quantityInStock == null ||
                quantityInStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Quantity in stock cannot be negative."
            );
        }

        if (unitCost == null ||
                unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Unit cost cannot be negative."
            );
        }

        if (sellingPrice == null ||
                sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Selling price cannot be negative."
            );
        }

        if (lowStockThreshold == null ||
                lowStockThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Low-stock threshold cannot be negative."
            );
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required."
            );
        }

        currency = currency.trim().toUpperCase(Locale.ROOT);


        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currency
            );
        }

        if (targetStockLevel == null ||
                targetStockLevel.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Target stock level cannot be negative."
            );
        }

        if (targetStockLevel.compareTo(lowStockThreshold) < 0) {
            throw new IllegalArgumentException(
                    "Target stock level cannot be below the low-stock threshold."
            );
        }

        if (targetStockLevel.compareTo(quantityInStock) < 0) {
            throw new IllegalArgumentException(
                    "Target stock level cannot be below the current stock quantity."
            );
        }

        InventoryItem item = new InventoryItem(
                productName.trim(),
                sku,
                quantityInStock,
                unitCost,
                sellingPrice,
                currency,
                lowStockThreshold,
                targetStockLevel
        );
        item.setOwner(getAuthenticatedUser());

        return inventoryRepository.save(item);


    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getAllInventoryItems() {

        if (currentUserService.isAdmin()) {
            return inventoryRepository.findAll();
        }

        return inventoryRepository.findAllByOwnerId(
                currentUserService.getCurrentUserId()
        );
    }
    @Override
    @Transactional(readOnly = true)
    public InventoryItem getInventoryItemBySku(String sku) {

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        String normalizedSku = sku.trim().toUpperCase(Locale.ROOT);

        return inventoryRepository
                .findBySkuAndOwnerId(
                        normalizedSku,
                        currentUserService.getCurrentUserId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory item not found"
                ));
    }


    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> searchInventoryItems(String productName) {

        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required for searching."
            );
        }

        String normalizedName = productName.trim();

        if (currentUserService.isAdmin()) {
            return inventoryRepository
                    .findByProductNameContainingIgnoreCase(normalizedName);
        }

        return inventoryRepository
                .findByProductNameContainingIgnoreCaseAndOwnerId(
                        normalizedName,
                        currentUserService.getCurrentUserId()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getLowStockItems() {

        List<InventoryItem> items;

        if (currentUserService.isAdmin()) {
            items = inventoryRepository.findAll();
        } else {
            items = inventoryRepository.findAllByOwnerId(
                    currentUserService.getCurrentUserId()
            );
        }

        return items.stream()
                .filter(item ->
                        item.getQuantityInStock()
                                .compareTo(item.getLowStockThreshold()) <= 0
                )
                .toList();
    }

    private User getAuthenticatedUser() {
        Long userId = currentUserService.getCurrentUserId();

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }
}