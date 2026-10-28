package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.InventorySaleRequest;
import tradepilot_api.entity.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface SaleService {
     Sale recordSale(
            String itemName,
            String saleType,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String currency,
            LocalDateTime soldAt
    );

    List<Sale> getAllSales();

     List<Sale> getSalesBetween(
            LocalDateTime start,
            LocalDateTime end);

    Sale recordInventorySale(InventorySaleRequest request);
}
