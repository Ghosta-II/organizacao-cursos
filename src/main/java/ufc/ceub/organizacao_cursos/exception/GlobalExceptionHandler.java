package ufc.ceub.organizacao_cursos.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Tratamento centralizado de exceções da API.
 * Converte cada exceção em um {@link ErroResponse} com o status HTTP adequado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Falha de validação (@Valid) nos dados de entrada → 400.
     * Cada campo inválido vira uma linha em detalhes no formato "campo: mensagem".
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroCampo)
                .toList();
        return montarResposta(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes);
    }

    /**
     * Violação de regra de negócio → 400 com a mensagem da exceção.
     */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex) {
        return montarResposta(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Recurso não encontrado → 404 com a mensagem da exceção.
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Corpo da requisição ilegível (JSON malformado, tipo incompatível etc.) → 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoMalformada(HttpMessageNotReadableException ex) {
        return montarResposta(HttpStatus.BAD_REQUEST, "Requisição malformada: verifique o JSON enviado", null);
    }

    /**
     * Rota inexistente → 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex) {
        return montarResposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", null);
    }

    /**
     * Qualquer outra exceção → 500 com mensagem fixa.
     * A exceção original é registrada apenas no log; nada dela vai para a resposta.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroGenerico(Exception ex) {
        log.error("Erro inesperado ao processar requisição", ex);
        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor. Tente novamente mais tarde.", null);
    }

    // Monta "campo: mensagem" a partir de um erro de validação.
    private String formatarErroCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }

    // Cria o ResponseEntity com o corpo padrão de erro.
    private ResponseEntity<ErroResponse> montarResposta(HttpStatus status, String mensagem, List<String> detalhes) {
        ErroResponse corpo = new ErroResponse(status.value(), mensagem, detalhes, LocalDateTime.now());
        return ResponseEntity.status(status).body(corpo);
    }
}
