package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.model.TipoRiscoAlerta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Analisa o print com um modelo de visao da NVIDIA (API compativel com a da OpenAI).
 * Escolhido por AnaliseImagemConfig quando ha NVIDIA_API_KEY e nao ha chave da Anthropic.
 */
public class NvidiaAnaliseImagem implements AnaliseImagem {
    private static final Logger LOG = LoggerFactory.getLogger(NvidiaAnaliseImagem.class);
    private static final String FORMATO = """

            Responda SOMENTE com um objeto JSON, sem texto antes ou depois, neste formato:
            {"nivel":"BAIXO|MEDIO|ALTO","motivo":"...","oQueFazer":"...","textoExtraido":"..."}
            """;

    private final String chave;
    private final String modelo;
    private final URI endereco;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();

    public NvidiaAnaliseImagem(String chave, String modelo, String urlBase) {
        this(chave, modelo, urlBase, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build());
    }

    NvidiaAnaliseImagem(String chave, String modelo, String urlBase, HttpClient http) {
        this.chave = chave;
        this.modelo = modelo;
        this.endereco = URI.create(urlBase.replaceAll("/+$", "") + "/chat/completions");
        this.http = http;
    }

    @Override
    public boolean disponivel() { return chave != null && !chave.isBlank(); }

    @Override
    public Optional<Resultado> analisar(String imagemBase64, String tipoImagem, String textoDaPessoa) {
        if (!disponivel()) return Optional.empty();
        String pedido = textoDaPessoa == null || textoDaPessoa.isBlank()
                ? "Esta mensagem e golpe?"
                : "Esta mensagem e golpe? Comentario da pessoa: " + textoDaPessoa;
        String tipo = tipoImagem == null || tipoImagem.isBlank() ? "image/jpeg" : tipoImagem;
        Map<String, Object> corpo = Map.of(
                "model", modelo,
                "max_tokens", 1500,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", ClaudeAnaliseImagem.INSTRUCOES + FORMATO),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", pedido),
                                Map.of("type", "image_url", "image_url", Map.of("url", "data:" + tipo + ";base64," + imagemBase64))))));
        try {
            HttpRequest req = HttpRequest.newBuilder(endereco)
                    .timeout(Duration.ofSeconds(90))
                    .header("Authorization", "Bearer " + chave)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corpo)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                LOG.warn("NVIDIA respondeu {} na analise de imagem: {}", resp.statusCode(), resumo(resp.body()));
                return Optional.empty();
            }
            String conteudo = json.readTree(resp.body()).path("choices").path(0).path("message").path("content").asText("");
            Optional<Resultado> r = interpretar(conteudo);
            if (r.isEmpty()) LOG.warn("Resposta da NVIDIA fora do formato esperado: {}", resumo(conteudo));
            return r;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            LOG.warn("Falha ao analisar imagem com a NVIDIA: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Le o JSON da resposta, mesmo se vier dentro de ```json ... ``` ou com texto em volta. */
    Optional<Resultado> interpretar(String conteudo) {
        if (conteudo == null) return Optional.empty();
        int ini = conteudo.indexOf('{'), fim = conteudo.lastIndexOf('}');
        if (ini < 0 || fim <= ini) return Optional.empty();
        try {
            JsonNode n = json.readTree(conteudo.substring(ini, fim + 1));
            TipoRiscoAlerta nivel = nivel(n.path("nivel").asText(""));
            String motivo = n.path("motivo").asText("").trim();
            String oQueFazer = n.path("oQueFazer").asText("").trim();
            if (nivel == null || motivo.isEmpty() || oQueFazer.isEmpty()) return Optional.empty();
            return Optional.of(new Resultado(nivel, motivo, oQueFazer, n.path("textoExtraido").asText("").trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static TipoRiscoAlerta nivel(String texto) {
        String t = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").trim().toUpperCase();
        for (TipoRiscoAlerta v : TipoRiscoAlerta.values()) if (t.equals(v.name())) return v;
        return null;
    }

    private static String resumo(String s) {
        return s == null ? "" : s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
