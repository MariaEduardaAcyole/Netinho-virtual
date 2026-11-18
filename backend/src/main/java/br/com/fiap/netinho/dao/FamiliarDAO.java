package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.Familiar;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de familiares e converte linhas do Oracle em modelos.
 */
@Repository
public class FamiliarDAO {
    private final JdbcTemplate jdbc;
    public FamiliarDAO(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public Optional<Familiar> buscarPorId(Long id) { return jdbc.query("SELECT * FROM FAMILIAR WHERE id_fam=?",rowMapper(),id).stream().findFirst(); }
    public List<Familiar> listar() { return jdbc.query("SELECT * FROM FAMILIAR ORDER BY id_fam",rowMapper()); }
    public Long inserir(Familiar x) { Long id=jdbc.queryForObject("SELECT SQ_FAM.NEXTVAL FROM DUAL",Long.class); jdbc.update("INSERT INTO FAMILIAR VALUES (?,?,?,?,?)",id,x.getNome(),x.getTelefone(),x.getEmail(),x.getDataCadastro()); return id; }
    public void atualizar(Long id,Familiar x) { jdbc.update("UPDATE FAMILIAR SET nm_fam=?,nr_fam_telefone=?,ds_fam_email=?,dt_fam_cadastro=? WHERE id_fam=?",x.getNome(),x.getTelefone(),x.getEmail(),x.getDataCadastro(),id); }
    public int excluir(Long id) { return jdbc.update("DELETE FROM FAMILIAR WHERE id_fam=?",id); }
    private org.springframework.jdbc.core.RowMapper<Familiar> rowMapper() { return (rs,n)->{Familiar x=new Familiar();x.setId(rs.getLong("id_fam"));x.setNome(rs.getString("nm_fam"));x.setTelefone(rs.getString("nr_fam_telefone"));x.setEmail(rs.getString("ds_fam_email"));x.setDataCadastro(DaoUtils.localDateTime(rs.getTimestamp("dt_fam_cadastro")));return x;}; }
}
