package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.*;
import br.com.fiap.netinho.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Classifica conteudos como risco de golpe, registra alertas e coordena os avisos permitidos.
 */
@Service
public class GolpeBO {
    private final AlertaGolpeDAO alertaDAO;
    private final IdosoDAO idosoDAO;
    private final ConsentimentoDAO consentimentoDAO;
    private final RedeApoioDAO redeDAO;
    private final FamiliarDAO familiarDAO;

    public GolpeBO(AlertaGolpeDAO alertaDAO,IdosoDAO idosoDAO,ConsentimentoDAO consentimentoDAO,RedeApoioDAO redeDAO,FamiliarDAO familiarDAO){
        this.alertaDAO=alertaDAO;this.idosoDAO=idosoDAO;this.consentimentoDAO=consentimentoDAO;this.redeDAO=redeDAO;this.familiarDAO=familiarDAO;
    }

    public Map<String,Object> verificar(Long idIdoso,String texto,boolean imagem,TipoOrigemAlerta origem){
        return registrar(idIdoso,texto,imagem,origem,null);
    }
    /** Verifica uma imagem ja analisada: o risco final e o maior entre as regras de texto e a analise. */
    public Map<String,Object> verificarComAnalise(Long idIdoso,String texto,AnaliseImagem.Resultado analise){
        return registrar(idIdoso,texto,true,TipoOrigemAlerta.MANUAL,analise);
    }
    private Map<String,Object> registrar(Long idIdoso,String texto,boolean imagem,TipoOrigemAlerta origem,AnaliseImagem.Resultado analise){
        idosoDAO.buscarPorId(idIdoso).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        if(origem==TipoOrigemAlerta.AUTOMATICO){
            Consentimento c=consentimentoDAO.buscarPorTipo(idIdoso,TipoConsentimento.LEITURA_NOTIFICACAO).orElse(null);
            if(c==null||c.getStatus()!=StatusConsentimento.CONCEDIDO)
                throw new RegraNegocioException("Alerta automatico bloqueado pela LGPD: conceda o consentimento LEITURA_NOTIFICACAO.");
        }
        if(texto.length()>500) throw new RegraNegocioException("A mensagem pode ter no maximo 500 caracteres.");
        String n=TarefaBO.normalizar(texto);int pontos=0;List<String> motivos=new ArrayList<>();
        if(n.matches(".*(dinheiro|pix|transferencia|pagar|pagamento).*")){pontos+=3;motivos.add("pede dinheiro, Pix ou transferencia");}
        if(n.matches(".*(senha|codigo|token|sms).*")){pontos+=4;motivos.add("pede senha, codigo, token ou SMS");}
        if(n.matches(".*(urgente|agora|bloqueado|ultimo aviso|premio|ganhou).*")){pontos+=2;motivos.add("usa urgencia ou ameaca");}
        boolean link=n.contains("http")||n.contains("bit.ly");
        if(link){pontos+=2;motivos.add("contem link");}
        if(link&&n.matches(".*(inss|beneficio|governo).*")){pontos+=2;motivos.add("usa INSS, beneficio ou governo junto com link");}
        if(n.contains("troquei de numero")||n.matches(".*se passa por parente.*")){pontos+=3;motivos.add("indica que trocou de numero ou se passa por parente");}
        TipoRiscoAlerta risco=pontos>=6?TipoRiscoAlerta.ALTO:pontos>=3?TipoRiscoAlerta.MEDIO:TipoRiscoAlerta.BAIXO;
        if(analise!=null&&analise.nivel()!=null&&analise.nivel().ordinal()>risco.ordinal())risco=analise.nivel();
        TipoConteudoAlerta conteudo=imagem?TipoConteudoAlerta.IMAGEM:(link?TipoConteudoAlerta.LINK:TipoConteudoAlerta.TEXTO);
        String motivo=motivos.isEmpty()?"Nenhum sinal forte de golpe foi encontrado.":String.join("; ",motivos)+".";
        if(analise!=null&&analise.motivo()!=null&&!analise.motivo().isBlank())motivo=analise.motivo().trim();
        if(motivo.length()>300) motivo=motivo.substring(0,300);
        AlertaGolpe x=new AlertaGolpe();x.setIdIdoso(idIdoso);x.setOrigem(origem);x.setConteudo(conteudo);x.setMensagem(texto);x.setRisco(risco);x.setMotivo(motivo);x.setFamiliarAvisado(risco==TipoRiscoAlerta.ALTO);x.setDataRegistro(LocalDateTime.now());x.setId(alertaDAO.inserir(x));
        Idoso idoso=idosoDAO.buscarPorId(idIdoso).orElseThrow();
        Map<String,Object> r=new LinkedHashMap<>();r.put("id",x.getId());r.put("nivel",risco.name());r.put("motivo",motivo);
        String oQueFazer=risco==TipoRiscoAlerta.ALTO?"Nao responda, nao abra links e ligue para um familiar.":risco==TipoRiscoAlerta.MEDIO?"Confirme por outro canal antes de agir.":"Nao compartilhe dados pessoais e procure ajuda se tiver duvida.";
        if(analise!=null&&analise.oQueFazer()!=null&&!analise.oQueFazer().isBlank()&&analise.nivel()==risco)oQueFazer=analise.oQueFazer().trim();
        r.put("oQueFazer",oQueFazer);
        r.put("respostaEmAudio",idoso.isSemLeitura());
        r.put("textoParaVoz",risco.name()+". "+motivo);
        if(risco==TipoRiscoAlerta.ALTO){
            List<String> familiares=new ArrayList<>();
            for(RedeApoio rede:redeDAO.listarPorIdoso(idIdoso))if(rede.isAutorizado())familiarDAO.buscarPorId(rede.getIdFamiliar()).ifPresent(f->familiares.add(f.getNome()));
            r.put("familiaresAvisados",familiares);
        }
        return r;
    }
    public List<AlertaGolpe> listar(Long idIdoso){return alertaDAO.listarPorIdoso(idIdoso);}
}
