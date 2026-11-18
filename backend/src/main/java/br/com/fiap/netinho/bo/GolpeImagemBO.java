package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.exception.RegraNegocioException;
import br.com.fiap.netinho.model.TipoRiscoAlerta;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Recebe o print enviado no "E golpe?", pede a analise da imagem e registra o alerta pelo GolpeBO.
 */
@Service
public class GolpeImagemBO {
    static final int MAX_BASE64 = 7_000_000; // ~5 MB de imagem
    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final int MAX_MENSAGEM = 500;

    private final GolpeBO golpeBO;
    private final AnaliseImagem analise;

    public GolpeImagemBO(GolpeBO golpeBO, AnaliseImagem analise) { this.golpeBO = golpeBO; this.analise = analise; }

    public Map<String, Object> verificar(Long idIdoso, String texto, String imagemBase64, String tipoImagem) {
        String tipo = tipoImagem == null ? "image/jpeg" : tipoImagem.toLowerCase();
        if (!TIPOS.contains(tipo)) throw new RegraNegocioException("Envie uma foto em JPG, PNG, WEBP ou GIF.");
        String base64 = imagemBase64 == null ? "" : imagemBase64.replaceFirst("^data:[^,]*,", "").trim();
        if (base64.isEmpty()) throw new RegraNegocioException("Nenhuma imagem foi enviada.");
        if (base64.length() > MAX_BASE64) throw new RegraNegocioException("A imagem e grande demais. Envie uma foto menor.");
        String comentario = texto == null ? "" : texto.trim();

        Optional<AnaliseImagem.Resultado> resultado = analise.analisar(base64, tipo, comentario);
        AnaliseImagem.Resultado usado = resultado.orElse(new AnaliseImagem.Resultado(
                TipoRiscoAlerta.MEDIO,
                "Nao consegui ler a imagem agora. Por seguranca, trate a mensagem com cuidado.",
                "Confirme com alguem da familia antes de responder.",
                ""));
        String mensagem = juntar(comentario, usado.textoExtraido());
        if (mensagem.isEmpty()) mensagem = "[imagem sem texto]";
        Map<String, Object> r = golpeBO.verificarComAnalise(idIdoso, mensagem, usado);
        r.put("imagemAnalisada", resultado.isPresent());
        return r;
    }

    private static String juntar(String comentario, String extraido) {
        String a = comentario == null ? "" : comentario.trim();
        String b = extraido == null ? "" : extraido.trim();
        String junto = a.isEmpty() ? b : b.isEmpty() ? a : a + " | " + b;
        return junto.length() > MAX_MENSAGEM ? junto.substring(0, MAX_MENSAGEM) : junto;
    }
}
