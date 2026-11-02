
package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tradepilot_api.dto.ProductPerformanceResponse;
import tradepilot_api.repository.ProductPerformanceProjection;
import tradepilot_api.repository.SaleRepository;
import tradepilot_api.service.CurrentUserService;
import tradepilot_api.service.ProductPerformanceService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPerformanceServiceImpl
        implements ProductPerformanceService {

    private final SaleRepository saleRepository;
    private final CurrentUserService currentUserService;

    @Override
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

        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException(
                    "Start date and end date must either both be provided or both be omitted"
            );
        }

        LocalDateTime start = startDate == null
                ? null
                : startDate.atStartOfDay();

        LocalDateTime end = endDate == null
                ? null
                : endDate.plusDays(1).atStartOfDay();

        List<ProductPerformanceProjection> results;

        boolean isAdmin = currentUserService.isAdmin();

        if (start == null) {

            // No date range specified.
            if (isAdmin) {
                results = saleRepository.findAllProductPerformance();
            } else {
                Long ownerId = currentUserService.getCurrentUserId();

                results = saleRepository
                        .findAllProductPerformanceByOwner(ownerId);
            }

        } else {

            // Both start and end dates are specified.
            if (isAdmin) {
                results = saleRepository
                        .findProductPerformanceBetween(start, end);
            } else {
                Long ownerId = currentUserService.getCurrentUserId();

                results = saleRepository
                        .findProductPerformanceBetweenByOwner(
                                start,
                                end,
                                ownerId
                        );
            }
        }

        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProductPerformanceResponse> getBestSellingProducts() {

        List<ProductPerformanceProjection> results;

        if (currentUserService.isAdmin()) {

            results = saleRepository.findBestSellingProducts();

        } else {

            Long ownerId = currentUserService.getCurrentUserId();

            results = saleRepository
                    .findBestSellingProductsByOwner(ownerId);
        }

        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductPerformanceResponse toResponse(
            ProductPerformanceProjection result) {

        return new ProductPerformanceResponse(
                result.getInventoryItemId(),
                result.getProductName(),
                result.getSku(),
                result.getCurrency(),
                result.getTotalQuantitySold(),
                result.getTotalRevenue(),
                result.getAverageSellingPrice(),
                result.getLastSoldAt()
        );
    }
}
