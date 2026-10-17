package ufc.ceub.organizacao_cursos.professor;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a dados de {@link Professor}.
 */
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    // Verifica se já existe professor cadastrado com o e-mail informado.
    boolean existsByEmail(String email);
}
