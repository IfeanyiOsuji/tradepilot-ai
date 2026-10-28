package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.entity.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface SaleService {
    public Sale recordSale(
            String itemName,
            String saleType,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String currency,
            LocalDateTime soldAt
    );
    public List<Sale> getAllSales();

    public List<Sale> getSalesBetween(
            LocalDateTime start,
            LocalDateTime end);
}
