package ufc.ceub.organizacao_cursos.professor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para criação e atualização de professor.
 *
 * @param nome  nome do professor (obrigatório)
 * @param email e-mail do professor (obrigatório e em formato válido)
 */
public record ProfessorRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotBlank(message = "e-mail é obrigatório")
        @Email(message = "e-mail inválido")
        String email
) {
}
