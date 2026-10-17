package ufc.ceub.organizacao_cursos.matricula;

import java.time.LocalDate;

/**
 * Dados de saída de uma matrícula.
 *
 * @param id            identificador da matrícula
 * @param alunoId       id do aluno matriculado
 * @param alunoNome     nome do aluno matriculado
 * @param cursoId       id do curso
 * @param cursoNome     nome do curso
 * @param professorNome nome do professor responsável pelo curso
 * @param dataMatricula data em que a matrícula foi realizada
 */
public record MatriculaResponse(
        Long id,
        Long alunoId,
        String alunoNome,
        Long cursoId,
        String cursoNome,
        String professorNome,
        LocalDate dataMatricula
) {

    // Converte a entidade para o DTO de resposta.
    public static MatriculaResponse de(Matricula matricula) {
        return new MatriculaResponse(
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAluno().getNome(),
                matricula.getCurso().getId(),
                matricula.getCurso().getNome(),
                matricula.getCurso().getProfessor().getNome(),
                matricula.getDataMatricula());
    }
}
