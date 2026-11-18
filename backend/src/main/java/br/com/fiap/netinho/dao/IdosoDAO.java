package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de idosos e converte linhas do Oracle em modelos.
 */
@Repository
public class IdosoDAO {
    private final JdbcTemplate jdbc;
    public IdosoDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Idoso> buscarPorId(Long id) {
        return jdbc.query("SELECT * FROM IDOSO WHERE id_ido=?", rowMapper(), id).stream().findFirst();
    }
    public List<Idoso> listar() { return jdbc.query("SELECT * FROM IDOSO ORDER BY id_ido", rowMapper()); }
    public Long inserir(Idoso x) {
        Long id = jdbc.queryForObject("SELECT SQ_IDO.NEXTVAL FROM DUAL", Long.class);
        jdbc.update("INSERT INTO IDOSO VALUES (?,?,?,?,?,?,?,?,?,?,?)", id,x.getNome(),x.getDataNascimento(),x.getTelefone(),x.getAparelho().name(),x.getNomeAssistente(),x.getVoz().name(),x.getJeitoFalar().name(),x.isSemLeitura()?1:0,x.isAceitaVoluntario()?1:0,x.getDataCadastro());
        return id;
    }
    public void atualizar(Long id, Idoso x) {
        jdbc.update("UPDATE IDOSO SET nm_ido=?,dt_ido_nascimento=?,nr_ido_telefone=?,tp_ido_aparelho=?,nm_ido_assistente=?,tp_ido_voz=?,tp_ido_jeito_falar=?,in_ido_sem_leitura=?,in_ido_aceita_voluntario=?,dt_ido_cadastro=? WHERE id_ido=?",x.getNome(),x.getDataNascimento(),x.getTelefone(),x.getAparelho().name(),x.getNomeAssistente(),x.getVoz().name(),x.getJeitoFalar().name(),x.isSemLeitura()?1:0,x.isAceitaVoluntario()?1:0,x.getDataCadastro(),id);
    }
    public int excluir(Long id) { return jdbc.update("DELETE FROM IDOSO WHERE id_ido=?", id); }
    public void atualizarModoSemLeitura(Long id, boolean ativo) { jdbc.update("UPDATE IDOSO SET in_ido_sem_leitura=? WHERE id_ido=?",ativo?1:0,id); }
    private org.springframework.jdbc.core.RowMapper<Idoso> rowMapper() {
        return (rs,n) -> { Idoso x=new Idoso(); x.setId(rs.getLong("id_ido")); x.setNome(rs.getString("nm_ido")); x.setDataNascimento(DaoUtils.localDateTime(rs.getTimestamp("dt_ido_nascimento"))); x.setTelefone(rs.getString("nr_ido_telefone")); x.setAparelho(TipoAparelho.valueOf(rs.getString("tp_ido_aparelho"))); x.setNomeAssistente(rs.getString("nm_ido_assistente")); x.setVoz(TipoVoz.valueOf(rs.getString("tp_ido_voz"))); x.setJeitoFalar(TipoJeitoFalar.valueOf(rs.getString("tp_ido_jeito_falar"))); x.setSemLeitura(rs.getInt("in_ido_sem_leitura")==1); x.setAceitaVoluntario(rs.getInt("in_ido_aceita_voluntario")==1); x.setDataCadastro(DaoUtils.localDateTime(rs.getTimestamp("dt_ido_cadastro"))); return x; };
    }
}
