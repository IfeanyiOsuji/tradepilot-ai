package tradepilot_api.service.impl;


import lombok.AllArgsConstructor;
import lombok.Locked;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.InventorySaleRequest;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.entity.Sale;
import tradepilot_api.entity.SaleType;
import tradepilot_api.repository.InventoryRepository;
import tradepilot_api.repository.SaleRepository;
import tradepilot_api.service.SaleService;
import tradepilot_api.service.StockMovementService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static tradepilot_api.utils.TradeUtil.SUPPORTED_CURRENCIES;

@Service

@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {



    private final SaleRepository saleRepository;
    private final StockMovementService stockMovementService;
    private final InventoryRepository inventoryRepository;

    @Transactional
    @Override
    public Sale recordSale(String itemName, String saleType, BigDecimal quantity, BigDecimal unitPrice, String currency, LocalDateTime soldAt) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }

        if (itemName.length() > 150) {
            throw new IllegalArgumentException("Item name must not exceed 150 characters");
        }

        if (saleType == null || (!saleType.equalsIgnoreCase("PRODUCT")
                && !saleType.equalsIgnoreCase("SERVICE"))) {
            throw new IllegalArgumentException("Sale type must be PRODUCT or SERVICE");
        }

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }

        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }

        String normalizedCurrency = currency.trim().toUpperCase(Locale.ROOT);

        if (!SUPPORTED_CURRENCIES.contains(normalizedCurrency)) {
            throw new IllegalArgumentException(
                    "Unsupported currency. Supported currencies: " + SUPPORTED_CURRENCIES
            );
        }

        if (soldAt == null) {
            throw new IllegalArgumentException("Sale date and time are required");
        }

        Sale sale = new Sale(
                itemName.trim(),
                tradepilot_api.entity.SaleType.valueOf(saleType.toUpperCase(Locale.ROOT)),
                quantity,
                unitPrice,
                normalizedCurrency,
                soldAt
        );

        return saleRepository.save(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    @Override
    public List<Sale> getSalesBetween(LocalDateTime start, LocalDateTime end) {
        return saleRepository
                .findBySoldAtGreaterThanEqualAndSoldAtLessThan(start, end);
    }

    @Override
    @Transactional
    public Sale recordInventorySale(InventorySaleRequest request) {
        InventoryItem item = inventoryRepository
                .findById(request.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found with ID: "
                                + request.inventoryItemId()
                ));

        Sale sale = new Sale(
                item.getProductName(),
                SaleType.PRODUCT,
                request.quantity(),
                request.unitPrice(),
                item.getCurrency(),
                request.soldAt()
        );

        // Save the sale within the current transaction.
        Sale savedSale = saleRepository.save(sale);

        // Reduce stock and record the movement.
        // StockMovementService joins this transaction.
        stockMovementService.recordSale(
                item.getId(),
                request.quantity(),
                "Sale recorded for " + item.getProductName()
        );

        return savedSale;
    }
}
