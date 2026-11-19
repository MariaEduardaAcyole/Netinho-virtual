package br.com.fiap.netinho.bo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Conversa do Netinho por um modelo de texto da NVIDIA (API compativel com a da OpenAI).
 * Sem NVIDIA_API_KEY fica desligado e o ConversaBO usa as regras de palavras-chave.
 */
@Service
public class NvidiaAssistente implements AssistenteIA {
    private static final Logger LOG = LoggerFactory.getLogger(NvidiaAssistente.class);

    private final String chave;
    private final String modelo;
    private final URI endereco;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json = new ObjectMapper();

    public NvidiaAssistente(@Value("${nvidia.api-key:}") String chave,
                            @Value("${nvidia.chat-model:google/gemma-4-31b-it}") String modelo,
                            @Value("${nvidia.base-url:https://integrate.api.nvidia.com/v1}") String urlBase) {
        this.chave = chave;
        this.modelo = modelo;
        this.endereco = URI.create(urlBase.replaceAll("/+$", "") + "/chat/completions");
        if (disponivel()) LOG.info("Conversa do Netinho pela NVIDIA ({}).", modelo);
        else LOG.warn("NVIDIA_API_KEY ausente: a conversa do Netinho usa so palavras-chave.");
    }

    @Override
    public boolean disponivel() { return chave != null && !chave.isBlank(); }

    @Override
    public Optional<String> responder(String instrucoes, List<Mensagem> conversa) {
        if (!disponivel()) return Optional.empty();
        List<Map<String, String>> mensagens = new ArrayList<>();
        mensagens.add(Map.of("role", "system", "content", instrucoes));
        for (Mensagem m : conversa) mensagens.add(Map.of("role", m.papel(), "content", m.texto()));
        Map<String, Object> corpo = Map.of("model", modelo, "max_tokens", 600, "temperature", 0.3, "messages", mensagens);
        try {
            HttpRequest req = HttpRequest.newBuilder(endereco)
                    .timeout(Duration.ofSeconds(25))
                    .header("Authorization", "Bearer " + chave)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corpo)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                LOG.warn("NVIDIA respondeu {} na conversa: {}", resp.statusCode(), resp.body() == null ? "" : resp.body().substring(0, Math.min(300, resp.body().length())));
                return Optional.empty();
            }
            String texto = json.readTree(resp.body()).path("choices").path(0).path("message").path("content").asText("");
            return texto.isBlank() ? Optional.empty() : Optional.of(texto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            LOG.warn("Falha na conversa com a NVIDIA: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
