package tradepilot_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.InventoryValuationResponse;
import tradepilot_api.repository.InventoryRepository;
import tradepilot_api.repository.InventoryValuationProjection;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryValuationServiceImpl implements InventoryValuationService {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<InventoryValuationResponse> getInventoryValuation() {

        return inventoryRepository.calculateInventoryValuation()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InventoryValuationResponse toResponse(
            InventoryValuationProjection projection) {

        BigDecimal potentialGrossMargin =
                projection.getTotalRetailValue()
                        .subtract(projection.getTotalCostValue());

        return new InventoryValuationResponse(
                projection.getCurrency(),
                projection.getTotalCostValue(),
                projection.getTotalRetailValue(),
                potentialGrossMargin,
                projection.getProductCount(),
                projection.getTotalQuantityInStock()
        );
    }
}