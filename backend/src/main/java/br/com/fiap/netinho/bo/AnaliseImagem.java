package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.model.TipoRiscoAlerta;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Optional;

/**
 * Le um print ou foto de mensagem e avalia o risco de golpe.
 * A implementacao real usa o Claude; nos testes ela e substituida por um mock.
 */
public interface AnaliseImagem {

    /** Resposta estruturada da analise (o esquema JSON e derivado deste record). */
    record Resultado(
            @JsonPropertyDescription("Risco de golpe: BAIXO, MEDIO ou ALTO.") TipoRiscoAlerta nivel,
            @JsonPropertyDescription("Por que esse risco, em uma ou duas frases simples, em portugues, para uma pessoa idosa.") String motivo,
            @JsonPropertyDescription("O que a pessoa deve fazer agora, em uma frase simples e direta, em portugues.") String oQueFazer,
            @JsonPropertyDescription("Texto principal que aparece na imagem, transcrito. Vazio se nao houver texto.") String textoExtraido) {}

    /** true quando ha chave de API configurada. */
    boolean disponivel();

    /** Vazio quando a analise nao pode ser feita (sem chave, recusa ou falha); quem chama usa as regras de texto. */
    Optional<Resultado> analisar(String imagemBase64, String tipoImagem, String textoDaPessoa);
}
