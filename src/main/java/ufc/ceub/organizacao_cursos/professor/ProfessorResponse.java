package ufc.ceub.organizacao_cursos.professor;

/**
 * Dados de saída de um professor.
 *
 * @param id    identificador do professor
 * @param nome  nome do professor
 * @param email e-mail do professor
 */
public record ProfessorResponse(
        Long id,
        String nome,
        String email
) {

    // Converte a entidade para o DTO de resposta.
    public static ProfessorResponse de(Professor professor) {
        return new ProfessorResponse(professor.getId(), professor.getNome(), professor.getEmail());
    }
}
