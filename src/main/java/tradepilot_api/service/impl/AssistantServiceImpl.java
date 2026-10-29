package tradepilot_api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tradepilot_api.dto.AssistantResponse;
import tradepilot_api.dto.BusinessSummary;
import tradepilot_api.dto.LowStockItemResponse;
import tradepilot_api.entity.InventoryItem;
import tradepilot_api.service.AssistantService;
import tradepilot_api.service.BusinessSummaryService;
import tradepilot_api.service.InventoryService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private final BusinessSummaryService businessSummaryService;
    private final InventoryService inventoryService;

    @Override
    public AssistantResponse answerQuestion(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a business question."
            );
        }

        String question = message.trim().toLowerCase(Locale.ROOT);

        if (isLowStockQuestion(question)) {
            return answerLowStockQuestion();
        }

        if (isRevenueQuestion(question)) {
            return answerFinancialQuestion(question, "REVENUE");
        }

        if (isExpenseQuestion(question)) {
            return answerFinancialQuestion(question, "EXPENSES");
        }

        if (isCashFlowQuestion(question)) {
            return answerFinancialQuestion(question, "NET_CASH_FLOW");
        }

        if (isSummaryQuestion(question)) {
            return answerFinancialQuestion(question, "SUMMARY");
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

        String answer = buildFinancialAnswer(
                summaries,
                intent,
                dateRange
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
                                    + " — Revenue: "
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

    private AssistantResponse answerLowStockQuestion() {
        List<LowStockItemResponse> lowStockItems =
                inventoryService.getLowStockItems()
                        .stream()
                        .map(this::toLowStockResponse)
                        .toList();

        String answer;

        if (lowStockItems.isEmpty()) {
            answer = "There are no products at or below their "
                    + "low-stock thresholds.";
        } else {
            answer = "Found " + lowStockItems.size()
                    + " product(s) at or below their low-stock thresholds:\n"
                    + lowStockItems.stream()
                    .map(item -> item.productName()
                            + " (SKU: " + item.sku()
                            + ") — stock: "
                            + item.quantityInStock().toPlainString()
                            + ", threshold: "
                            + item.lowStockThreshold().toPlainString())
                    .collect(Collectors.joining("\n"));
        }

        return new AssistantResponse(
                answer,
                "LOW_STOCK",
                null,
                null,
                List.of(),
                lowStockItems
        );
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

            LocalDate monday = today.with(
                    DayOfWeek.MONDAY
            );

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