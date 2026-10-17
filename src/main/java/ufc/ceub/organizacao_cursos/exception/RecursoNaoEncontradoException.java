package ufc.ceub.organizacao_cursos.exception;

/**
 * Lançada quando um recurso solicitado (professor, curso, matrícula etc.)
 * não existe. Respondida pela API com status 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
