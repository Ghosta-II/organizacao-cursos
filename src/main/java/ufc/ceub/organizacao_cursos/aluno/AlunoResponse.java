package ufc.ceub.organizacao_cursos.aluno;

/**
 * Dados de saída de um aluno.
 *
 * @param id    identificador do aluno
 * @param nome  nome do aluno
 * @param email e-mail do aluno
 * @param ra    registro acadêmico do aluno
 */
public record AlunoResponse(
        Long id,
        String nome,
        String email,
        String ra
) {

    // Converte a entidade para o DTO de resposta.
    public static AlunoResponse de(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getRa());
    }
}
