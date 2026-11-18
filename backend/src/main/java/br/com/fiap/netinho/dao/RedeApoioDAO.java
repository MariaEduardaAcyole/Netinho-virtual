package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.RedeApoio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL dos relacionamentos entre idosos e familiares.
 */
@Repository
public class RedeApoioDAO {
    private final JdbcTemplate jdbc;
    public RedeApoioDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public List<RedeApoio> listarPorIdoso(Long id){return jdbc.query("SELECT * FROM REDE_APOIO WHERE id_ido=? ORDER BY nr_rea_ordem_contato",rowMapper(),id);}
    public Optional<RedeApoio> buscar(Long idIdoso,Long idFamiliar){return jdbc.query("SELECT * FROM REDE_APOIO WHERE id_ido=? AND id_fam=?",rowMapper(),idIdoso,idFamiliar).stream().findFirst();}
    public void inserir(RedeApoio x){jdbc.update("INSERT INTO REDE_APOIO VALUES (?,?,?,?,?,?)",x.getIdIdoso(),x.getIdFamiliar(),x.getParentesco(),x.getOrdemContato(),x.isAutorizado()?1:0,x.getDataCadastro());}
    public void atualizar(Long idIdoso,Long idFamiliar,RedeApoio x){jdbc.update("UPDATE REDE_APOIO SET ds_rea_parentesco=?,nr_rea_ordem_contato=?,in_rea_autorizado=?,dt_rea_cadastro=? WHERE id_ido=? AND id_fam=?",x.getParentesco(),x.getOrdemContato(),x.isAutorizado()?1:0,x.getDataCadastro(),idIdoso,idFamiliar);}
    public int excluir(Long idIdoso,Long idFamiliar){return jdbc.update("DELETE FROM REDE_APOIO WHERE id_ido=? AND id_fam=?",idIdoso,idFamiliar);}
    private org.springframework.jdbc.core.RowMapper<RedeApoio> rowMapper(){return (rs,n)->{RedeApoio x=new RedeApoio();x.setIdIdoso(rs.getLong("id_ido"));x.setIdFamiliar(rs.getLong("id_fam"));x.setParentesco(rs.getString("ds_rea_parentesco"));x.setOrdemContato(DaoUtils.nullableInteger(rs,"nr_rea_ordem_contato"));x.setAutorizado(rs.getInt("in_rea_autorizado")==1);x.setDataCadastro(DaoUtils.localDateTime(rs.getTimestamp("dt_rea_cadastro")));return x;};}
}
