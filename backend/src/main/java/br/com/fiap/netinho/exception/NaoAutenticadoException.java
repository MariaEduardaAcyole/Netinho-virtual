package br.com.fiap.netinho.exception;

/**
 * Representa um login recusado (usuario ou senha errados, ou tentativas demais).
 */
public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String mensagem) { super(mensagem); }
}
