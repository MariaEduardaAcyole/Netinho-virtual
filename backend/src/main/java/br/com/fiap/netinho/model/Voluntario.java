package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa um voluntario e sua situacao de cadastro no sistema.
 */
public class Voluntario {
    /** Coluna: id_vol */ private Long id;
    /** Coluna: nm_vol */ private String nome;
    /** Coluna: nr_vol_telefone */ private String telefone;
    /** Coluna: ds_vol_email */ private String email;
    /** Coluna: st_vol */ private StatusVoluntario status;
    /** Coluna: dt_vol_cadastro */ private LocalDateTime dataCadastro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public StatusVoluntario getStatus() { return status; }
    public void setStatus(StatusVoluntario status) { this.status = status; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
