package tradepilot_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tradepilot_api.dto.AssistantRequest;
import tradepilot_api.dto.AssistantResponse;
import tradepilot_api.service.AssistantService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public AssistantResponse chat(
            @Valid @RequestBody AssistantRequest request) {

        return assistantService.answerQuestion(request.message());
    }
}