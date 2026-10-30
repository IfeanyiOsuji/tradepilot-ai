
package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tradepilot_api.dto.AssistantResponse;
import tradepilot_api.dto.BusinessSummary;
import tradepilot_api.dto.LowStockItemResponse;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.service.AssistantService;
import tradepilot_api.service.BusinessSummaryService;
import tradepilot_api.service.InventoryService;
import tradepilot_api.service.ai.MetaAiClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private static final Logger log =
            LoggerFactory.getLogger(AssistantServiceImpl.class);

    private final BusinessSummaryService businessSummaryService;
    private final InventoryService inventoryService;
    private final MetaAiClient metaAiClient;

    @Override
    public AssistantResponse answerQuestion(String message) {

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a business question."
            );
        }

        String question = message.trim().toLowerCase(Locale.ROOT);

        if (isLowStockQuestion(question)) {
            return answerLowStockQuestion(message);
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
                List.of()
        );
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
                        .append(summary.totalRevenue().toPlainString());

                data.append("\nTotal expenses: ")
                        .append(summary.totalExpenses().toPlainString());

                data.append("\nNet cash flow: ")
                        .append(summary.netCashFlow().toPlainString());

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
                4. If data is missing, clearly say so.
                5. Distinguish net cash flow from accounting profit.
                6. Do not describe net cash flow as profit.
                7. Use the currency provided with each figure.
                8. Give a clear, concise and useful explanation.
                9. Do not claim access to information not supplied.

                User's question:
                %s

                %s

                Provide a natural-language answer to the user's question.
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
                                    + summary.totalRevenue().toPlainString()
                                    + ", Expenses: "
                                    + summary.totalExpenses().toPlainString()
                                    + ", Net cash flow: "
                                    + summary.netCashFlow().toPlainString()
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
                        + switch (intent) {
                    case "REVENUE" ->
                            summary.totalRevenue().toPlainString();
                    case "EXPENSES" ->
                            summary.totalExpenses().toPlainString();
                    case "NET_CASH_FLOW" ->
                            summary.netCashFlow().toPlainString();
                    default -> "0";
                })
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
                lowStockItems
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
                || question.equals("summarize my business");
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

    private record DateRange(
            LocalDate startDate,
            LocalDate endDate,
            String description) {
    }
}
