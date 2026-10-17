package ufc.ceub.organizacao_cursos.matricula;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a dados de {@link Matricula}.
 */
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    // Verifica se o aluno já está matriculado no curso informado.
    boolean existsByAlunoIdAndCursoId(Long alunoId, Long cursoId);

    // Verifica se existe alguma matrícula vinculada ao curso informado.
    boolean existsByCursoId(Long cursoId);

    // Lista as matrículas de um aluno.
    List<Matricula> findByAlunoId(Long alunoId);

    // Lista as matrículas de um curso.
    List<Matricula> findByCursoId(Long cursoId);
}
