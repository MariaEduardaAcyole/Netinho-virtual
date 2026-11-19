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
 * Conversa do Netinho por modelos de texto da NVIDIA (API compativel com a da OpenAI).
 * Tenta os modelos na ordem configurada (o primeiro e o rapido, o seguinte e reserva), cada um com
 * tempo curto: a pessoa idosa nao pode ficar esperando. Sem NVIDIA_API_KEY fica desligado e o
 * ConversaBO usa as regras de palavras-chave.
 */
@Service
public class NvidiaAssistente implements AssistenteIA {
    private static final Logger LOG = LoggerFactory.getLogger(NvidiaAssistente.class);

    private final String chave;
    private final List<String> modelos;
    private final Duration limite;
    private final URI endereco;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json = new ObjectMapper();

    public NvidiaAssistente(@Value("${nvidia.api-key:}") String chave,
                            @Value("${nvidia.chat-model:nvidia/nemotron-3.5-lightning-30b-a3b,nvidia/nemotron-3.5-bolt-e4b}") String modelos,
                            @Value("${nvidia.chat-timeout-seconds:8}") int segundos,
                            @Value("${nvidia.base-url:https://integrate.api.nvidia.com/v1}") String urlBase) {
        this.chave = chave;
        this.modelos = java.util.Arrays.stream(modelos.split(",")).map(String::trim).filter(m -> !m.isEmpty()).toList();
        this.limite = Duration.ofSeconds(Math.max(2, segundos));
        this.endereco = URI.create(urlBase.replaceAll("/+$", "") + "/chat/completions");
        if (disponivel()) LOG.info("Conversa do Netinho pela NVIDIA ({}).", this.modelos);
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
        for (String modelo : modelos) {
            long inicio = System.nanoTime();
            Optional<String> r = perguntar(modelo, mensagens);
            LOG.debug("{} respondeu em {} ms", modelo, (System.nanoTime() - inicio) / 1_000_000);
            if (r.isPresent()) return r;
            if (Thread.currentThread().isInterrupted()) break;
        }
        return Optional.empty();
    }

    private Optional<String> perguntar(String modelo, List<Map<String, String>> mensagens) {
        // Sem "pensar em voz alta": nos modelos com raciocinio isso deixava a resposta lenta ou vazia.
        Map<String, Object> corpo = Map.of("model", modelo, "max_tokens", 400, "temperature", 0.3, "messages", mensagens,
                "chat_template_kwargs", Map.of("enable_thinking", false));
        try {
            HttpRequest req = HttpRequest.newBuilder(endereco)
                    .timeout(limite)
                    .header("Authorization", "Bearer " + chave)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corpo)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                LOG.warn("NVIDIA ({}) respondeu {} na conversa: {}", modelo, resp.statusCode(), resp.body() == null ? "" : resp.body().substring(0, Math.min(300, resp.body().length())));
                return Optional.empty();
            }
            String texto = json.readTree(resp.body()).path("choices").path(0).path("message").path("content").asText("");
            if (texto.isBlank()) LOG.warn("NVIDIA ({}) devolveu resposta vazia: {}", modelo, resp.body().substring(0, Math.min(300, resp.body().length())));
            return texto.isBlank() ? Optional.empty() : Optional.of(texto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            LOG.warn("Falha na conversa com a NVIDIA ({}): {}", modelo, e.getMessage());
            return Optional.empty();
        }
    }
}
