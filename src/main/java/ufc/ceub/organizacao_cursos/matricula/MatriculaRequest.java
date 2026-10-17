package ufc.ceub.organizacao_cursos.matricula;

import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para realizar uma matrícula.
 *
 * @param alunoId id do aluno a ser matriculado (obrigatório)
 * @param cursoId id do curso em que o aluno será matriculado (obrigatório)
 */
public record MatriculaRequest(
        @NotNull(message = "aluno é obrigatório")
        Long alunoId,

        @NotNull(message = "curso é obrigatório")
        Long cursoId
) {
}
