package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa uma permissao concedida ou revogada pelo idoso ou familiar.
 */
public class Consentimento {
    /** Coluna: id_con */ private Long id;
    /** Coluna: id_ido */ private Long idIdoso;
    /** Coluna: id_fam */ private Long idFamiliar;
    /** Coluna: tp_con */ private TipoConsentimento tipo;
    /** Coluna: st_con */ private StatusConsentimento status;
    /** Coluna: dh_con_concessao */ private LocalDateTime dataConcessao;
    /** Coluna: dh_con_revogacao */ private LocalDateTime dataRevogacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdIdoso() { return idIdoso; }
    public void setIdIdoso(Long idIdoso) { this.idIdoso = idIdoso; }
    public Long getIdFamiliar() { return idFamiliar; }
    public void setIdFamiliar(Long idFamiliar) { this.idFamiliar = idFamiliar; }
    public TipoConsentimento getTipo() { return tipo; }
    public void setTipo(TipoConsentimento tipo) { this.tipo = tipo; }
    public StatusConsentimento getStatus() { return status; }
    public void setStatus(StatusConsentimento status) { this.status = status; }
    public LocalDateTime getDataConcessao() { return dataConcessao; }
    public void setDataConcessao(LocalDateTime dataConcessao) { this.dataConcessao = dataConcessao; }
    public LocalDateTime getDataRevogacao() { return dataRevogacao; }
    public void setDataRevogacao(LocalDateTime dataRevogacao) { this.dataRevogacao = dataRevogacao; }
}
