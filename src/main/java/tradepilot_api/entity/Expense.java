package tradepilot_api.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime incurredAt;

    public Expense(
            String description,
            ExpenseCategory category,
            BigDecimal amount,
            String currency,
            LocalDateTime incurredAt
    ) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.incurredAt = incurredAt;
    }
}
