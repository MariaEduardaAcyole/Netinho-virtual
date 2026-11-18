package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de voluntarios e converte linhas do Oracle em modelos.
 */
@Repository
public class VoluntarioDAO {
    private final JdbcTemplate jdbc;
    public VoluntarioDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Voluntario> buscarPorId(Long id){return jdbc.query("SELECT * FROM VOLUNTARIO WHERE id_vol=?",rowMapper(),id).stream().findFirst();}
    public List<Voluntario> listar(){return jdbc.query("SELECT * FROM VOLUNTARIO ORDER BY id_vol",rowMapper());}
    public Long inserir(Voluntario x){Long id=jdbc.queryForObject("SELECT SQ_VOL.NEXTVAL FROM DUAL",Long.class);jdbc.update("INSERT INTO VOLUNTARIO VALUES (?,?,?,?,?,?)",id,x.getNome(),x.getTelefone(),x.getEmail(),x.getStatus().name(),x.getDataCadastro());return id;}
    public void atualizar(Long id,Voluntario x){jdbc.update("UPDATE VOLUNTARIO SET nm_vol=?,nr_vol_telefone=?,ds_vol_email=?,st_vol=?,dt_vol_cadastro=? WHERE id_vol=?",x.getNome(),x.getTelefone(),x.getEmail(),x.getStatus().name(),x.getDataCadastro(),id);}
    public int excluir(Long id){return jdbc.update("DELETE FROM VOLUNTARIO WHERE id_vol=?",id);}
    public void atualizarStatus(Long id,StatusVoluntario status){jdbc.update("UPDATE VOLUNTARIO SET st_vol=? WHERE id_vol=?",status.name(),id);}
    public List<Voluntario> aprovados(){return jdbc.query("SELECT * FROM VOLUNTARIO WHERE st_vol='APROVADO' ORDER BY id_vol",rowMapper());}
    private org.springframework.jdbc.core.RowMapper<Voluntario> rowMapper(){return (rs,n)->{Voluntario x=new Voluntario();x.setId(rs.getLong("id_vol"));x.setNome(rs.getString("nm_vol"));x.setTelefone(rs.getString("nr_vol_telefone"));x.setEmail(rs.getString("ds_vol_email"));x.setStatus(StatusVoluntario.valueOf(rs.getString("st_vol")));x.setDataCadastro(DaoUtils.localDateTime(rs.getTimestamp("dt_vol_cadastro")));return x;};}
}
