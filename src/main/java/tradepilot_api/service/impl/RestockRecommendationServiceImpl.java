package tradepilot_api.service.impl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.RestockRecommendationResponse;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.repository.InventoryRepository;
import tradepilot_api.service.RestockRecommendationService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestockRecommendationServiceImpl
        implements RestockRecommendationService {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<RestockRecommendationResponse> getRestockRecommendations() {

        List<InventoryItem> lowStockItems =
                inventoryRepository.findAll().stream()
                        .filter(item ->
                                item.getQuantityInStock()
                                        .compareTo(item.getLowStockThreshold()) <= 0
                        )
                        .toList();

        return lowStockItems.stream()
                .map(this::toResponse)
                .toList();
    }

    private RestockRecommendationResponse toResponse(InventoryItem item) {

        BigDecimal recommendedRestockQuantity =
                item.getTargetStockLevel().subtract(item.getQuantityInStock());

        BigDecimal estimatedRestockCost =
                recommendedRestockQuantity.multiply(item.getUnitCost());

        return new RestockRecommendationResponse(
                item.getId(),
                item.getProductName(),
                item.getSku(),
                item.getCurrency(),
                item.getQuantityInStock(),
                item.getLowStockThreshold(),
                item.getTargetStockLevel(),
                recommendedRestockQuantity,
                item.getUnitCost(),
                estimatedRestockCost
        );
    }
}