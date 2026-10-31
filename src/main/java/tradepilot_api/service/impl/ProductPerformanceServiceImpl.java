
        package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.ProductPerformanceResponse;
import tradepilot_api.repository.ProductPerformanceProjection;
import tradepilot_api.repository.SaleRepository;
import tradepilot_api.service.ProductPerformanceService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPerformanceServiceImpl
        implements ProductPerformanceService {

    private final SaleRepository saleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductPerformanceResponse> getProductPerformance(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null && endDate != null
                && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date must not be before start date"
            );
        }

        LocalDateTime start = startDate == null
                ? null
                : startDate.atStartOfDay();

        LocalDateTime end = endDate == null
                ? null
                : endDate.plusDays(1).atStartOfDay();

        List<ProductPerformanceProjection> results;

        if (start == null && end == null) {
            results = saleRepository.findAllProductPerformance();
        } else if (start != null && end != null) {
            results = saleRepository.findProductPerformanceBetween(start, end);
        } else {
            throw new IllegalArgumentException(
                    "Start date and end date must either both be provided or both be omitted"
            );
        }
        return results.stream()
                .map(result -> new ProductPerformanceResponse(
                        result.getInventoryItemId(),
                        result.getProductName(),
                        result.getSku(),
                        result.getCurrency(),
                        result.getTotalQuantitySold(),
                        result.getTotalRevenue(),
                        result.getAverageSellingPrice(),
                        result.getLastSoldAt()
                ))
                .toList();
    }

    @Override
    public List<ProductPerformanceResponse> getBestSellingProducts() {

            return saleRepository.findBestSellingProducts()
                    .stream()
                    .map(result -> new ProductPerformanceResponse(
                            result.getInventoryItemId(),
                            result.getProductName(),
                            result.getSku(),
                            result.getCurrency(),
                            result.getTotalQuantitySold(),
                            result.getTotalRevenue(),
                            result.getAverageSellingPrice(),
                            result.getLastSoldAt()
                    ))
                    .toList();

    }
}
