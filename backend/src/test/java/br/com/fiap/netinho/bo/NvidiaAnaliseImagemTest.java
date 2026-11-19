package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.model.TipoRiscoAlerta;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica a leitura da resposta do modelo da NVIDIA, que nem sempre vem como JSON puro.
 */
class NvidiaAnaliseImagemTest {
    private final NvidiaAnaliseImagem nvidia = new NvidiaAnaliseImagem("chave", "modelo", "http://localhost/v1");

    @Test void leJsonPuro(){
        var r = nvidia.interpretar("{\"nivel\":\"ALTO\",\"motivo\":\"Pede Pix.\",\"oQueFazer\":\"Nao pague.\",\"textoExtraido\":\"me manda um pix\"}");
        assertTrue(r.isPresent());
        assertEquals(TipoRiscoAlerta.ALTO, r.get().nivel());
        assertEquals("me manda um pix", r.get().textoExtraido());
    }
    @Test void leJsonDentroDeBlocoDeCodigoEComAcento(){
        var r = nvidia.interpretar("Claro!\n```json\n{\"nivel\":\"MÉDIO\",\"motivo\":\"Link estranho.\",\"oQueFazer\":\"Confirme.\"}\n```");
        assertEquals(TipoRiscoAlerta.MEDIO, r.orElseThrow().nivel());
        assertEquals("", r.get().textoExtraido());
    }
    @Test void recusaRespostaIncompletaOuSemJson(){
        assertTrue(nvidia.interpretar("Nao sei dizer.").isEmpty());
        assertTrue(nvidia.interpretar("{\"nivel\":\"TALVEZ\",\"motivo\":\"x\",\"oQueFazer\":\"y\"}").isEmpty());
        assertTrue(nvidia.interpretar("{\"nivel\":\"ALTO\",\"motivo\":\"\",\"oQueFazer\":\"y\"}").isEmpty());
        assertTrue(nvidia.interpretar(null).isEmpty());
    }
    @Test void semChaveFicaDesligadaENaoChamaNada(){
        var desligada = new NvidiaAnaliseImagem("", "modelo", "http://localhost/v1");
        assertFalse(desligada.disponivel());
        assertTrue(desligada.analisar("QUJD", "image/png", "").isEmpty());
    }
    @Test void configEscolheClaudeDepoisNvidia(){
        var cfg = new AnaliseImagemConfig();
        assertInstanceOf(ClaudeAnaliseImagem.class, cfg.analiseImagem("sk-ant", "claude", "nvapi", "m", "http://x/v1"));
        assertInstanceOf(NvidiaAnaliseImagem.class, cfg.analiseImagem("", "claude", "nvapi", "m", "http://x/v1"));
        assertFalse(cfg.analiseImagem("", "claude", "", "m", "http://x/v1").disponivel());
    }
}
