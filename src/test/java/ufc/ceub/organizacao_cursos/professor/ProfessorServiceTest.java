package ufc.ceub.organizacao_cursos.professor;

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

import ufc.ceub.organizacao_cursos.curso.CursoRepository;
import ufc.ceub.organizacao_cursos.exception.RecursoNaoEncontradoException;
import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Testes unitários das regras de negócio de professores.
 * Os repositories são mocks: nenhum banco de dados é usado.
 */
@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    private ProfessorRepository repository;

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private ProfessorService service;

    @Test
    @DisplayName("Caso 1 - cadastra professor com e-mail disponível")
    void deveCadastrarProfessor() {
        when(repository.existsByEmail("carloss@ceub.br")).thenReturn(false);
        when(repository.save(any(Professor.class))).thenAnswer(invocacao -> {
            Professor salvo = invocacao.getArgument(0);
            ReflectionTestUtils.setField(salvo, "id", 1L);
            return salvo;
        });

        ProfessorResponse resposta = service.criar(new ProfessorRequest("Carlos Fodao", "carloss@ceub.br"));

        assertThat(resposta).isEqualTo(new ProfessorResponse(1L, "Carlos Fodao", "carloss@ceub.br"));
    }

    @Test
    @DisplayName("Caso 2 - recusa cadastro com e-mail repetido e não salva")
    void naoDeveCadastrarProfessorComEmailRepetido() {
        when(repository.existsByEmail("carloss@ceub.br")).thenReturn(true);
        ProfessorRequest request = new ProfessorRequest("Carlos Fodao", "carloss@ceub.br");

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe professor com este e-mail");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Caso 4 - busca professor existente pelo id")
    void deveBuscarProfessorPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(professorCadastrado()));

        ProfessorResponse resposta = service.buscarPorId(1L);

        assertThat(resposta).isEqualTo(new ProfessorResponse(1L, "Carlos Fodao", "carloss@ceub.br"));
    }

    @Test
    @DisplayName("Caso 5 - falha ao buscar professor inexistente")
    void naoDeveBuscarProfessorInexistente() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Professor não encontrado");
    }

    @Test
    @DisplayName("Caso 13 - recusa remover professor com cursos vinculados")
    void naoDeveRemoverProfessorComCursos() {
        when(repository.findById(1L)).thenReturn(Optional.of(professorCadastrado()));
        when(cursoRepository.existsByProfessorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Professor possui cursos vinculados");

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Caso 21 - remove professor sem cursos vinculados")
    void deveRemoverProfessorSemCursos() {
        Professor professor = professorCadastrado();
        when(repository.findById(1L)).thenReturn(Optional.of(professor));
        when(cursoRepository.existsByProfessorId(1L)).thenReturn(false);

        service.remover(1L);

        verify(repository).delete(professor);
    }

    // Monta um professor como se já estivesse salvo no banco.
    private Professor professorCadastrado() {
        Professor professor = new Professor("Carlos Fodao", "carloss@ceub.br");
        ReflectionTestUtils.setField(professor, "id", 1L);
        return professor;
    }
}
