package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Executa operacoes SQL de guias e converte linhas do Oracle em modelos.
 */
@Repository
public class GuiaDAO {
    private final JdbcTemplate jdbc;
    public GuiaDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Optional<Guia> buscarPorId(Integer id){return jdbc.query("SELECT * FROM GUIA WHERE id_gui=?",rowMapper(),id).stream().findFirst();}
    public List<Guia> listar(){return jdbc.query("SELECT * FROM GUIA ORDER BY id_gui",rowMapper());}
    public Integer inserir(Guia x){Integer id=jdbc.queryForObject("SELECT SQ_GUI.NEXTVAL FROM DUAL",Integer.class);jdbc.update("INSERT INTO GUIA VALUES (?,?,?,?,?,?)",id,x.getNome(),x.getAplicativo(),x.getDescricao(),x.getStatus().name(),x.getDataAtualizacao());return id;}
    public void atualizar(Integer id,Guia x){jdbc.update("UPDATE GUIA SET nm_gui=?,nm_gui_aplicativo=?,ds_gui=?,st_gui=?,dt_gui_atualizacao=? WHERE id_gui=?",x.getNome(),x.getAplicativo(),x.getDescricao(),x.getStatus().name(),x.getDataAtualizacao(),id);}
    public int excluir(Integer id){return jdbc.update("DELETE FROM GUIA WHERE id_gui=?",id);}
    public List<Guia> ativos(){return jdbc.query("SELECT * FROM GUIA WHERE st_gui='ATIVO'",rowMapper());}
    private org.springframework.jdbc.core.RowMapper<Guia> rowMapper(){return (rs,n)->{Guia x=new Guia();x.setId(rs.getInt("id_gui"));x.setNome(rs.getString("nm_gui"));x.setAplicativo(rs.getString("nm_gui_aplicativo"));x.setDescricao(rs.getString("ds_gui"));x.setStatus(StatusGuia.valueOf(rs.getString("st_gui")));x.setDataAtualizacao(DaoUtils.localDateTime(rs.getTimestamp("dt_gui_atualizacao")));return x;};}
}
