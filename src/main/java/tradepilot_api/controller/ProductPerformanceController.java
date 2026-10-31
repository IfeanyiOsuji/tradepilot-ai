
package tradepilot_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tradepilot_api.dto.ProductPerformanceResponse;
import tradepilot_api.service.ProductPerformanceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics/products")
public class ProductPerformanceController {

    private final ProductPerformanceService productPerformanceService;

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
}
