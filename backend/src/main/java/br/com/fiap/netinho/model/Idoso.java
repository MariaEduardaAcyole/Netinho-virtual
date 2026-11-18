package br.com.fiap.netinho.model;

import java.time.LocalDateTime;

/**
 * Representa o perfil e as preferencias de acessibilidade de uma pessoa idosa.
 */
public class Idoso {
    /** Coluna: id_ido */
    private Long id;
    /** Coluna: nm_ido */
    private String nome;
    /** Coluna: dt_ido_nascimento */
    private LocalDateTime dataNascimento;
    /** Coluna: nr_ido_telefone */
    private String telefone;
    /** Coluna: tp_ido_aparelho */
    private TipoAparelho aparelho;
    /** Coluna: nm_ido_assistente */
    private String nomeAssistente;
    /** Coluna: tp_ido_voz */
    private TipoVoz voz;
    /** Coluna: tp_ido_jeito_falar */
    private TipoJeitoFalar jeitoFalar;
    /** Coluna: in_ido_sem_leitura */
    private boolean semLeitura;
    /** Coluna: in_ido_aceita_voluntario */
    private boolean aceitaVoluntario;
    /** Coluna: dt_ido_cadastro */
    private LocalDateTime dataCadastro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDateTime getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDateTime dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public TipoAparelho getAparelho() { return aparelho; }
    public void setAparelho(TipoAparelho aparelho) { this.aparelho = aparelho; }
    public String getNomeAssistente() { return nomeAssistente; }
    public void setNomeAssistente(String nomeAssistente) { this.nomeAssistente = nomeAssistente; }
    public TipoVoz getVoz() { return voz; }
    public void setVoz(TipoVoz voz) { this.voz = voz; }
    public TipoJeitoFalar getJeitoFalar() { return jeitoFalar; }
    public void setJeitoFalar(TipoJeitoFalar jeitoFalar) { this.jeitoFalar = jeitoFalar; }
    public boolean isSemLeitura() { return semLeitura; }
    public void setSemLeitura(boolean semLeitura) { this.semLeitura = semLeitura; }
    public boolean isAceitaVoluntario() { return aceitaVoluntario; }
    public void setAceitaVoluntario(boolean aceitaVoluntario) { this.aceitaVoluntario = aceitaVoluntario; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
