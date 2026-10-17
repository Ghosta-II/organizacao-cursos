package ufc.ceub.organizacao_cursos.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para criação e atualização de aluno.
 *
 * @param nome  nome do aluno (obrigatório)
 * @param email e-mail do aluno (obrigatório e em formato válido)
 * @param ra    registro acadêmico do aluno (obrigatório)
 */
public record AlunoRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotBlank(message = "e-mail é obrigatório")
        @Email(message = "e-mail inválido")
        String email,

        @NotBlank(message = "RA é obrigatório")
        String ra
) {
}
