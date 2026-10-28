package tradepilot_api.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tradepilot_api.entity.Expense;
import tradepilot_api.entity.ExpenseCategory;
import tradepilot_api.repository.ExpenseRepository;
import tradepilot_api.service.ExpenseService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static tradepilot_api.utils.TradeUtil.SUPPORTED_CURRENCIES;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    @Override
    @Transactional
    public Expense recordExpense(String description, String category, BigDecimal amount, String currency, LocalDateTime incurredAt) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Expense description is required");
        }

        if (description.length() > 150) {
            throw new IllegalArgumentException(
                    "Description must not exceed 150 characters");
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Expense category is required");
        }

        ExpenseCategory expenseCategory;

        try {
            expenseCategory = ExpenseCategory.valueOf(
                    category.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid category. Supported categories: "
                            + java.util.Arrays.toString(
                            ExpenseCategory.values()));
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Expense amount must be greater than zero");
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        String normalizedCurrency =
                currency.trim().toUpperCase(Locale.ROOT);

        if (!SUPPORTED_CURRENCIES.contains(normalizedCurrency)) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + normalizedCurrency);
        }

        if (incurredAt == null) {
            throw new IllegalArgumentException(
                    "Expense date and time are required");
        }

        Expense expense = new Expense(
                description.trim(),
                expenseCategory,
                amount,
                normalizedCurrency,
                incurredAt
        );

        return expenseRepository.save(expense);

    }

    @Override
    @Transactional
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public List<Expense> getExpensesBetween(LocalDateTime start, LocalDateTime end) {
        return expenseRepository
                .findByIncurredAtGreaterThanEqualAndIncurredAtLessThan(start, end);
    }
}
