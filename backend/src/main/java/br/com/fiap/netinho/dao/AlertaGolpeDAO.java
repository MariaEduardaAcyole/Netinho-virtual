package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de alertas de golpe e converte linhas do Oracle em modelos.
 */
@Repository
public class AlertaGolpeDAO {
    private final JdbcTemplate jdbc;
    public AlertaGolpeDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<AlertaGolpe> buscarPorId(Long id){return jdbc.query("SELECT * FROM ALERTA_GOLPE WHERE id_ale=?",rowMapper(),id).stream().findFirst();}
    public List<AlertaGolpe> listarPorIdoso(Long id){return jdbc.query("SELECT * FROM ALERTA_GOLPE WHERE id_ido=? ORDER BY dh_ale_registro DESC",rowMapper(),id);}
    public Long inserir(AlertaGolpe x){Long id=jdbc.queryForObject("SELECT SQ_ALE.NEXTVAL FROM DUAL",Long.class);jdbc.update("INSERT INTO ALERTA_GOLPE VALUES (?,?,?,?,?,?,?,?,?)",id,x.getIdIdoso(),x.getOrigem().name(),x.getConteudo().name(),x.getMensagem(),x.getRisco().name(),x.getMotivo(),x.isFamiliarAvisado()?1:0,x.getDataRegistro());return id;}
    private org.springframework.jdbc.core.RowMapper<AlertaGolpe> rowMapper(){return (rs,n)->{AlertaGolpe x=new AlertaGolpe();x.setId(rs.getLong("id_ale"));x.setIdIdoso(rs.getLong("id_ido"));x.setOrigem(TipoOrigemAlerta.valueOf(rs.getString("tp_ale_origem")));x.setConteudo(TipoConteudoAlerta.valueOf(rs.getString("tp_ale_conteudo")));x.setMensagem(rs.getString("tx_ale_mensagem"));x.setRisco(TipoRiscoAlerta.valueOf(rs.getString("tp_ale_risco")));x.setMotivo(rs.getString("ds_ale_motivo"));x.setFamiliarAvisado(rs.getInt("in_ale_familiar_avisado")==1);x.setDataRegistro(DaoUtils.localDateTime(rs.getTimestamp("dh_ale_registro")));return x;};}
}
