package tradepilot_api.service;

import org.springframework.stereotype.Service;
import tradepilot_api.dto.RestockRecommendationResponse;

import java.util.List;

@Service
public interface RestockRecommendationService {
    List<RestockRecommendationResponse> getRestockRecommendations();
}
