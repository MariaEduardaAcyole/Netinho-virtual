package br.com.fiap.netinho.exception;

/**
 * Representa a busca por um recurso que nao existe.
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) { super(mensagem); }
}
