package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de atendimentos e converte linhas do Oracle em modelos.
 */
@Repository
public class AtendimentoDAO {
    private final JdbcTemplate jdbc;
    public AtendimentoDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Atendimento> buscarPorId(Long id){return jdbc.query("SELECT * FROM ATENDIMENTO WHERE id_ate=?",rowMapper(),id).stream().findFirst();}
    public List<Atendimento> listarPorIdoso(Long id){return jdbc.query("SELECT * FROM ATENDIMENTO WHERE id_ido=? ORDER BY dh_ate_solicitacao DESC",rowMapper(),id);}
    public Long inserir(Atendimento x){Long id=jdbc.queryForObject("SELECT SQ_ATE.NEXTVAL FROM DUAL",Long.class);jdbc.update("INSERT INTO ATENDIMENTO VALUES (?,?,?,?,?,?,?,?,?,?,?)",id,x.getIdIdoso(),x.getIdTarefa(),x.getIdPasso(),x.getIdFamiliar(),x.getIdVoluntario(),x.getStatus().name(),x.getTentativas(),x.getDataSolicitacao(),x.getDataInicio(),x.getDataFim());return id;}
    public void iniciar(Long id,Long idFamiliar,Long idVoluntario,java.time.LocalDateTime inicio){jdbc.update("UPDATE ATENDIMENTO SET id_fam=?,id_vol=?,st_ate='EM_ANDAMENTO',dh_ate_inicio=? WHERE id_ate=?",idFamiliar,idVoluntario,inicio,id);}
    public void concluir(Long id,java.time.LocalDateTime fim){jdbc.update("UPDATE ATENDIMENTO SET st_ate='CONCLUIDO',dh_ate_fim=? WHERE id_ate=?",fim,id);}
    public List<Atendimento> concluidosPorVoluntario(Long idVol){return jdbc.query("SELECT * FROM ATENDIMENTO WHERE id_vol=? AND st_ate='CONCLUIDO' ORDER BY dh_ate_fim",rowMapper(),idVol);}
    private org.springframework.jdbc.core.RowMapper<Atendimento> rowMapper(){return (rs,n)->{Atendimento x=new Atendimento();x.setId(rs.getLong("id_ate"));x.setIdIdoso(rs.getLong("id_ido"));x.setIdTarefa(DaoUtils.nullableLong(rs,"id_tar"));x.setIdPasso(DaoUtils.nullableLong(rs,"id_pas"));x.setIdFamiliar(DaoUtils.nullableLong(rs,"id_fam"));x.setIdVoluntario(DaoUtils.nullableLong(rs,"id_vol"));x.setStatus(StatusAtendimento.valueOf(rs.getString("st_ate")));x.setTentativas(rs.getInt("qt_ate_tentativas"));x.setDataSolicitacao(DaoUtils.localDateTime(rs.getTimestamp("dh_ate_solicitacao")));x.setDataInicio(DaoUtils.localDateTime(rs.getTimestamp("dh_ate_inicio")));x.setDataFim(DaoUtils.localDateTime(rs.getTimestamp("dh_ate_fim")));return x;};}
}
