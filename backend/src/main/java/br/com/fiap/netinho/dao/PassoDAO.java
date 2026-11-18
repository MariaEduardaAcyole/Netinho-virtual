package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.Passo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de passos de guias e converte linhas do Oracle em modelos.
 */
@Repository
public class PassoDAO {
    private final JdbcTemplate jdbc;
    public PassoDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Passo> buscarPorId(Long id){return jdbc.query("SELECT * FROM PASSO WHERE id_pas=?",rowMapper(),id).stream().findFirst();}
    public List<Passo> listarPorGuia(Integer idGuia){return jdbc.query("SELECT * FROM PASSO WHERE id_gui=? ORDER BY nr_pas_ordem",rowMapper(),idGuia);}
    public Long inserir(Passo x){Long id=jdbc.queryForObject("SELECT SQ_PAS.NEXTVAL FROM DUAL",Long.class);jdbc.update("INSERT INTO PASSO VALUES (?,?,?,?,?)",id,x.getIdGuia(),x.getOrdem(),x.getElemento(),x.getInstrucao());return id;}
    public void atualizar(Long id,Passo x){jdbc.update("UPDATE PASSO SET id_gui=?,nr_pas_ordem=?,nm_pas_elemento=?,tx_pas_instrucao=? WHERE id_pas=?",x.getIdGuia(),x.getOrdem(),x.getElemento(),x.getInstrucao(),id);}
    public int excluir(Long id){return jdbc.update("DELETE FROM PASSO WHERE id_pas=?",id);}
    private org.springframework.jdbc.core.RowMapper<Passo> rowMapper(){return (rs,n)->{Passo x=new Passo();x.setId(rs.getLong("id_pas"));x.setIdGuia(rs.getInt("id_gui"));x.setOrdem(rs.getInt("nr_pas_ordem"));x.setElemento(rs.getString("nm_pas_elemento"));x.setInstrucao(rs.getString("tx_pas_instrucao"));return x;};}
}
