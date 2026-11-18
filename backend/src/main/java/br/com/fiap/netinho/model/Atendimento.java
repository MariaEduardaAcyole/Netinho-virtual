package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa uma solicitacao de ajuda e seu ciclo de vida.
 */
public class Atendimento {
    /** Coluna: id_ate */ private Long id;
    /** Coluna: id_ido */ private Long idIdoso;
    /** Coluna: id_tar */ private Long idTarefa;
    /** Coluna: id_pas */ private Long idPasso;
    /** Coluna: id_fam */ private Long idFamiliar;
    /** Coluna: id_vol */ private Long idVoluntario;
    /** Coluna: st_ate */ private StatusAtendimento status;
    /** Coluna: qt_ate_tentativas */ private Integer tentativas;
    /** Coluna: dh_ate_solicitacao */ private LocalDateTime dataSolicitacao;
    /** Coluna: dh_ate_inicio */ private LocalDateTime dataInicio;
    /** Coluna: dh_ate_fim */ private LocalDateTime dataFim;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdIdoso() { return idIdoso; }
    public void setIdIdoso(Long idIdoso) { this.idIdoso = idIdoso; }
    public Long getIdTarefa() { return idTarefa; }
    public void setIdTarefa(Long idTarefa) { this.idTarefa = idTarefa; }
    public Long getIdPasso() { return idPasso; }
    public void setIdPasso(Long idPasso) { this.idPasso = idPasso; }
    public Long getIdFamiliar() { return idFamiliar; }
    public void setIdFamiliar(Long idFamiliar) { this.idFamiliar = idFamiliar; }
    public Long getIdVoluntario() { return idVoluntario; }
    public void setIdVoluntario(Long idVoluntario) { this.idVoluntario = idVoluntario; }
    public StatusAtendimento getStatus() { return status; }
    public void setStatus(StatusAtendimento status) { this.status = status; }
    public Integer getTentativas() { return tentativas; }
    public void setTentativas(Integer tentativas) { this.tentativas = tentativas; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }
}
