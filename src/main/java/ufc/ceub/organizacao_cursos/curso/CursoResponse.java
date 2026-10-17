package ufc.ceub.organizacao_cursos.curso;

/**
 * Dados de saída de um curso.
 *
 * @param id            identificador do curso
 * @param nome          nome do curso
 * @param cargaHoraria  carga horária do curso em horas
 * @param professorId   id do professor responsável
 * @param professorNome nome do professor responsável
 */
public record CursoResponse(
        Long id,
        String nome,
        Integer cargaHoraria,
        Long professorId,
        String professorNome
) {

    // Converte a entidade para o DTO de resposta.
    public static CursoResponse de(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNome(),
                curso.getCargaHoraria(),
                curso.getProfessor().getId(),
                curso.getProfessor().getNome());
    }
}
