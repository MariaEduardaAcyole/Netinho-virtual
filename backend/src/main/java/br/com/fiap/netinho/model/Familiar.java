package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa um familiar cadastrado para apoiar os idosos do sistema.
 */
public class Familiar {
    /** Coluna: id_fam */ private Long id;
    /** Coluna: nm_fam */ private String nome;
    /** Coluna: nr_fam_telefone */ private String telefone;
    /** Coluna: ds_fam_email */ private String email;
    /** Coluna: dt_fam_cadastro */ private LocalDateTime dataCadastro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
