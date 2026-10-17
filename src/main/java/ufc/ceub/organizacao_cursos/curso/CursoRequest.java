package ufc.ceub.organizacao_cursos.curso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Dados de entrada para criação e atualização de curso.
 *
 * @param nome         nome do curso (obrigatório)
 * @param cargaHoraria carga horária do curso em horas (obrigatória e positiva)
 * @param professorId  id do professor responsável (obrigatório)
 */
public record CursoRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotNull(message = "carga horária é obrigatória")
        @Positive(message = "carga horária deve ser positiva")
        Integer cargaHoraria,

        @NotNull(message = "professor é obrigatório")
        Long professorId
) {
}
