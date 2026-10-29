package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.AssistantResponse;

@Service
public interface AssistantService {
    AssistantResponse answerQuestion(String message);
}
