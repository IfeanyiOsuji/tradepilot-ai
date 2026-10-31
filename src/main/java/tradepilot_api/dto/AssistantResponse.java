package tradepilot_api.dto;

import java.time.LocalDate;
import java.util.List;

public record AssistantResponse(
        String answer,
        String intent,
        LocalDate startDate,
        LocalDate endDate,
        List<BusinessSummary> summaries,
        List<LowStockItemResponse> lowStockItems,
        List<RestockRecommendationResponse> restockRecommendations,
        List<ProductPerformanceResponse> bestSellingProducts
) {
}