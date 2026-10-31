
package tradepilot_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tradepilot_api.dto.InactiveProductResponse;
import tradepilot_api.dto.InventoryValuationResponse;
import tradepilot_api.dto.ProductPerformanceResponse;
import tradepilot_api.service.InventoryAnalyticsService;
import tradepilot_api.service.InventoryValuationService;
import tradepilot_api.service.ProductPerformanceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics/products")
public class ProductPerformanceController {

    private final ProductPerformanceService productPerformanceService;
    private final InventoryAnalyticsService inventoryAnalyticsService;
    private final InventoryValuationService inventoryValuationService;

    @GetMapping("/performance")
    public List<ProductPerformanceResponse> getProductPerformance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return productPerformanceService.getProductPerformance(
                startDate,
                endDate
        );
    }
    @GetMapping("/best-selling")
    public List<ProductPerformanceResponse> getBestSellingProducts() {
        return productPerformanceService.getBestSellingProducts();
    }

    @GetMapping("/inactive")
    public List<InactiveProductResponse> getInactiveProducts(
            @RequestParam(defaultValue = "30") int days) {

        return inventoryAnalyticsService.getInactiveProducts(days);
    }

    @GetMapping("/inventory-valuation")
    public List<InventoryValuationResponse> getInventoryValuation() {

        return inventoryValuationService.getInventoryValuation();
    }

}
