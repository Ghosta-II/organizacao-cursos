package ufc.ceub.organizacao_cursos.exception;

/**
 * Lançada quando uma regra de negócio é violada
 * (ex.: aluno já matriculado no curso). Respondida pela API com status 400.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
