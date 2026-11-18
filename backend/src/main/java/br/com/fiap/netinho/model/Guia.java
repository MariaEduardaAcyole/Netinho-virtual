package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa um roteiro de orientacao composto por passos.
 */
public class Guia {
    /** Coluna: id_gui */ private Integer id;
    /** Coluna: nm_gui */ private String nome;
    /** Coluna: nm_gui_aplicativo */ private String aplicativo;
    /** Coluna: ds_gui */ private String descricao;
    /** Coluna: st_gui */ private StatusGuia status;
    /** Coluna: dt_gui_atualizacao */ private LocalDateTime dataAtualizacao;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getAplicativo() { return aplicativo; }
    public void setAplicativo(String aplicativo) { this.aplicativo = aplicativo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public StatusGuia getStatus() { return status; }
    public void setStatus(StatusGuia status) { this.status = status; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
