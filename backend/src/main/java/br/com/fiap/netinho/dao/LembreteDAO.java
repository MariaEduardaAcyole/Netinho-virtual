package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de lembretes e converte linhas do Oracle em modelos.
 */
@Repository
public class LembreteDAO {
    private final JdbcTemplate jdbc;
    public LembreteDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Lembrete> buscarPorId(Long id){return jdbc.query("SELECT * FROM LEMBRETE WHERE id_lem=?",rowMapper(),id).stream().findFirst();}
    public List<Lembrete> listar(){return jdbc.query("SELECT * FROM LEMBRETE ORDER BY dh_lem_agendamento",rowMapper());}
    public List<Lembrete> listarPorIdoso(Long id){return jdbc.query("SELECT * FROM LEMBRETE WHERE id_ido=? ORDER BY dh_lem_agendamento",rowMapper(),id);}
    public Long inserir(Lembrete x){Long id=jdbc.queryForObject("SELECT SQ_LEM.NEXTVAL FROM DUAL",Long.class);jdbc.update("INSERT INTO LEMBRETE VALUES (?,?,?,?,?,?,?,?)",id,x.getIdIdoso(),x.getIdFamiliar(),x.getTipo().name(),x.getDescricao(),x.getDataAgendamento(),x.getStatus().name(),x.getDataCumprimento());return id;}
    public void atualizar(Long id,Lembrete x){jdbc.update("UPDATE LEMBRETE SET id_ido=?,id_fam=?,tp_lem=?,ds_lem=?,dh_lem_agendamento=?,st_lem=?,dh_lem_cumprimento=? WHERE id_lem=?",x.getIdIdoso(),x.getIdFamiliar(),x.getTipo().name(),x.getDescricao(),x.getDataAgendamento(),x.getStatus().name(),x.getDataCumprimento(),id);}
    public int excluir(Long id){return jdbc.update("DELETE FROM LEMBRETE WHERE id_lem=?",id);}
    private org.springframework.jdbc.core.RowMapper<Lembrete> rowMapper(){return (rs,n)->{Lembrete x=new Lembrete();x.setId(rs.getLong("id_lem"));x.setIdIdoso(rs.getLong("id_ido"));x.setIdFamiliar(DaoUtils.nullableLong(rs,"id_fam"));x.setTipo(TipoLembrete.valueOf(rs.getString("tp_lem")));x.setDescricao(rs.getString("ds_lem"));x.setDataAgendamento(DaoUtils.localDateTime(rs.getTimestamp("dh_lem_agendamento")));x.setStatus(StatusLembrete.valueOf(rs.getString("st_lem")));x.setDataCumprimento(DaoUtils.localDateTime(rs.getTimestamp("dh_lem_cumprimento")));return x;};}
}
