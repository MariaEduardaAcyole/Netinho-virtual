package br.com.fiap.netinho.bo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Voz natural do Netinho: pede ao servico interno de voz (deploy/voz, NVIDIA Magpie TTS) o MP3 da fala.
 * Guarda as ultimas falas, porque frases como "Toque no botao abaixo" se repetem muito.
 * Se o servico nao responder, devolve vazio e o app usa a voz do proprio aparelho.
 */
@Service
public class VozBO {
    private static final Logger LOG = LoggerFactory.getLogger(VozBO.class);
    static final int MAX_TEXTO = 600;
    static final Set<String> VOZES = Set.of("PT-BR.Isabela", "PT-BR.Louise", "PT-BR.Diego");
    private static final int MAX_GUARDADAS = 200;

    private final URI endereco;
    private final String vozPadrao;
    private final boolean ligada;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, byte[]> guardadas = new LinkedHashMap<>(64, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, byte[]> e) { return size() > MAX_GUARDADAS; }
    };

    public VozBO(@Value("${voz.url:http://netinho-voz:8000}") String url,
                 @Value("${voz.nome:PT-BR.Diego}") String vozPadrao,
                 @Value("${nvidia.api-key:}") String chave) {
        this.endereco = URI.create(url.replaceAll("/+$", "") + "/sintetizar");
        this.vozPadrao = VOZES.contains(vozPadrao) ? vozPadrao : "PT-BR.Diego";
        this.ligada = chave != null && !chave.isBlank() && !url.isBlank();
    }

    public boolean ligada() { return ligada; }

    public Optional<byte[]> falar(String texto, String voz) {
        String t = texto == null ? "" : texto.replaceAll("\\s+", " ").trim();
        if (!ligada || t.isEmpty()) return Optional.empty();
        if (t.length() > MAX_TEXTO) t = t.substring(0, MAX_TEXTO);
        String v = voz != null && VOZES.contains(voz) ? voz : vozPadrao;
        String chave = v + "|" + t;
        synchronized (guardadas) {
            byte[] pronto = guardadas.get(chave);
            if (pronto != null) return Optional.of(pronto);
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(endereco)
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(Map.of("texto", t, "voz", v))))
                    .build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() != 200 || resp.body().length == 0) {
                LOG.warn("Servico de voz respondeu {}", resp.statusCode());
                return Optional.empty();
            }
            synchronized (guardadas) { guardadas.put(chave, resp.body()); }
            return Optional.of(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            LOG.warn("Falha no servico de voz: {}", e.toString());
            return Optional.empty();
        }
    }
}
