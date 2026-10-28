package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface ExpenseService {
    public Expense recordExpense(
            String description,
            String category,
            BigDecimal amount,
            String currency,
            LocalDateTime incurredAt
    );

    public List<Expense> getAllExpenses();
    public List<Expense> getExpensesBetween(
            LocalDateTime start,
            LocalDateTime end);
}
