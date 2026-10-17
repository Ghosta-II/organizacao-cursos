package ufc.ceub.organizacao_cursos.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo padrão de resposta de erro da API.
 *
 * @param status    código HTTP da resposta
 * @param mensagem  descrição resumida do erro
 * @param detalhes  lista opcional de detalhes (ex.: erros por campo); null quando não se aplica
 * @param timestamp momento em que o erro ocorreu
 */
public record ErroResponse(
        int status,
        String mensagem,
        List<String> detalhes,
        LocalDateTime timestamp
) {
}
