package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa um conteudo analisado e o alerta de risco associado ao idoso.
 */
public class AlertaGolpe {
    /** Coluna: id_ale */ private Long id;
    /** Coluna: id_ido */ private Long idIdoso;
    /** Coluna: tp_ale_origem */ private TipoOrigemAlerta origem;
    /** Coluna: tp_ale_conteudo */ private TipoConteudoAlerta conteudo;
    /** Coluna: tx_ale_mensagem */ private String mensagem;
    /** Coluna: tp_ale_risco */ private TipoRiscoAlerta risco;
    /** Coluna: ds_ale_motivo */ private String motivo;
    /** Coluna: in_ale_familiar_avisado */ private boolean familiarAvisado;
    /** Coluna: dh_ale_registro */ private LocalDateTime dataRegistro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdIdoso() { return idIdoso; }
    public void setIdIdoso(Long idIdoso) { this.idIdoso = idIdoso; }
    public TipoOrigemAlerta getOrigem() { return origem; }
    public void setOrigem(TipoOrigemAlerta origem) { this.origem = origem; }
    public TipoConteudoAlerta getConteudo() { return conteudo; }
    public void setConteudo(TipoConteudoAlerta conteudo) { this.conteudo = conteudo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public TipoRiscoAlerta getRisco() { return risco; }
    public void setRisco(TipoRiscoAlerta risco) { this.risco = risco; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public boolean isFamiliarAvisado() { return familiarAvisado; }
    public void setFamiliarAvisado(boolean familiarAvisado) { this.familiarAvisado = familiarAvisado; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }
}
