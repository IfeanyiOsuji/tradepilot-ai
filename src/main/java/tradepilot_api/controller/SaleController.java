package tradepilot_api.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tradepilot_api.dto.SaleRequest;
import tradepilot_api.entity.Sale;
import tradepilot_api.service.SaleService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;



    @PostMapping
    public Sale recordSale(@Valid @RequestBody SaleRequest request) {
        return saleService.recordSale(
                request.itemName(),
                request.saleType(),
                request.quantity(),
                request.unitPrice(),
                request.currency(),
                request.soldAt()
        );
    }

    @GetMapping
    public List<Sale> getAllSales() {
        return saleService.getAllSales();
    }

}
