package br.com.fiap.netinho.dto;

/**
 * Agrupa os formatos de entrada e resposta usados pelos endpoints REST.
 */
public class Dtos {
    public record ConfirmacaoRequest(String resposta) {}
    public record AvancarRequest(boolean concluido) {}
    public record IdResponse(Long id) {}
    public record TarefaRequest(Long idIdoso, String pedido, Integer idGuia) {}
    public record GolpeRequest(Long idIdoso, String texto, boolean imagem) {}
    public record AtendimentoRequest(Long idIdoso, Long idTarefa, Long idPasso, Integer tentativas) {}
    public record IniciarAtendimentoRequest(Long idFamiliar, Long idVoluntario) {}
    public record EmergenciaRequest(Long idIdoso) {}
    public record RedeApoioRequest(Long idFamiliar, String parentesco, Integer ordemContato, boolean autorizado) {}
    public record StatusRequest(String status) {}
    public record ModoSemLeituraRequest(boolean ativo) {}
}
