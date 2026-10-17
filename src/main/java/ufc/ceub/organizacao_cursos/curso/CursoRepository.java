package ufc.ceub.organizacao_cursos.curso;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a dados de {@link Curso}.
 */
public interface CursoRepository extends JpaRepository<Curso, Long> {

    // Verifica se existe algum curso vinculado ao professor informado.
    boolean existsByProfessorId(Long professorId);
}
