
package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tradepilot_api.dto.*;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.service.*;
import tradepilot_api.service.ai.MetaAiClient;
import tradepilot_api.utils.CurrencyFormatter;
import tradepilot_api.utils.TradeUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static tradepilot_api.utils.TradeUtil.extractInactivityDays;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private static final Logger log =
            LoggerFactory.getLogger(AssistantServiceImpl.class);

    private final BusinessSummaryService businessSummaryService;
    private final InventoryService inventoryService;
    private final MetaAiClient metaAiClient;
    private final RestockRecommendationService restockRecommendationService;
    private final ProductPerformanceService productPerformanceService;
    private final InventoryAnalyticsService inventoryAnalyticsService;
    private final InventoryValuationService inventoryValuationService;
    private final FinancialTrendService financialTrendService;
    private final FinancialComparisonService financialComparisonService;

    @Override
    public AssistantResponse answerQuestion(String message) {

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a business question."
            );
        }

        String question = message.trim().toLowerCase(Locale.ROOT);

        if (isFinancialComparisonQuestion(question)) {
            return answerFinancialComparisonQuestion(message, question);
        }

        if (isDailyFinancialTrendQuestion(question)) {
            return answerDailyFinancialTrendQuestion(message, question);
        }

        if (isLowStockQuestion(question)) {
            return answerLowStockQuestion(message);
        }

        if (isInactiveProductsQuestion(question)) {
            return answerInactiveProductsQuestion(question);
        }
        if (isRestockQuestion(question)) {
            return answerRestockQuestion(message);
        }

        if (isBestSellingQuestion(question)) {
            return answerBestSellingQuestion(message);
        }

        if (isInventoryValuationQuestion(question)) {
            return answerInventoryValuationQuestion(question);
        }
        if (isRevenueQuestion(question)) {
            return answerFinancialQuestion(message, question, "REVENUE");
        }

        if (isExpenseQuestion(question)) {
            return answerFinancialQuestion(message, question, "EXPENSES");
        }

        if (isCashFlowQuestion(question)) {
            return answerFinancialQuestion(message, question, "NET_CASH_FLOW");
        }

        if (isSummaryQuestion(question)) {
            return answerFinancialQuestion(message, question, "SUMMARY");
        }




        return new AssistantResponse(
                "I cannot answer that question yet. Try asking about "
                        + "revenue, expenses, net cash flow, or low-stock products.",
                "UNSUPPORTED",
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private AssistantResponse answerFinancialComparisonQuestion(
            String message,
            String question) {

        List<FinancialComparisonResponse> comparisons;

        String normalizedQuestion = question.toLowerCase(Locale.ROOT);

        boolean monthly = normalizedQuestion.contains("month")
                || normalizedQuestion.contains("monthly");

        if (monthly) {
            comparisons = financialComparisonService.compareMonths();
        } else {
            comparisons = financialComparisonService.compareWeeks();
        }

        if (comparisons.isEmpty()) {
            return new AssistantResponse(
                    "There is not enough recorded financial data to compare these periods.",
                    monthly ? "MONTHLY_COMPARISON" : "WEEKLY_COMPARISON",
                    null,
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    comparisons
            );
        }

        String prompt = buildFinancialComparisonPrompt(
                message,
                comparisons,
                monthly
        );

        String fallbackAnswer = buildFinancialComparisonAnswer(
                comparisons,
                monthly
        );

        String aiAnswer = generateAiAnswer(prompt, fallbackAnswer);

        return new AssistantResponse(
                aiAnswer,
                monthly ? "MONTHLY_COMPARISON" : "WEEKLY_COMPARISON",
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                comparisons
        );
    }

    private boolean isFinancialComparisonQuestion(String question) {

        boolean hasComparisonKeyword =
                question.contains("compare")
                        || question.contains("comparison")
                        || question.contains("compared with")
                        || question.contains("compared to")
                        || question.contains("versus")
                        || question.contains(" vs ")
                        || question.contains("vs.")
                        || question.contains("change from")
                        || question.contains("change between")
                        || question.contains("improve")
                        || question.contains("increased")
                        || question.contains("decreased");

        boolean hasPeriodKeyword =
                question.contains("week")
                        || question.contains("weekly")
                        || question.contains("month")
                        || question.contains("monthly");

        return hasComparisonKeyword && hasPeriodKeyword;
    }

    private String buildFinancialComparisonPrompt(
            String question,
            List<FinancialComparisonResponse> comparisons,
            boolean monthly) {

        StringBuilder facts = new StringBuilder();

        for (FinancialComparisonResponse comparison : comparisons) {

            facts.append("\nCurrency: ")
                    .append(comparison.currency());

            facts.append("\nCurrent period: ")
                    .append(comparison.currentPeriodStart())
                    .append(" to ")
                    .append(comparison.currentPeriodEnd());

            facts.append("\nPrevious period: ")
                    .append(comparison.previousPeriodStart())
                    .append(" to ")
                    .append(comparison.previousPeriodEnd());

            facts.append("\nCurrent revenue: ")
                    .append(CurrencyFormatter.format(
                            comparison.currentRevenue(),
                            comparison.currency()
                    ));

            facts.append("\nPrevious revenue: ")
                    .append(CurrencyFormatter.format(
                            comparison.previousRevenue(),
                            comparison.currency()
                    ));

            facts.append("\nRevenue change: ")
                    .append(CurrencyFormatter.format(
                            comparison.revenueChange(),
                            comparison.currency()
                    ));

            facts.append("\nRevenue percentage change: ")
                    .append(formatPercentage(
                            comparison.revenueChangePercentage()
                    ));

            facts.append("\nCurrent expenses: ")
                    .append(CurrencyFormatter.format(
                            comparison.currentExpenses(),
                            comparison.currency()
                    ));

            facts.append("\nPrevious expenses: ")
                    .append(CurrencyFormatter.format(
                            comparison.previousExpenses(),
                            comparison.currency()
                    ));

            facts.append("\nExpense change: ")
                    .append(CurrencyFormatter.format(
                            comparison.expenseChange(),
                            comparison.currency()
                    ));

            facts.append("\nExpense percentage change: ")
                    .append(formatPercentage(
                            comparison.expenseChangePercentage()
                    ));

            facts.append("\nCurrent net cash flow: ")
                    .append(CurrencyFormatter.format(
                            comparison.currentNetCashFlow(),
                            comparison.currency()
                    ));

            facts.append("\nPrevious net cash flow: ")
                    .append(CurrencyFormatter.format(
                            comparison.previousNetCashFlow(),
                            comparison.currency()
                    ));

            facts.append("\nNet cash flow change: ")
                    .append(CurrencyFormatter.format(
                            comparison.netCashFlowChange(),
                            comparison.currency()
                    ));

            facts.append("\nNet cash flow percentage change: ")
                    .append(formatPercentage(
                            comparison.netCashFlowChangePercentage()
                    ));

            facts.append("\n--------------------\n");
        }

        return """
            You are TradePilot AI, a business financial assistant.

            Answer the user's question using only the verified
            financial comparison data supplied below.

            Comparison type: %s

            User question: %s

            Verified financial data:
            %s

            Instructions:
            1. Do not invent or recalculate any financial figures.
            2. Preserve all supplied monetary amounts and percentages exactly.
            3. Do not combine amounts from different currencies.
            4. Explain whether revenue increased or decreased.
            5. Explain whether expenses increased or decreased.
            6. Explain whether net cash flow improved or declined.
            7. An increase in expenses means the business spent more;
               it is not automatically a positive result.
            8. A negative net cash flow change means cash flow declined.
            9. If a percentage change is N/A, explain that the previous
               period had a zero value, so percentage change is undefined.
            10. The current week or month may still be in progress.
                Do not describe it as a completed period.
            11. Net cash flow is not the same as accounting profit.
            12. Keep the explanation clear, practical and concise.

            Provide useful business insights without making unsupported
            assumptions.
            
            Do not include empty headings or sections.
            Only include a heading when you have relevant information to present.
            
            IMPORTANT: When the current period has zero revenue and zero expenses,
            do not automatically describe this as a business decline or poor performance.
            Explain that no transactions have been recorded for the period so far.
            If the period is still in progress, describe the comparison as provisional.
            """.formatted(
                monthly ? "Monthly comparison" : "Weekly comparison",
                question,
                facts
        );
    }

    private String formatPercentage(BigDecimal percentage) {

        if (percentage == null) {
            return "N/A";
        }

        return percentage
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }

    private String buildFinancialComparisonAnswer(
            List<FinancialComparisonResponse> comparisons,
            boolean monthly) {

        StringBuilder answer = new StringBuilder();



        answer.append(monthly
                ? "Monthly financial comparison"
                : "Weekly financial comparison");

        answer.append("\n");

        for (FinancialComparisonResponse comparison : comparisons) {

            answer.append("\nCurrency: ")
                    .append(comparison.currency());

            answer.append("\nCurrent period revenue: ")
                    .append(CurrencyFormatter.format(
                            comparison.currentRevenue(),
                            comparison.currency()
                    ));

            answer.append("\nPrevious period revenue: ")
                    .append(CurrencyFormatter.format(
                            comparison.previousRevenue(),
                            comparison.currency()
                    ));

            BigDecimal revenueChange = comparison.revenueChange();

            String revenueChangeText = revenueChange.compareTo(BigDecimal.ZERO) < 0
                    ? "decreased by " + CurrencyFormatter.format(
                    revenueChange.abs(), comparison.currency())
                    : "increased by " + CurrencyFormatter.format(
                    revenueChange, comparison.currency());

            answer.append("\nRevenue change: ")
                    .append(revenueChangeText);

            answer.append(" (")
                    .append(formatPercentage(
                            comparison.revenueChangePercentage()
                    ))
                    .append(")");

            answer.append("\nCurrent period expenses: ")
                    .append(CurrencyFormatter.format(
                            comparison.currentExpenses(),
                            comparison.currency()
                    ));

            answer.append("\nPrevious period expenses: ")
                    .append(CurrencyFormatter.format(
                            comparison.previousExpenses(),
                            comparison.currency()
                    ));

            answer.append("\nExpense change: ")
                    .append(CurrencyFormatter.format(
                            comparison.expenseChange(),
                            comparison.currency()
                    ));

            answer.append(" (")
                    .append(formatPercentage(
                            comparison.expenseChangePercentage()
                    ))
                    .append(")");

            answer.append("\nCurrent net cash flow: ")
                    .append(CurrencyFormatter.format(
                            comparison.currentNetCashFlow(),
                            comparison.currency()
                    ));

            answer.append("\nPrevious net cash flow: ")
                    .append(CurrencyFormatter.format(
                            comparison.previousNetCashFlow(),
                            comparison.currency()
                    ));

            answer.append("\nNet cash flow change: ")
                    .append(CurrencyFormatter.format(
                            comparison.netCashFlowChange(),
                            comparison.currency()
                    ));

            answer.append(" (")
                    .append(formatPercentage(
                            comparison.netCashFlowChangePercentage()
                    ))
                    .append(")\n");
        }

        return answer.toString();
    }


    private AssistantResponse answerRestockQuestion(String question) {

        List<RestockRecommendationResponse> recommendations =
                restockRecommendationService.getRestockRecommendations();

        String fallbackAnswer;

        if (recommendations.isEmpty()) {
            fallbackAnswer = "No products currently meet the low-stock "
                    + "criteria for a restock recommendation.";
        } else {
            fallbackAnswer = "Found " + recommendations.size()
                    + " product(s) requiring restocking:\n"
                    + recommendations.stream()
                    .map(item -> item.productName()
                            + " (SKU: " + item.sku()
                            + ") - Current stock: "
                            + item.quantityInStock().toPlainString()
                            + ", Target stock: "
                            + item.targetStockLevel().toPlainString()
                            + ", Recommended restock: "
                            + item.recommendedRestockQuantity().toPlainString()
                            + ", Unit cost: "
                            + CurrencyFormatter.format(
                            item.unitCost(),
                            item.currency()
                    )
                            + ", Estimated restock cost: "
                            + CurrencyFormatter.format(
                            item.estimatedRestockCost(),
                            item.currency()
                    ))
                    .collect(Collectors.joining("\n"));
        }

        String prompt = buildRestockPrompt(question, recommendations);

        String answer = generateAiAnswer(prompt, fallbackAnswer);

        return new AssistantResponse(
                answer,
                "RESTOCK_RECOMMENDATIONS",
                null,
                null,
                List.of(),
                List.of(),
                recommendations,
                List.of(),
                List.of(),
                List.of()
        );
    }
    private AssistantResponse answerFinancialQuestion(
            String originalQuestion,
            String question,
            String intent) {

        DateRange dateRange = resolveDateRange(question);

        List<BusinessSummary> summaries;

        if (dateRange.startDate() == null) {
            summaries = businessSummaryService.getSummary();
        } else {
            summaries = businessSummaryService.getSummary(
                    dateRange.startDate(),
                    dateRange.endDate()
            );
        }

        // Deterministic answer remains available as a fallback.
        String fallbackAnswer = buildFinancialAnswer(
                summaries,
                intent,
                dateRange
        );

        // Muse Spark explains the backend-calculated figures.
        String answer = generateAiAnswer(
                buildFinancialPrompt(
                        originalQuestion,
                        intent,
                        dateRange,
                        summaries
                ),
                fallbackAnswer
        );

        return new AssistantResponse(
                answer,
                intent,
                dateRange.startDate(),
                dateRange.endDate(),
                summaries,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private AssistantResponse answerBestSellingQuestion(String question) {

        List<ProductPerformanceResponse> bestSellingProducts =
                productPerformanceService.getBestSellingProducts();

        String fallbackAnswer;

        if (bestSellingProducts.isEmpty()) {
            fallbackAnswer = "No inventory-linked product sales have been recorded yet.";
        } else {
            fallbackAnswer = "Your best-selling products are:\n"
                    + bestSellingProducts.stream()
                    .map(item -> item.productName()
                            + " (SKU: " + item.sku() + ")"
                            + " - Quantity sold: "
                            + item.totalQuantitySold().toPlainString()
                            + ", Revenue: "
                            + CurrencyFormatter.format(
                            item.totalRevenue(),
                            item.currency()
                    )
                            + ", Average selling price: "
                            + CurrencyFormatter.format(
                            item.averageSellingPrice(),
                            item.currency()
                    ))
                    .collect(Collectors.joining("\n"));
        }

        String prompt = buildBestSellingPrompt(question, bestSellingProducts);

        String answer = generateAiAnswer(prompt, fallbackAnswer);

        return new AssistantResponse(
                answer,
                "BEST_SELLING_PRODUCTS",
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                bestSellingProducts,
                List.of(),
                List.of()
        );
    }
    private AssistantResponse answerInventoryValuationQuestion(String question) {

        List<InventoryValuationResponse> valuations =
                inventoryValuationService.getInventoryValuation();

        String fallbackAnswer;

        if (valuations.isEmpty()) {
            fallbackAnswer = "There is no inventory valuation data available.";
        } else {
            fallbackAnswer = "Your current inventory valuation:\n"
                    + valuations.stream()
                    .map(item -> "Currency: " + item.currency()
                            + "\nTotal cost value: "
                            + CurrencyFormatter.format(
                            item.totalCostValue(),
                            item.currency()
                    )
                            + "\nTotal retail value: "
                            + CurrencyFormatter.format(
                            item.totalRetailValue(),
                            item.currency()
                    )
                            + "\nPotential gross margin: "
                            + CurrencyFormatter.format(
                            item.potentialGrossMargin(),
                            item.currency()
                    )
                            + "\nProduct count: "
                            + item.productCount()
                            + "\nTotal quantity in stock: "
                            + item.totalQuantityInStock().toPlainString())
                    .collect(Collectors.joining("\n\n"));
        }

        String prompt = buildInventoryValuationPrompt(question, valuations);

        String answer = generateAiAnswer(prompt, fallbackAnswer);

        return new AssistantResponse(
                answer,
                "INVENTORY_VALUATION",
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                valuations
        );
    }

    private String buildInventoryValuationPrompt(
            String question,
            List<InventoryValuationResponse> valuations) {

        String valuationData = valuations.isEmpty()
                ? "No inventory valuation data is available."
                : valuations.stream()
                .map(item -> """
                    Currency: %s
                    Total cost value: %s
                    Total retail value: %s
                    Potential gross margin: %s
                    Product count: %d
                    Total quantity in stock: %s
                    """.formatted(
                        item.currency(),
                        CurrencyFormatter.format(
                                item.totalCostValue(),
                                item.currency()
                        ),
                        CurrencyFormatter.format(
                                item.totalRetailValue(),
                                item.currency()
                        ),
                        CurrencyFormatter.format(
                                item.potentialGrossMargin(),
                                item.currency()
                        ),
                        item.productCount(),
                        item.totalQuantityInStock().toPlainString()
                ))
                .collect(Collectors.joining("\n"));

        return """
            You are TradePilot AI, an AI business assistant.

            The user's question:
            %s

            Verified inventory valuation data:
            %s

            Instructions:
            - Answer using only the supplied inventory valuation data.
            - Do not invent financial figures.
            - Do not calculate, recalculate, or modify supplied figures.
            - Preserve the exact amounts and currency codes supplied.
            - Use the supplied formatted monetary amounts exactly as written.
            - Do not remove currency symbols or thousands separators.
            - Explain that total cost value represents the cost of current stock.
            - Explain that total retail value represents the potential selling
              value of current stock at listed selling prices.
            - Explain that potential gross margin is not realized profit.
            - Keep different currencies separate. Never combine different currencies.
            - Keep the answer concise and relevant to the user's question.
            """.formatted(question, valuationData);
    }

    private String buildFinancialPrompt(
            String question,
            String intent,
            DateRange dateRange,
            List<BusinessSummary> summaries) {

        StringBuilder data = new StringBuilder();

        data.append("Business data retrieved from the database:\n");
        data.append("Period: ").append(dateRange.description()).append("\n");
        data.append("Intent: ").append(intent).append("\n");

        if (summaries.isEmpty()) {
            data.append("No recorded business transactions were found.");
        } else {
            for (BusinessSummary summary : summaries) {

                data.append("\nCurrency: ")
                        .append(summary.currency());

                data.append("\nTotal revenue: ")
                        .append(CurrencyFormatter.format(
                                summary.totalRevenue(),
                                summary.currency()
                        ));

                data.append("\nTotal expenses: ")
                        .append(CurrencyFormatter.format(
                                summary.totalExpenses(),
                                summary.currency()
                        ));

                data.append("\nNet cash flow: ")
                        .append(CurrencyFormatter.format(
                                summary.netCashFlow(),
                                summary.currency()
                        ));

                data.append("\n");
            }
        }

        return """
            You are TradePilot AI, a helpful business assistant
            for small businesses.

            Answer the user's business question using only the
            business data supplied below.

            STRICT RULES:
            1. Never invent or estimate financial figures.
            2. Never change any supplied numerical values.
            3. Do not perform your own financial calculations.
            4. Preserve the exact formatted monetary amounts supplied.
            5. Do not remove currency symbols or thousands separators.
            6. If data is missing, clearly say so.
            7. Distinguish net cash flow from accounting profit.
            8. Do not describe net cash flow as profit.
            9. Use the currency provided with each figure.
            10. Give a clear, concise and useful explanation.
            11. Do not claim access to information not supplied.

            User's question:
            %s

            %s

            Provide a natural-language answer to the user's question.
            """.formatted(question, data);
    }
    private String buildBestSellingPrompt(
            String question,
            List<ProductPerformanceResponse> products) {

        StringBuilder data = new StringBuilder();

        if (products.isEmpty()) {
            data.append("No inventory-linked product sales have been recorded.");
        } else {
            for (ProductPerformanceResponse product : products) {

                data.append("\nProduct: ").append(product.productName());

                data.append("\nSKU: ").append(product.sku());

                data.append("\nTotal quantity sold: ")
                        .append(product.totalQuantitySold().toPlainString());

                data.append("\nTotal revenue: ")
                        .append(CurrencyFormatter.format(
                                product.totalRevenue(),
                                product.currency()
                        ));

                data.append("\nAverage selling price: ")
                        .append(CurrencyFormatter.format(
                                product.averageSellingPrice(),
                                product.currency()
                        ));

                data.append("\nLast sold at: ")
                        .append(product.lastSoldAt());

                data.append("\n");
            }
        }

        return """
            You are TradePilot AI, a helpful business assistant
            for small businesses.

            Answer the user's question using only the product sales
            data supplied below.

            STRICT RULES:
            1. Never invent products, sales quantities, or revenue figures.
            2. Never change the supplied numerical values.
            3. Do not calculate or modify sales figures.
            4. Preserve the exact formatted monetary amounts supplied.
            5. Do not remove currency symbols or thousands separators.
            6. Use the currency provided with each monetary amount.
            7. Distinguish quantity sold from revenue generated.
            8. Identify the best-selling products using the supplied data.
            9. If no products are listed, clearly say no inventory-linked
               product sales have been recorded.
            10. Do not claim access to sales records not supplied.
            11. Keep the answer clear and concise.
            12. Never combine amounts from different currencies.

            User's question:
            %s

            Product sales data:
            %s

            Provide a natural-language answer.
            """.formatted(question, data);
    }

    private String buildFinancialAnswer(
            List<BusinessSummary> summaries,
            String intent,
            DateRange dateRange) {

        if (summaries.isEmpty()) {
            return "No recorded business transactions were found "
                    + dateRange.description() + ".";
        }

        String period = dateRange.description();

        if ("SUMMARY".equals(intent)) {
            return "Business summary " + period + ":\n"
                    + summaries.stream()
                    .map(summary ->
                            summary.currency()
                                    + " - Revenue: "
                                    + CurrencyFormatter.format(
                                    summary.totalRevenue(),
                                    summary.currency()
                            )
                                    + ", Expenses: "
                                    + CurrencyFormatter.format(
                                    summary.totalExpenses(),
                                    summary.currency()
                            )
                                    + ", Net cash flow: "
                                    + CurrencyFormatter.format(
                                    summary.netCashFlow(),
                                    summary.currency()
                            )
                    )
                    .collect(Collectors.joining("\n"));
        }

        String label = switch (intent) {
            case "REVENUE" -> "Recorded revenue";
            case "EXPENSES" -> "Recorded expenses";
            case "NET_CASH_FLOW" -> "Net cash flow";
            default -> "Business total";
        };

        return label + " " + period + ":\n"
                + summaries.stream()
                .map(summary -> summary.currency() + " "
                        + CurrencyFormatter.format(
                        switch (intent) {
                            case "REVENUE" -> summary.totalRevenue();
                            case "EXPENSES" -> summary.totalExpenses();
                            case "NET_CASH_FLOW" -> summary.netCashFlow();
                            default -> java.math.BigDecimal.ZERO;
                        },
                        summary.currency()
                ))
                .collect(Collectors.joining("\n"));
    }
    private AssistantResponse answerLowStockQuestion(String question) {

        List<LowStockItemResponse> lowStockItems =
                inventoryService.getLowStockItems()
                        .stream()
                        .map(this::toLowStockResponse)
                        .toList();

        String fallbackAnswer;

        if (lowStockItems.isEmpty()) {
            fallbackAnswer = "There are no products at or below their "
                    + "low-stock thresholds.";
        } else {
            fallbackAnswer = "Found " + lowStockItems.size()
                    + " product(s) at or below their low-stock thresholds:\n"
                    + lowStockItems.stream()
                    .map(item -> item.productName()
                            + " (SKU: " + item.sku()
                            + ") - stock: "
                            + item.quantityInStock().toPlainString()
                            + ", threshold: "
                            + item.lowStockThreshold().toPlainString())
                    .collect(Collectors.joining("\n"));
        }

        String prompt = buildLowStockPrompt(question, lowStockItems);

        String answer = generateAiAnswer(prompt, fallbackAnswer);

        return new AssistantResponse(
                answer,
                "LOW_STOCK",
                null,
                null,
                List.of(),
                lowStockItems,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private AssistantResponse answerInactiveProductsQuestion(String question) {

        int days = extractInactivityDays(question);

        List<InactiveProductResponse> inactiveProducts =
                inventoryAnalyticsService.getInactiveProducts(days);

        String fallbackAnswer;

        if (inactiveProducts.isEmpty()) {
            fallbackAnswer = "There are no products that have been inactive "
                    + "for the last " + days + " days.";
        } else {
            fallbackAnswer = "Found " + inactiveProducts.size()
                    + " inactive product(s) in the last " + days + " days:\n"
                    + inactiveProducts.stream()
                    .map(item -> item.productName()
                            + " (SKU: " + item.sku()
                            + ") - stock: "
                            + item.quantityInStock().toPlainString()
                            + ", last sold: "
                            + (item.neverSold()
                            ? "Never sold"
                            : item.lastSoldAt().toString()))
                    .collect(Collectors.joining("\n"));
        }

        String prompt = buildInactiveProductsPrompt(
                question,
                inactiveProducts,
                days
        );

        String answer = generateAiAnswer(prompt, fallbackAnswer);

        return new AssistantResponse(
                answer,
                "INACTIVE_PRODUCTS",
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                inactiveProducts,
                List.of()
        );
    }

    private String buildLowStockPrompt(
            String question,
            List<LowStockItemResponse> items) {

        StringBuilder data = new StringBuilder();

        if (items.isEmpty()) {
            data.append("No products are currently at or below their low-stock thresholds.");
        } else {
            for (LowStockItemResponse item : items) {
                data.append("\nProduct: ").append(item.productName());
                data.append("\nSKU: ").append(item.sku());
                data.append("\nQuantity in stock: ")
                        .append(item.quantityInStock().toPlainString());
                data.append("\nLow-stock threshold: ")
                        .append(item.lowStockThreshold().toPlainString());
                data.append("\n");
            }
        }

        return """
                You are TradePilot AI, a business inventory assistant.

                Answer the user's question using only the inventory data below.

                STRICT RULES:
                1. Never invent products or stock quantities.
                2. Never change supplied quantities or thresholds.
                3. Do not calculate stock quantities yourself.
                4. Identify products that need attention.
                5. Give practical, concise recommendations.
                6. If no products are listed, say there are no low-stock alerts.

                User's question:
                %s

                Inventory data:
                %s

                Provide a natural-language answer.
                """.formatted(question, data);
    }

    private String buildInactiveProductsPrompt(
            String question,
            List<InactiveProductResponse> inactiveProducts,
            int days
    ) {
        return """
            You are TradePilot AI, an AI business assistant for small businesses.

            The user's question is:
            %s

            The following data was retrieved from the business database.
            It represents products that have not sold in the last %d days
            or have never been sold.

            Verified product data:
            %s

            Instructions:
            - Answer using only the supplied product data.
            - Distinguish products that have never sold from products that
              were sold previously but have not sold recently.
            - Do not invent product names, stock quantities, or sales dates.
            - Do not claim that a product has never sold if it has a last-sale date.
            - Provide practical recommendations where appropriate, but clearly
              distinguish suggestions from verified facts.
            - If the list is empty, explain that no inactive products were found
              for the specified period.
            - The inactivity period selected by the backend is authoritative.
            - Do not reinterpret, modify, or infer a different inactivity period from the user's wording.
            - Always describe the results using the supplied backend inactivity period.
            - If the backend supplies 30 days, describe the results as covering 30 days.
            """.formatted(
                question,
                days,
                inactiveProducts.isEmpty()
                        ? "No inactive products found."
                        : inactiveProducts.stream()
                        .map(item -> item.productName()
                                + " | SKU: " + item.sku()
                                + " | Stock: " + item.quantityInStock()
                                + " | Last sold: "
                                + (item.neverSold()
                                ? "Never sold"
                                : String.valueOf(item.lastSoldAt())))
                        .collect(Collectors.joining("\n"))
        );
    }

    private AssistantResponse answerDailyFinancialTrendQuestion(
            String originalQuestion,
            String question) {

        DateRange dateRange = resolveTrendDateRange(question);

        List<DailyFinancialTrendResponse> trends =
                financialTrendService.getDailyTrends(
                        dateRange.startDate(),
                        dateRange.endDate()
                );

        String fallbackAnswer;

        if (trends.isEmpty()) {
            fallbackAnswer = "No recorded sales or expenses were found "
                    + dateRange.description() + ".";
        } else {
            fallbackAnswer = "Daily financial trends "
                    + dateRange.description() + ":\n"
                    + trends.stream()
                    .map(item -> item.date()
                            + " (" + item.currency() + ")"
                            + " - Sales: "
                            + CurrencyFormatter.format(
                            item.sales(), item.currency()
                    )
                            + ", Sales transactions: " + item.salesCount()
                            + ", Expenses: "
                            + CurrencyFormatter.format(
                            item.expenses(), item.currency()
                    )
                            + ", Expense transactions: " + item.expenseCount()
                            + ", Net cash flow: "
                            + CurrencyFormatter.format(
                            item.netCashFlow(), item.currency()
                    ))
                    .collect(Collectors.joining("\n"));
        }

        String prompt = buildDailyFinancialTrendPrompt(
                originalQuestion,
                dateRange,
                trends
        );

        String answer = generateAiAnswer(prompt, fallbackAnswer);

        return new AssistantResponse(
                answer,
                "DAILY_FINANCIAL_TRENDS",
                dateRange.startDate(),
                dateRange.endDate(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                trends
        );
    }

    private String buildDailyFinancialTrendPrompt(
            String question,
            DateRange dateRange,
            List<DailyFinancialTrendResponse> trends) {

        String trendData;

        if (trends.isEmpty()) {
            trendData = "No daily financial records were found for this period.";
        } else {
            trendData = trends.stream()
                    .map(item -> """
                    Date: %s
                    Currency: %s
                    Sales: %s
                    Sales transaction count: %d
                    Expenses: %s
                    Expense transaction count: %d
                    Net cash flow: %s
                    """.formatted(
                            item.date(),
                            item.currency(),
                            CurrencyFormatter.format(
                                    item.sales(), item.currency()
                            ),
                            item.salesCount(),
                            CurrencyFormatter.format(
                                    item.expenses(), item.currency()
                            ),
                            item.expenseCount(),
                            CurrencyFormatter.format(
                                    item.netCashFlow(), item.currency()
                            )
                    ))
                    .collect(Collectors.joining("\n"));
        }

        return """
        You are TradePilot AI, a helpful business assistant
        for small businesses.

        The user's question:
        %s

        Reporting period:
        %s

        Verified daily financial data retrieved from the database:
        %s

        STRICT RULES:
        1. Answer using only the supplied daily financial data.
        2. Never invent or estimate financial figures.
        3. Never calculate, recalculate, or modify supplied figures.
        4. Preserve the exact formatted monetary amounts supplied.
        5. Do not remove currency symbols or thousands separators.
        6. Keep different currencies separate.
        7. Do not describe net cash flow as accounting profit.
        8. A zero expense amount means no expenses were recorded for that day.
        9. Do not claim that missing dates had no business activity unless
           the supplied data explicitly establishes that.
        10. Do not claim access to records that were not supplied.
        11. You may identify patterns and compare supplied daily figures,
            but do not calculate new financial totals or percentages.
        12. Keep the answer concise and useful.

        Provide a natural-language answer to the user's question.
        """.formatted(question, dateRange.description(), trendData);
    }
    private String generateAiAnswer(
            String prompt,
            String fallbackAnswer) {

        try {
            String aiAnswer = metaAiClient.ask(prompt);

            if (aiAnswer == null || aiAnswer.isBlank()) {
                log.warn("Meta AI returned an empty answer. Using fallback.");
                return fallbackAnswer;
            }

            return aiAnswer.trim();

        } catch (RuntimeException exception) {
            log.error(
                    "Meta AI request failed. Using deterministic fallback.",
                    exception
            );

            return fallbackAnswer;
        }
    }

    private LowStockItemResponse toLowStockResponse(
            InventoryItem item) {

        return new LowStockItemResponse(
                item.getId(),
                item.getProductName(),
                item.getSku(),
                item.getQuantityInStock(),
                item.getLowStockThreshold()
        );
    }

    private boolean isLowStockQuestion(String question) {
        return question.contains("low stock")
                || question.contains("running low")
                || question.contains("stock running out")
                || question.contains("out of stock");
    }

    private boolean isRevenueQuestion(String question) {
        return question.contains("revenue")
                || question.contains("sales")
                || question.contains("sold");
    }

    private boolean isBestSellingQuestion(String question) {
        return question.contains("best-selling")
                || question.contains("best selling")
                || question.contains("sell the most")
                || question.contains("selling the most")
                || question.contains("top-selling")
                || question.contains("top selling")
                || question.contains("most popular products");
    }

    private boolean isExpenseQuestion(String question) {
        return question.contains("expense")
                || question.contains("expenses")
                || question.contains("spent")
                || question.contains("spending");
    }

    private boolean isCashFlowQuestion(String question) {
        return question.contains("cash flow")
                || question.contains("cashflow");
    }


    private boolean isSummaryQuestion(String question) {
        return question.contains("business summary")
                || question.contains("overall summary")
                || question.equals("summary")
                || question.contains("summarize my business")
                || question.contains("summarise my business")
                || question.contains("business performing")
                || question.contains("business performance")
                || question.contains("business doing")
                || question.contains("business doing financially")
                || question.contains("business overview")
                || question.contains("financial overview")
                || question.contains("financial summary")
                || question.contains("overall performance")
                || question.contains("financial health")
                || question.contains("how is my business")
                || question.contains("how is the business");
    }


    private boolean isRestockQuestion(String question) {
        return question.contains("restock")
                || question.contains("replenish")
                || question.contains("refill inventory")
                || question.contains("products to reorder");
    }

    private boolean isInactiveProductsQuestion(String question) {
        String normalized = question.toLowerCase(Locale.ROOT);

        return normalized.contains("not sold")
                || normalized.contains("never sold")
                || normalized.contains("never been sold")
                || normalized.contains("inactive product")
                || normalized.contains("products inactive")
                || normalized.contains("slow-moving")
                || normalized.contains("slow moving")
                || normalized.contains("haven't sold")
                || normalized.contains("have not sold")
                || normalized.contains("unsold product")
                || normalized.contains("unsold products")
                || normalized.contains("products that don't sell")
                || normalized.contains("products that do not sell");
    }

    private boolean isInventoryValuationQuestion(String question) {
        String normalized = question.toLowerCase(Locale.ROOT);

        return normalized.contains("inventory value")
                || normalized.contains("value of my inventory")
                || normalized.contains("inventory worth")
                || normalized.contains("stock value")
                || normalized.contains("cost value")
                || normalized.contains("retail value")
                || normalized.contains("potential gross margin")
                || normalized.contains("invested in my stock")
                || normalized.contains("invested in my current stock")
                || normalized.contains("investment in my inventory")
                || normalized.contains("cost of my current stock")
                || normalized.contains("how much did i invest")
                || normalized.contains("value of my stock");
    }

    private boolean isDailyFinancialTrendQuestion(String question) {

        return question.contains("daily")
                || question.contains("day by day")
                || question.contains("per day")
                || question.contains("each day")
                || question.contains("daily trend")
                || question.contains("sales trend")
                || question.contains("expense trend")
                || question.contains("daily performance");
    }

    private DateRange resolveDateRange(String question) {

        LocalDate today = LocalDate.now();

        if (question.contains("last week")) {
            throw new IllegalArgumentException(
                    "Last-week queries are not supported yet. "
                            + "Try asking about this week or all-time totals."
            );
        }

        if (question.contains("this week")
                || question.contains("this week's")
                || question.contains("weekly")) {

            LocalDate monday = today.with(DayOfWeek.MONDAY);

            return new DateRange(
                    monday,
                    today,
                    "from " + monday + " to " + today
            );
        }

        if (question.contains("this month")
                || question.contains("this month's")
                || question.contains("monthly")) {

            LocalDate firstDay = today.withDayOfMonth(1);

            return new DateRange(
                    firstDay,
                    today,
                    "from " + firstDay + " to " + today
            );
        }

        if (question.contains("today")
                || question.contains("today's")) {

            return new DateRange(
                    today,
                    today,
                    "for " + today
            );
        }

        return new DateRange(
                null,
                null,
                "across all recorded dates"
        );
    }

    private String buildRestockPrompt(
            String question,
            List<RestockRecommendationResponse> recommendations) {

        StringBuilder data = new StringBuilder();

        if (recommendations.isEmpty()) {
            data.append("No products currently require restocking.");
        } else {
            for (RestockRecommendationResponse item : recommendations) {
                data.append("\nProduct: ").append(item.productName());
                data.append("\nSKU: ").append(item.sku());

                data.append("\nCurrent stock: ")
                        .append(item.quantityInStock().toPlainString());

                data.append("\nLow-stock threshold: ")
                        .append(item.lowStockThreshold().toPlainString());

                data.append("\nTarget stock level: ")
                        .append(item.targetStockLevel().toPlainString());

                data.append("\nRecommended restock quantity: ")
                        .append(item.recommendedRestockQuantity().toPlainString());

                data.append("\nUnit cost: ")
                        .append(item.currency())
                        .append(" ")
                        .append(item.unitCost().toPlainString());

                data.append("\nEstimated restock cost: ")
                        .append(item.currency())
                        .append(" ")
                        .append(item.estimatedRestockCost().toPlainString());

                data.append("\n");
            }
        }

        return """
            You are TradePilot AI, a helpful inventory assistant
            for small businesses.

            Answer the user's restocking question using only the
            inventory data supplied below.

            STRICT RULES:
            1. Never invent products, quantities, or prices.
            2. Never change the supplied numerical values.
            3. Do not calculate or modify restock quantities or costs.
            4. Use the supplied currency for every monetary amount.
            5. Explain the recommended restock quantities clearly.
            6. If no products are listed, say no restocking is currently recommended.
            7. Do not claim that a purchase order has been created or placed.
            8. Keep the answer concise and practical.

            User's question:
            %s

            Inventory restock data:
            %s

            Provide a natural-language answer.
            """.formatted(question, data);
    }



    private DateRange resolveTrendDateRange(String question) {

        LocalDate today = LocalDate.now();

        if (question.contains("last week")) {
            LocalDate thisMonday = today.with(DayOfWeek.MONDAY);
            LocalDate lastMonday = thisMonday.minusWeeks(1);
            LocalDate lastSunday = thisMonday.minusDays(1);

            return new DateRange(
                    lastMonday,
                    lastSunday,
                    "from " + lastMonday + " to " + lastSunday
            );
        }

        if (question.contains("this week")
                || question.contains("this week's")
                || question.contains("weekly")) {

            LocalDate monday = today.with(DayOfWeek.MONDAY);

            return new DateRange(
                    monday,
                    today,
                    "from " + monday + " to " + today
            );
        }

        if (question.contains("this month")
                || question.contains("this month's")
                || question.contains("monthly")) {

            LocalDate firstDay = today.withDayOfMonth(1);

            return new DateRange(
                    firstDay,
                    today,
                    "from " + firstDay + " to " + today
            );
        }

        if (question.contains("yesterday")) {
            LocalDate yesterday = today.minusDays(1);

            return new DateRange(
                    yesterday,
                    yesterday,
                    "for " + yesterday
            );
        }

        if (question.contains("today")) {
            return new DateRange(
                    today,
                    today,
                    "for " + today
            );
        }

        LocalDate startDate = today.minusDays(6);

        return new DateRange(
                startDate,
                today,
                "from " + startDate + " to " + today
        );
    }


    private record DateRange(
            LocalDate startDate,
            LocalDate endDate,
            String description) {
    }
}
