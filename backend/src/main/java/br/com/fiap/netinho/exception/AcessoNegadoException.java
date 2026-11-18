package br.com.fiap.netinho.exception;

/**
 * Representa uma operacao autenticada, mas sobre dados de outra pessoa ou fora do que a conta pode fazer.
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) { super(mensagem); }
}
