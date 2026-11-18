package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.*;
import br.com.fiap.netinho.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta pedidos, controla estados das tarefas e avanca os passos dos guias.
 */
@Service
public class TarefaBO {
    private final TarefaDAO tarefaDAO;
    private final IdosoDAO idosoDAO;
    private final RedeApoioDAO redeDAO;
    private final FamiliarDAO familiarDAO;
    private final GuiaDAO guiaDAO;
    private final PassoDAO passoDAO;
    private final Map<Long,Integer> progresso = new ConcurrentHashMap<>();

    public TarefaBO(TarefaDAO tarefaDAO,IdosoDAO idosoDAO,RedeApoioDAO redeDAO,FamiliarDAO familiarDAO,GuiaDAO guiaDAO,PassoDAO passoDAO){
        this.tarefaDAO=tarefaDAO;this.idosoDAO=idosoDAO;this.redeDAO=redeDAO;this.familiarDAO=familiarDAO;this.guiaDAO=guiaDAO;this.passoDAO=passoDAO;
    }

    public Tarefa criar(Long idIdoso,String pedido){
        Idoso idoso=idosoDAO.buscarPorId(idIdoso).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        String norma=normalizar(pedido);
        String[] bloqueios={"pix","banco","pagamento","transferencia","nubank","itau","bradesco","caixa","picpay","mercado pago"};
        for(String b:bloqueios) if(norma.contains(b)){
            Tarefa x=salvarRecusada(idoso,pedido,TipoModoTarefa.FAZ_PRA_MIM,null,"Pedido financeiro bloqueado por seguranca.");
            throw new RegraNegocioException("Pedido recusado: pagamentos e dados bancarios nao podem ser executados.");
        }
        if(Pattern.compile("\\b(senha|codigo|token|sms)\\b").matcher(norma).find()){
            salvarRecusada(idoso,pedido,TipoModoTarefa.FAZ_PRA_MIM,null,"Pedido para digitar segredo.");
            throw new RegraNegocioException("Pedido recusado: nao posso pedir ou digitar senha, codigo, token ou SMS.");
        }

        if(Pattern.compile("\\b(aposentadoria|aposentado|inss|beneficio)\\b").matcher(norma).find()){
            List<Guia> ativos=guiaDAO.ativos();
            Guia guia=ativos.stream().filter(g->normalizar(g.getNome()).contains("aposentadoria")||normalizar(g.getAplicativo()).contains("inss")).findFirst()
                    .orElseThrow(()->new RecursoNaoEncontradoException("Guia ativo correspondente nao encontrado."));
            return salvarGuia(idIdoso,guia,pedido);
        }

        String nome=extrairContato(pedido);
        List<Familiar> todos=new ArrayList<>();
        List<Familiar> opcoes=new ArrayList<>();
        for(RedeApoio r:redeDAO.listarPorIdoso(idIdoso)){
            Familiar f=familiarDAO.buscarPorId(r.getIdFamiliar()).orElse(null);
            if(f==null) continue;
            todos.add(f);
            if(nome!=null&&combina(nome,f,r)) opcoes.add(f);
        }
        String contatos=todos.isEmpty()?"nenhum contato cadastrado":String.join(", ",todos.stream().map(Familiar::getNome).toList());
        if(nome==null) throw new RecursoNaoEncontradoException("Nao entendi para quem enviar. Diga, por exemplo: manda mensagem pro Rafael. Contatos: "+contatos+".");
        if(opcoes.isEmpty()) throw new RecursoNaoEncontradoException("Nao encontrei '"+nome+"' na sua rede de apoio. Contatos: "+contatos+".");
        if(opcoes.size()>1) throw new ConflitoException("Contato ambiguo. Opcoes: "+opcoes.stream().map(Familiar::getNome).toList());
        Tarefa x=new Tarefa();x.setIdIdoso(idIdoso);x.setModo(TipoModoTarefa.FAZ_PRA_MIM);x.setPedido(pedido);x.setStatus(StatusTarefa.AGUARDANDO);x.setDataSolicitacao(LocalDateTime.now());x.setId(tarefaDAO.inserir(x));return x;
    }

    /** Inicia uma tarefa ME_ENSINA para o guia escolhido na lista do aplicativo. */
    public Tarefa iniciarGuia(Long idIdoso,Integer idGuia){
        idosoDAO.buscarPorId(idIdoso).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        Guia guia=guiaDAO.buscarPorId(idGuia).orElseThrow(()->new RecursoNaoEncontradoException("Guia nao encontrado."));
        if(guia.getStatus()!=StatusGuia.ATIVO) throw new RegraNegocioException("Este guia nao esta ativo.");
        if(passoDAO.listarPorGuia(idGuia).isEmpty()) throw new RegraNegocioException("Este guia ainda nao tem passos.");
        return salvarGuia(idIdoso,guia,"Quero aprender: "+guia.getNome());
    }
    private Tarefa salvarGuia(Long idIdoso,Guia guia,String pedido){
        Tarefa x=new Tarefa();x.setIdIdoso(idIdoso);x.setIdGuia(guia.getId());x.setModo(TipoModoTarefa.ME_ENSINA);x.setPedido(pedido);x.setStatus(StatusTarefa.EM_ANDAMENTO);x.setDataSolicitacao(LocalDateTime.now());
        x.setId(tarefaDAO.inserir(x));progresso.put(x.getId(),1);return x;
    }
    private Tarefa salvarRecusada(Idoso idoso,String pedido,TipoModoTarefa modo,Integer idGuia,String motivo){
        Tarefa x=new Tarefa();x.setIdIdoso(idoso.getId());x.setIdGuia(idGuia);x.setModo(modo);x.setPedido(pedido);x.setStatus(StatusTarefa.RECUSADA);x.setDataSolicitacao(LocalDateTime.now());x.setDataConclusao(LocalDateTime.now());x.setId(tarefaDAO.inserir(x));return x;
    }
    public Map<String,Object> confirmar(Long id,String resposta){
        Tarefa x=tarefaDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Tarefa nao encontrada."));
        if(x.getStatus()!=StatusTarefa.AGUARDANDO) throw new RegraNegocioException("A tarefa nao pode ser confirmada fora do estado AGUARDANDO.");
        String r=normalizar(resposta);
        if(!r.equals("sim")&&!r.equals("nao")) throw new RegraNegocioException("Confirmacao aceita somente SIM ou NAO.");
        LocalDateTime fim=LocalDateTime.now();
        StatusTarefa status=r.equals("sim")?StatusTarefa.CONCLUIDA:StatusTarefa.CANCELADA;
        tarefaDAO.atualizarStatus(id,status,fim);
        return Map.of("id",id,"status",status.name(),"dataConclusao",fim.toString(),"respostaEmAudio",false);
    }
    public void desfazer(Long id){
        Tarefa x=tarefaDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Tarefa nao encontrada."));
        if(x.getStatus()!=StatusTarefa.CONCLUIDA||x.getDataConclusao()==null||ChronoUnit.SECONDS.between(x.getDataConclusao(),LocalDateTime.now())>5)
            throw new RegraNegocioException("Desfazer somente funciona em ate 5 segundos apos a conclusao.");
        tarefaDAO.atualizarStatus(id,StatusTarefa.CANCELADA,LocalDateTime.now());
    }
    public Map<String,Object> passoAtual(Long id){
        Tarefa x=tarefaDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Tarefa nao encontrada."));
        if(x.getModo()!=TipoModoTarefa.ME_ENSINA||x.getIdGuia()==null) throw new RegraNegocioException("A tarefa nao possui guia.");
        int ordem=progresso.getOrDefault(id,1);
        Passo passo=passoDAO.listarPorGuia(x.getIdGuia()).stream().filter(p->p.getOrdem()==ordem).findFirst().orElseThrow(()->new RecursoNaoEncontradoException("Passo nao encontrado."));
        Idoso i=idosoDAO.buscarPorId(x.getIdIdoso()).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        Map<String,Object> r=new LinkedHashMap<>();r.put("txPasInstrucao",passo.getInstrucao());r.put("nmPasElemento",passo.getElemento());r.put("textoParaVoz",passo.getInstrucao());r.put("respostaEmAudio",i.isSemLeitura());return r;
    }
    public Map<String,Object> avancar(Long id,boolean concluido){
        if(!concluido) throw new RegraNegocioException("A pessoa precisa concluir o passo atual antes de avancar.");
        Tarefa x=tarefaDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Tarefa nao encontrada."));
        if(x.getModo()!=TipoModoTarefa.ME_ENSINA) throw new RegraNegocioException("Somente tarefas ME_ENSINA podem avancar passos.");
        List<Passo> passos=passoDAO.listarPorGuia(x.getIdGuia());
        int atual=progresso.getOrDefault(id,1);
        Passo atualPasso=passos.stream().filter(p->p.getOrdem()==atual).findFirst().orElseThrow(()->new RecursoNaoEncontradoException("Passo atual nao encontrado."));
        Optional<Passo> seguinte=passos.stream().filter(p->p.getOrdem()==atual+1).findFirst();
        if(seguinte.isPresent()){progresso.put(id,atual+1);return Map.of("passoAtual",atual+1,"concluido",false);}
        LocalDateTime fim=LocalDateTime.now();tarefaDAO.atualizarStatus(id,StatusTarefa.CONCLUIDA,fim);progresso.remove(id);
        return Map.of("concluido",true,"mensagem","Voce conseguiu! Fez tudo sozinha.","dataConclusao",fim.toString());
    }
    public String explicarOutroModo(Long id){
        Tarefa x=tarefaDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Tarefa nao encontrada."));
        Passo p=passoDAO.listarPorGuia(x.getIdGuia()).stream().filter(v->v.getOrdem()==progresso.getOrDefault(id,1)).findFirst().orElseThrow(()->new RecursoNaoEncontradoException("Passo nao encontrado."));
        return "Procure "+p.getElemento()+" na tela. Vou deixar ele brilhando. E so tocar nele.";
    }
    /** Pega a palavra depois de para/pra/pro, pulando artigos e possessivos ("pro meu filho", "para a Beatriz"). */
    private String extrairContato(String pedido){
        Matcher m=Pattern.compile("(?iu)(?<![\\p{L}])(?:para|pra|pro|pros|pras)\\s+(?:(?:o|a|os|as|meu|minha|meus|minhas|seu|sua)\\s+)*(\\p{L}+)").matcher(pedido);
        return m.find()?m.group(1):null;
    }
    /** O contato combina pelo primeiro nome, por qualquer parte do nome ou pelo parentesco (filho, neta...). */
    private boolean combina(String nome,Familiar f,RedeApoio r){
        String alvo=normalizar(nome);
        if(alvo.length()<2) return false;
        for(String parte:normalizar(f.getNome()).split("\\s+")) if(parte.equals(alvo)) return true;
        return normalizar(r.getParentesco()).equals(alvo);
    }
    public static String normalizar(String s){return s==null?"":java.text.Normalizer.normalize(s.toLowerCase(Locale.ROOT),java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+","");}
}
