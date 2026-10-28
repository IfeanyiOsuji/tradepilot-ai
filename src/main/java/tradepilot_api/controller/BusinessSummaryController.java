package tradepilot_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tradepilot_api.dto.BusinessSummary;
import tradepilot_api.service.BusinessSummaryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/business")
public class BusinessSummaryController {
    private final BusinessSummaryService businessSummaryService;

    @GetMapping("/summary")
    public List<BusinessSummary> getSummary() {
        return businessSummaryService.getSummary();
    }
}
