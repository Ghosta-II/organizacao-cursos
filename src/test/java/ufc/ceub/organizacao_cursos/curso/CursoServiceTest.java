package ufc.ceub.organizacao_cursos.curso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;
import ufc.ceub.organizacao_cursos.matricula.MatriculaRepository;
import ufc.ceub.organizacao_cursos.professor.Professor;
import ufc.ceub.organizacao_cursos.professor.ProfessorRepository;

/**
 * Testes unitários das regras de negócio de cursos.
 * Os repositories são mocks: nenhum banco de dados é usado.
 */
@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository repository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @InjectMocks
    private CursoService service;

    @Test
    @DisplayName("Caso 10 - cadastra curso e devolve o nome do professor")
    void deveCadastrarCurso() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professorCadastrado()));
        when(repository.save(any(Curso.class))).thenAnswer(invocacao -> {
            Curso salvo = invocacao.getArgument(0);
            ReflectionTestUtils.setField(salvo, "id", 1L);
            return salvo;
        });

        CursoResponse resposta = service.criar(new CursoRequest("Engenharia de Software", 3600, 1L));

        assertThat(resposta).isEqualTo(
                new CursoResponse(1L, "Engenharia de Software", 3600, 1L, "Carlos Fodao"));
    }

    @Test
    @DisplayName("Caso 11 - recusa cadastro com professor inexistente e não salva")
    void naoDeveCadastrarCursoComProfessorInexistente() {
        when(professorRepository.findById(999L)).thenReturn(Optional.empty());
        CursoRequest request = new CursoRequest("Engenharia de Software", 3600, 999L);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Professor informado não existe");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Caso 18 - recusa remover curso com matrículas")
    void naoDeveRemoverCursoComMatriculas() {
        when(repository.findById(1L)).thenReturn(Optional.of(cursoCadastrado()));
        when(matriculaRepository.existsByCursoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Curso possui matrículas ativas");

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Caso 20 - remove curso sem matrículas")
    void deveRemoverCursoSemMatriculas() {
        Curso curso = cursoCadastrado();
        when(repository.findById(1L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.existsByCursoId(1L)).thenReturn(false);

        service.remover(1L);

        verify(repository).delete(curso);
    }

    // Monta um professor como se já estivesse salvo no banco.
    private Professor professorCadastrado() {
        Professor professor = new Professor("Carlos Fodao", "carloss@ceub.br");
        ReflectionTestUtils.setField(professor, "id", 1L);
        return professor;
    }

    // Monta um curso como se já estivesse salvo no banco.
    private Curso cursoCadastrado() {
        Curso curso = new Curso("Engenharia de Software", 3600, professorCadastrado());
        ReflectionTestUtils.setField(curso, "id", 1L);
        return curso;
    }
}
