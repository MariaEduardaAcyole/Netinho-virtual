package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa um lembrete agendado para um idoso, com tipo e estado.
 */
public class Lembrete {
    /** Coluna: id_lem */ private Long id;
    /** Coluna: id_ido */ private Long idIdoso;
    /** Coluna: id_fam */ private Long idFamiliar;
    /** Coluna: tp_lem */ private TipoLembrete tipo;
    /** Coluna: ds_lem */ private String descricao;
    /** Coluna: dh_lem_agendamento */ private LocalDateTime dataAgendamento;
    /** Coluna: st_lem */ private StatusLembrete status;
    /** Coluna: dh_lem_cumprimento */ private LocalDateTime dataCumprimento;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdIdoso() { return idIdoso; }
    public void setIdIdoso(Long idIdoso) { this.idIdoso = idIdoso; }
    public Long getIdFamiliar() { return idFamiliar; }
    public void setIdFamiliar(Long idFamiliar) { this.idFamiliar = idFamiliar; }
    public TipoLembrete getTipo() { return tipo; }
    public void setTipo(TipoLembrete tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(LocalDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }
    public StatusLembrete getStatus() { return status; }
    public void setStatus(StatusLembrete status) { this.status = status; }
    public LocalDateTime getDataCumprimento() { return dataCumprimento; }
    public void setDataCumprimento(LocalDateTime dataCumprimento) { this.dataCumprimento = dataCumprimento; }
}
