package br.com.fiap.netinho.exception;

/**
 * Representa uma operacao rejeitada por uma regra de negocio.
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) { super(mensagem); }
}
