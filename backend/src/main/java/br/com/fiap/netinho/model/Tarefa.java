package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa um pedido do idoso e o estado do fluxo Faz pra mim ou Me ensina.
 */
public class Tarefa {
    /** Coluna: id_tar */ private Long id;
    /** Coluna: id_ido */ private Long idIdoso;
    /** Coluna: id_gui */ private Integer idGuia;
    /** Coluna: tp_tar_modo */ private TipoModoTarefa modo;
    /** Coluna: ds_tar_pedido */ private String pedido;
    /** Coluna: st_tar */ private StatusTarefa status;
    /** Coluna: dh_tar_solicitacao */ private LocalDateTime dataSolicitacao;
    /** Coluna: dh_tar_conclusao */ private LocalDateTime dataConclusao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdIdoso() { return idIdoso; }
    public void setIdIdoso(Long idIdoso) { this.idIdoso = idIdoso; }
    public Integer getIdGuia() { return idGuia; }
    public void setIdGuia(Integer idGuia) { this.idGuia = idGuia; }
    public TipoModoTarefa getModo() { return modo; }
    public void setModo(TipoModoTarefa modo) { this.modo = modo; }
    public String getPedido() { return pedido; }
    public void setPedido(String pedido) { this.pedido = pedido; }
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }
}
