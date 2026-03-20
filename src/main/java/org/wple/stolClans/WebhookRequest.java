package org.wple.stolClans;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// https://discord.com/api/webhooks/1433097004749291580/Nq_haBHm2sOhvHoAeJu8_8P3PTOe2t3cAJx5yBtf_3Gw1IDqCGC6I3oYeILTLfbALfa8
public class WebhookRequest {
    private final HttpClient httpClient;

    //private String content;
    //private String username;
    //private String avatarUrl;
    //private String threadName;
    //private List<Embed> embeds;

    public WebhookRequest() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendToDiscord(String webhookUrl, String msg) throws IOException, InterruptedException {
        sendToDiscord(webhookUrl, null, msg);
    }

    public void sendToDiscord(String webhookUrl, Long threadId, String msg) throws IOException, InterruptedException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", msg);

        String finalUrl = webhookUrl;
        if (threadId != null) {
            finalUrl += "?thread_id=" + threadId;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{ \"content\": \"" + msg + "\" }"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to send webhook: HTTP " + response.statusCode() + " - " + response.body());
        }
    }
}
