package br.com.fiap.netinho.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Converte excecoes da aplicacao em respostas HTTP consistentes para os clientes da API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Map<String,Object>> resposta(HttpStatus status, String erro, String mensagem, HttpServletRequest request) {
        return ResponseEntity.status(status).body(Map.of("timestamp",LocalDateTime.now().toString(),"status",status.value(),"erro",erro,"mensagem",mensagem,"caminho",request.getRequestURI()));
    }
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String,Object>> naoEncontrado(RecursoNaoEncontradoException e,HttpServletRequest r){return resposta(HttpStatus.NOT_FOUND,"Nao encontrado",e.getMessage(),r);}
    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<Map<String,Object>> naoAutenticado(NaoAutenticadoException e,HttpServletRequest r){return resposta(HttpStatus.UNAUTHORIZED,"Nao autenticado",e.getMessage(),r);}
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String,Object>> acessoNegado(AcessoNegadoException e,HttpServletRequest r){return resposta(HttpStatus.FORBIDDEN,"Proibido",e.getMessage(),r);}
    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String,Object>> conflito(ConflitoException e,HttpServletRequest r){return resposta(HttpStatus.CONFLICT,"Conflito",e.getMessage(),r);}
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String,Object>> regra(RegraNegocioException e,HttpServletRequest r){return resposta(HttpStatus.UNPROCESSABLE_ENTITY,"Regra de negocio",e.getMessage(),r);}
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> banco(DataIntegrityViolationException e,HttpServletRequest r){return resposta(HttpStatus.CONFLICT,"Conflito no banco","Violacao de PK, UK ou FK.",r);}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> validacao(MethodArgumentNotValidException e,HttpServletRequest r){return resposta(HttpStatus.BAD_REQUEST,"Dados invalidos","Campos obrigatorios ou formatos invalidos.",r);}
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String,Object>> recursoNaoEncontrado(NoResourceFoundException e,HttpServletRequest r){return resposta(HttpStatus.NOT_FOUND,"Nao encontrado","Endpoint nao encontrado.",r);}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> interno(Exception e,HttpServletRequest r){return resposta(HttpStatus.INTERNAL_SERVER_ERROR,"Erro interno","Falha inesperada no servidor.",r);}
}
