package ufc.ceub.organizacao_cursos.aluno;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a dados de {@link Aluno}.
 */
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    // Verifica se já existe aluno cadastrado com o RA informado.
    boolean existsByRa(String ra);
}
