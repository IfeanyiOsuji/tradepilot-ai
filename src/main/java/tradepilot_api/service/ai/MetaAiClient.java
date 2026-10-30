
package tradepilot_api.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class MetaAiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public MetaAiClient(
            @Value("${meta.ai.base-url}") String baseUrl,
            @Value("${meta.ai.api-key}") String apiKey,
            @Value("${meta.ai.model}") String model) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "MODEL_API_KEY environment variable is not configured."
            );
        }

        this.apiKey = apiKey;
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String ask(String prompt) {

        ChatRequest request = new ChatRequest(
                model,
                List.of(new ChatMessage("user", prompt))
        );

        try {
            ChatResponse response = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null
                    || response.choices() == null
                    || response.choices().isEmpty()
                    || response.choices().get(0).message() == null
                    || response.choices().get(0).message().content() == null) {

                throw new IllegalStateException(
                        "Meta AI returned an empty response."
                );
            }

            return response.choices().get(0).message().content();

        } catch (RestClientResponseException exception) {

            throw new IllegalStateException(
                    "Meta AI API returned HTTP "
                            + exception.getStatusCode().value(),
                    exception
            );

        } catch (ResourceAccessException exception) {

            throw new IllegalStateException(
                    "Could not connect to the Meta AI API.",
                    exception
            );
        }
    }

    private record ChatRequest(
            String model,
            List<ChatMessage> messages) {
    }

    private record ChatMessage(
            String role,
            String content) {
    }

    private record ChatResponse(
            List<Choice> choices) {
    }

    private record Choice(
            ChatMessage message) {
    }
}
