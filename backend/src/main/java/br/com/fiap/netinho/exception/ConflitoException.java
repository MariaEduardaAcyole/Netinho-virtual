package br.com.fiap.netinho.exception;

/**
 * Representa uma tentativa de operacao que conflita com o estado ou as restricoes dos dados.
 */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) { super(mensagem); }
}
