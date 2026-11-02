package tradepilot_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tradepilot_api.dto.DailyFinancialTrendResponse;
import tradepilot_api.service.FinancialTrendService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics/financial-trends")
@RequiredArgsConstructor
public class FinancialTrendController {

    private final FinancialTrendService financialTrendService;

    @GetMapping("/daily")
    public List<DailyFinancialTrendResponse> getDailyTrends(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return financialTrendService.getDailyTrends(startDate, endDate);
    }
}