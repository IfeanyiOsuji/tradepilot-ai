package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.ProductPerformanceResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public interface ProductPerformanceService {
    List<ProductPerformanceResponse> getProductPerformance(
            LocalDate startDate,
            LocalDate endDate
    );
    List<ProductPerformanceResponse> getBestSellingProducts();
}
