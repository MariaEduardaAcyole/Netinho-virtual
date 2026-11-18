package br.com.fiap.netinho.bo;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Analisa o print com o Claude (SDK oficial anthropic-java) e devolve um resultado estruturado.
 * Sem ANTHROPIC_API_KEY a analise fica desligada e o GolpeBO usa so as regras de texto.
 */
@Service
public class ClaudeAnaliseImagem implements AnaliseImagem {
    private static final Logger LOG = LoggerFactory.getLogger(ClaudeAnaliseImagem.class);

    private static final String INSTRUCOES = """
            Voce ajuda pessoas idosas no Brasil a descobrir se uma mensagem recebida no celular e golpe.
            Voce recebe a foto ou print da mensagem (WhatsApp, SMS, e-mail, rede social ou site) e, as vezes,
            um comentario da pessoa. Leia o que aparece na imagem e avalie o risco de golpe.

            Sinais fortes de golpe: pedido de Pix, transferencia, pagamento ou deposito; pedido de senha, codigo,
            token ou SMS; "troquei de numero" ou alguem se passando por parente; urgencia, ameaca ou premio;
            link encurtado ou estranho; boleto, cobranca ou "beneficio do INSS" inesperados; QR code para pagar.

            Use ALTO quando houver pedido de dinheiro ou dados junto com urgencia, troca de numero ou link suspeito;
            MEDIO quando houver um sinal isolado que merece confirmacao; BAIXO quando nao houver sinal de golpe.
            Se a imagem nao for de uma mensagem ou estiver ilegivel, use MEDIO e diga isso no motivo.

            Escreva motivo e oQueFazer em portugues do Brasil, com frases curtas e palavras simples, sem termos
            tecnicos, falando diretamente com a pessoa. Nunca peca para ela clicar em links ou enviar dados.
            """;

    private final AnthropicClient client;
    private final String modelo;

    public ClaudeAnaliseImagem(@Value("${anthropic.api-key:}") String chave,
                               @Value("${anthropic.model:claude-opus-5-5}") String modelo) {
        this.modelo = modelo;
        if (chave == null || chave.isBlank()) {
            this.client = null;
            LOG.warn("ANTHROPIC_API_KEY ausente: a analise de imagens no 'E golpe?' fica desligada.");
        } else {
            this.client = AnthropicOkHttpClient.builder().apiKey(chave).timeout(Duration.ofSeconds(90)).build();
        }
    }

    @Override
    public boolean disponivel() { return client != null; }

    @Override
    public Optional<Resultado> analisar(String imagemBase64, String tipoImagem, String textoDaPessoa) {
        if (client == null) return Optional.empty();
        ImageBlockParam imagem = ImageBlockParam.builder()
                .source(Base64ImageSource.builder()
                        .data(imagemBase64)
                        .mediaType(tipoMidia(tipoImagem))
                        .build())
                .build();
        String pedido = textoDaPessoa == null || textoDaPessoa.isBlank()
                ? "Esta mensagem e golpe?"
                : "Esta mensagem e golpe? Comentario da pessoa: " + textoDaPessoa;
        StructuredMessageCreateParams<Resultado> params = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(8000L)
                .system(INSTRUCOES)
                .outputConfig(Resultado.class)
                .addUserMessageOfContentBlockParams(List.of(
                        ContentBlockParam.ofImage(imagem),
                        ContentBlockParam.ofText(TextBlockParam.builder().text(pedido).build())))
                // Se o modelo recusar, a API tenta outro modelo automaticamente.
                .putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                .build();
        try {
            StructuredMessage<Resultado> resposta = client.messages().create(params);
            if (resposta.stopReason().map(StopReason.REFUSAL::equals).orElse(false)) {
                LOG.warn("Analise de imagem recusada pelo modelo.");
                return Optional.empty();
            }
            return resposta.content().stream()
                    .flatMap(bloco -> bloco.text().stream())
                    .map(texto -> texto.text())
                    .filter(r -> r != null && r.nivel() != null)
                    .findFirst();
        } catch (RuntimeException e) {
            LOG.warn("Falha ao analisar imagem com o Claude: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static Base64ImageSource.MediaType tipoMidia(String tipo) {
        if (tipo == null) return Base64ImageSource.MediaType.IMAGE_JPEG;
        return switch (tipo.toLowerCase()) {
            case "image/png" -> Base64ImageSource.MediaType.IMAGE_PNG;
            case "image/webp" -> Base64ImageSource.MediaType.IMAGE_WEBP;
            case "image/gif" -> Base64ImageSource.MediaType.IMAGE_GIF;
            default -> Base64ImageSource.MediaType.IMAGE_JPEG;
        };
    }
}
