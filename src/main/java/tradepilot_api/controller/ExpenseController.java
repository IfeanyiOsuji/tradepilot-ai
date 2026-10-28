package tradepilot_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tradepilot_api.dto.ExpenseRequest;
import tradepilot_api.entity.Expense;
import tradepilot_api.service.ExpenseService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public Expense recordExpense(
            @Valid @RequestBody ExpenseRequest request) {

        return expenseService.recordExpense(
                request.description(),
                request.category(),
                request.amount(),
                request.currency(),
                request.incurredAt()
        );
    }

    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }
}
