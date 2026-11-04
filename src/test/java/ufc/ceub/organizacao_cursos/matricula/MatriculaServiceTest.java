package ufc.ceub.organizacao_cursos.matricula;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ufc.ceub.organizacao_cursos.aluno.Aluno;
import ufc.ceub.organizacao_cursos.aluno.AlunoRepository;
import ufc.ceub.organizacao_cursos.curso.Curso;
import ufc.ceub.organizacao_cursos.curso.CursoRepository;
import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;
import ufc.ceub.organizacao_cursos.professor.Professor;

/**
 * Testes unitários das regras de negócio de matrículas.
 * Os repositories são mocks: nenhum banco de dados é usado.
 */
@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository repository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private MatriculaService service;

    @Test
    @DisplayName("Caso 14 - matricula aluno e devolve aluno, curso, professor e data atual")
    void deveMatricularAluno() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(alunoCadastrado()));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoCadastrado()));
        when(repository.existsByAlunoIdAndCursoId(1L, 1L)).thenReturn(false);
        when(repository.save(any(Matricula.class))).thenAnswer(invocacao -> {
            Matricula salva = invocacao.getArgument(0);
            ReflectionTestUtils.setField(salva, "id", 1L);
            return salva;
        });
        LocalDate antes = LocalDate.now();

        MatriculaResponse resposta = service.matricular(new MatriculaRequest(1L, 1L));

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.alunoId()).isEqualTo(1L);
        assertThat(resposta.alunoNome()).isEqualTo("Ana Souza");
        assertThat(resposta.cursoId()).isEqualTo(1L);
        assertThat(resposta.cursoNome()).isEqualTo("Engenharia de Software");
        assertThat(resposta.professorNome()).isEqualTo("Carlos Fodao");
        assertThat(resposta.dataMatricula()).isBetween(antes, LocalDate.now());
    }

    @Test
    @DisplayName("Caso 15 - recusa matrícula repetida do aluno no mesmo curso e não salva")
    void naoDeveMatricularAlunoJaMatriculado() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(alunoCadastrado()));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoCadastrado()));
        when(repository.existsByAlunoIdAndCursoId(1L, 1L)).thenReturn(true);
        MatriculaRequest request = new MatriculaRequest(1L, 1L);

        assertThatThrownBy(() -> service.matricular(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Aluno já matriculado neste curso");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Caso 16 - recusa matrícula de aluno inexistente e não salva")
    void naoDeveMatricularAlunoInexistente() {
        when(alunoRepository.findById(999L)).thenReturn(Optional.empty());
        MatriculaRequest request = new MatriculaRequest(999L, 1L);

        assertThatThrownBy(() -> service.matricular(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Aluno informado não existe");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Caso 17 - lista as matrículas do aluno com curso e professor")
    void deveListarMatriculasDoAluno() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(repository.findByAlunoId(1L)).thenReturn(List.of(matriculaCadastrada()));

        List<MatriculaResponse> resposta = service.listarPorAluno(1L);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).alunoId()).isEqualTo(1L);
        assertThat(resposta.get(0).cursoNome()).isEqualTo("Engenharia de Software");
        assertThat(resposta.get(0).professorNome()).isEqualTo("Carlos Fodao");
    }

    @Test
    @DisplayName("Caso 19 - cancela matrícula existente")
    void deveCancelarMatricula() {
        Matricula matricula = matriculaCadastrada();
        when(repository.findById(1L)).thenReturn(Optional.of(matricula));

        service.cancelar(1L);

        verify(repository).delete(matricula);
    }

    // Monta um aluno como se já estivesse salvo no banco.
    private Aluno alunoCadastrado() {
        Aluno aluno = new Aluno("Ana Souza", "ana@email.com", "2024001");
        ReflectionTestUtils.setField(aluno, "id", 1L);
        return aluno;
    }

    // Monta um curso, com seu professor, como se já estivesse salvo no banco.
    private Curso cursoCadastrado() {
        Professor professor = new Professor("Carlos Fodao", "carloss@ceub.br");
        ReflectionTestUtils.setField(professor, "id", 1L);
        Curso curso = new Curso("Engenharia de Software", 3600, professor);
        ReflectionTestUtils.setField(curso, "id", 1L);
        return curso;
    }

    // Monta uma matrícula como se já estivesse salva no banco.
    private Matricula matriculaCadastrada() {
        Matricula matricula = new Matricula(alunoCadastrado(), cursoCadastrado());
        ReflectionTestUtils.setField(matricula, "id", 1L);
        return matricula;
    }
}
