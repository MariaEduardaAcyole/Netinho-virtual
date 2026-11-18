package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de consentimentos e converte linhas do Oracle em modelos.
 */
@Repository
public class ConsentimentoDAO {
    private final JdbcTemplate jdbc;
    public ConsentimentoDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Consentimento> buscarPorId(Long id){return jdbc.query("SELECT * FROM CONSENTIMENTO WHERE id_con=?",rowMapper(),id).stream().findFirst();}
    public Optional<Consentimento> buscarPorTipo(Long idIdoso,TipoConsentimento tipo){return jdbc.query("SELECT * FROM CONSENTIMENTO WHERE id_ido=? AND tp_con=?",rowMapper(),idIdoso,tipo.name()).stream().findFirst();}
    public List<Consentimento> listarPorIdoso(Long id){return jdbc.query("SELECT * FROM CONSENTIMENTO WHERE id_ido=? ORDER BY dh_con_concessao DESC",rowMapper(),id);}
    public Long inserir(Consentimento x){Long id=jdbc.queryForObject("SELECT SQ_CON.NEXTVAL FROM DUAL",Long.class);jdbc.update("INSERT INTO CONSENTIMENTO VALUES (?,?,?,?,?,?,?)",id,x.getIdIdoso(),x.getIdFamiliar(),x.getTipo().name(),x.getStatus().name(),x.getDataConcessao(),x.getDataRevogacao());return id;}
    public void atualizarStatus(Long id,StatusConsentimento status,java.time.LocalDateTime data){jdbc.update("UPDATE CONSENTIMENTO SET st_con=?,dh_con_revogacao=? WHERE id_con=?",status.name(),status==StatusConsentimento.REVOGADO?data:null,id);}
    private org.springframework.jdbc.core.RowMapper<Consentimento> rowMapper(){return (rs,n)->{Consentimento x=new Consentimento();x.setId(rs.getLong("id_con"));x.setIdIdoso(rs.getLong("id_ido"));x.setIdFamiliar(DaoUtils.nullableLong(rs,"id_fam"));x.setTipo(TipoConsentimento.valueOf(rs.getString("tp_con")));x.setStatus(StatusConsentimento.valueOf(rs.getString("st_con")));x.setDataConcessao(DaoUtils.localDateTime(rs.getTimestamp("dh_con_concessao")));x.setDataRevogacao(DaoUtils.localDateTime(rs.getTimestamp("dh_con_revogacao")));return x;};}
}
