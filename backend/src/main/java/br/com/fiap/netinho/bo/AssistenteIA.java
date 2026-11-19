package br.com.fiap.netinho.bo;

import java.util.List;
import java.util.Optional;

/**
 * Modelo de linguagem que conversa com a pessoa idosa. A implementacao real usa a NVIDIA;
 * nos testes ela e substituida por um mock.
 */
public interface AssistenteIA {
    /** Uma fala da conversa: papel "user" (pessoa) ou "assistant" (Netinho). */
    record Mensagem(String papel, String texto) {}

    /** true quando ha chave de API configurada. */
    boolean disponivel();

    /** Texto devolvido pelo modelo, ou vazio se nao der para responder (sem chave ou falha). */
    Optional<String> responder(String instrucoes, List<Mensagem> conversa);
}
