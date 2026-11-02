
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
        List<ProductPerformanceResponse> bestSellingProducts,
        List<InactiveProductResponse> inactiveProductResponses,
        List<InventoryValuationResponse> inventoryValuations,
        List<DailyFinancialTrendResponse> dailyFinancialTrends,
        List<FinancialComparisonResponse> financialComparisons
) {
    // Preserves existing constructor calls that provide 11 arguments.
    public AssistantResponse(
            String answer,
            String intent,
            LocalDate startDate,
            LocalDate endDate,
            List<BusinessSummary> summaries,
            List<LowStockItemResponse> lowStockItems,
            List<RestockRecommendationResponse> restockRecommendations,
            List<ProductPerformanceResponse> bestSellingProducts,
            List<InactiveProductResponse> inactiveProductResponses,
            List<InventoryValuationResponse> inventoryValuations,
            List<DailyFinancialTrendResponse> dailyFinancialTrends
    ) {
        this(
                answer,
                intent,
                startDate,
                endDate,
                summaries,
                lowStockItems,
                restockRecommendations,
                bestSellingProducts,
                inactiveProductResponses,
                inventoryValuations,
                dailyFinancialTrends,
                List.of()
        );
    }

    // Preserves existing constructor calls that provide 10 arguments.
    public AssistantResponse(
            String answer,
            String intent,
            LocalDate startDate,
            LocalDate endDate,
            List<BusinessSummary> summaries,
            List<LowStockItemResponse> lowStockItems,
            List<RestockRecommendationResponse> restockRecommendations,
            List<ProductPerformanceResponse> bestSellingProducts,
            List<InactiveProductResponse> inactiveProductResponses,
            List<InventoryValuationResponse> inventoryValuations
    ) {
        this(
                answer,
                intent,
                startDate,
                endDate,
                summaries,
                lowStockItems,
                restockRecommendations,
                bestSellingProducts,
                inactiveProductResponses,
                inventoryValuations,
                List.of(),
                List.of()
        );
    }
}
