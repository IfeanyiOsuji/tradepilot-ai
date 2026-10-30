
package tradepilot_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tradepilot_api.service.ai.MetaAiClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MetaAiClientTest {

    @Autowired
    private MetaAiClient metaAiClient;

    @Test
    void shouldConnectToMetaAi() {

        String response = metaAiClient.ask(
                "Reply with exactly two words: JAVA CONNECTED"
        );

        assertNotNull(response);
        assertFalse(response.isBlank());

        System.out.println("Meta AI response: " + response);
    }
}
