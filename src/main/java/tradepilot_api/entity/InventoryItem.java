package tradepilot_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items",
        uniqueConstraints = {@UniqueConstraint(name = "uk_inventory_items_sku", columnNames = "sku")
        }
)
@Getter
@NoArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityInStock;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sellingPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lowStockThreshold;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public InventoryItem(
            String productName,
            String sku,
            BigDecimal quantityInStock,
            BigDecimal unitCost,
            BigDecimal sellingPrice,
            String currency,
            BigDecimal lowStockThreshold) {

        this.productName = productName;
        this.sku = sku;
        this.quantityInStock = quantityInStock;
        this.unitCost = unitCost;
        this.sellingPrice = sellingPrice;
        this.currency = currency;
        this.lowStockThreshold = lowStockThreshold;
    }
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
