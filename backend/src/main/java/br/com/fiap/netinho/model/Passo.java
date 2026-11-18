package br.com.fiap.netinho.model;


/**
 * Representa uma instrucao ordenada pertencente a um guia.
 */
public class Passo {
    /** Coluna: id_pas */ private Long id;
    /** Coluna: id_gui */ private Integer idGuia;
    /** Coluna: nr_pas_ordem */ private Integer ordem;
    /** Coluna: nm_pas_elemento */ private String elemento;
    /** Coluna: tx_pas_instrucao */ private String instrucao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getIdGuia() { return idGuia; }
    public void setIdGuia(Integer idGuia) { this.idGuia = idGuia; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public String getElemento() { return elemento; }
    public void setElemento(String elemento) { this.elemento = elemento; }
    public String getInstrucao() { return instrucao; }
    public void setInstrucao(String instrucao) { this.instrucao = instrucao; }
}
