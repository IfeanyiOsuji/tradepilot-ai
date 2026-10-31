package tradepilot_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.InactiveProductResponse;
import tradepilot_api.repository.InactiveProductProjection;
import tradepilot_api.repository.InventoryRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryAnalyticsServiceImpl implements InventoryAnalyticsService {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<InactiveProductResponse> getInactiveProducts(int days) {

        if (days <= 0) {
            throw new IllegalArgumentException(
                    "Inactivity period must be greater than zero"
            );
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        List<InactiveProductProjection> products =
                inventoryRepository.findInventoryProductsWithLastSaleDate();

        return products.stream()
                .filter(product ->
                        product.getLastSoldAt() == null
                                || !product.getLastSoldAt().isAfter(cutoffDate)
                )
                .map(product -> new InactiveProductResponse(
                        product.getInventoryItemId(),
                        product.getProductName(),
                        product.getSku(),
                        product.getCurrency(),
                        product.getQuantityInStock(),
                        product.getLowStockThreshold(),
                        product.getLastSoldAt(),
                        product.getLastSoldAt() == null
                ))
                .toList();
    }
}