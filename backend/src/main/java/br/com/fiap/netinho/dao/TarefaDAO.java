package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de tarefas e converte linhas do Oracle em modelos.
 */
@Repository
public class TarefaDAO {
    private final JdbcTemplate jdbc;
    public TarefaDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Tarefa> buscarPorId(Long id){return jdbc.query("SELECT * FROM TAREFA WHERE id_tar=?",rowMapper(),id).stream().findFirst();}
    public List<Tarefa> listarPorIdoso(Long id){return jdbc.query("SELECT * FROM TAREFA WHERE id_ido=? ORDER BY dh_tar_solicitacao DESC",rowMapper(),id);}
    public Long inserir(Tarefa x){Long id=jdbc.queryForObject("SELECT SQ_TAR.NEXTVAL FROM DUAL",Long.class);jdbc.update("INSERT INTO TAREFA VALUES (?,?,?,?,?,?,?,?)",id,x.getIdIdoso(),x.getIdGuia(),x.getModo().name(),x.getPedido(),x.getStatus().name(),x.getDataSolicitacao(),x.getDataConclusao());return id;}
    public void atualizarStatus(Long id,StatusTarefa status,java.time.LocalDateTime conclusao){jdbc.update("UPDATE TAREFA SET st_tar=?,dh_tar_conclusao=? WHERE id_tar=?",status.name(),conclusao,id);}
    public int excluir(Long id){return jdbc.update("DELETE FROM TAREFA WHERE id_tar=?",id);}
    private org.springframework.jdbc.core.RowMapper<Tarefa> rowMapper(){return (rs,n)->{Tarefa x=new Tarefa();x.setId(rs.getLong("id_tar"));x.setIdIdoso(rs.getLong("id_ido"));x.setIdGuia(DaoUtils.nullableInteger(rs,"id_gui"));x.setModo(TipoModoTarefa.valueOf(rs.getString("tp_tar_modo")));x.setPedido(rs.getString("ds_tar_pedido"));x.setStatus(StatusTarefa.valueOf(rs.getString("st_tar")));x.setDataSolicitacao(DaoUtils.localDateTime(rs.getTimestamp("dh_tar_solicitacao")));x.setDataConclusao(DaoUtils.localDateTime(rs.getTimestamp("dh_tar_conclusao")));return x;};}
}
