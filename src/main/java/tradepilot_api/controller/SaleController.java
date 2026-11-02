
package tradepilot_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tradepilot_api.dto.InventorySaleRequest;
import tradepilot_api.dto.SaleRequest;
import tradepilot_api.dto.SaleResponse;
import tradepilot_api.service.SaleService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public SaleResponse recordSale(
            @Valid @RequestBody SaleRequest request
    ) {
        return SaleResponse.from(saleService.recordSale(
                request.itemName(),
                request.saleType(),
                request.quantity(),
                request.unitPrice(),
                request.currency(),
                request.soldAt()
        ));
    }

    @GetMapping
    public List<SaleResponse> getAllSales() {
        return saleService.getAllSales()
                .stream()
                .map(SaleResponse::from)
                .toList();
    }

    @PostMapping("/from-inventory")
    public SaleResponse recordInventorySale(
            @Valid @RequestBody InventorySaleRequest request
    ) {
        return SaleResponse.from(
                saleService.recordInventorySale(request)
        );
    }
}
