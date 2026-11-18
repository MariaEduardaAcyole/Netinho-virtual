package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa o vinculo, a ordem e a autorizacao de contato entre idoso e familiar.
 */
public class RedeApoio {
    /** Coluna: id_ido */ private Long idIdoso;
    /** Coluna: id_fam */ private Long idFamiliar;
    /** Coluna: ds_rea_parentesco */ private String parentesco;
    /** Coluna: nr_rea_ordem_contato */ private Integer ordemContato;
    /** Coluna: in_rea_autorizado */ private boolean autorizado;
    /** Coluna: dt_rea_cadastro */ private LocalDateTime dataCadastro;

    public Long getIdIdoso() { return idIdoso; }
    public void setIdIdoso(Long idIdoso) { this.idIdoso = idIdoso; }
    public Long getIdFamiliar() { return idFamiliar; }
    public void setIdFamiliar(Long idFamiliar) { this.idFamiliar = idFamiliar; }
    public String getParentesco() { return parentesco; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }
    public Integer getOrdemContato() { return ordemContato; }
    public void setOrdemContato(Integer ordemContato) { this.ordemContato = ordemContato; }
    public boolean isAutorizado() { return autorizado; }
    public void setAutorizado(boolean autorizado) { this.autorizado = autorizado; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
