package ufc.ceub.organizacao_cursos.aluno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Testes unitários das regras de negócio de alunos.
 * O repository é um mock: nenhum banco de dados é usado.
 */
@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository repository;

    @InjectMocks
    private AlunoService service;

    @Test
    @DisplayName("Caso 6 - cadastra aluno com RA disponível")
    void deveCadastrarAluno() {
        when(repository.existsByRa("2024001")).thenReturn(false);
        when(repository.save(any(Aluno.class))).thenAnswer(invocacao -> {
            Aluno salvo = invocacao.getArgument(0);
            ReflectionTestUtils.setField(salvo, "id", 1L);
            return salvo;
        });

        AlunoResponse resposta = service.criar(new AlunoRequest("Ana Souza", "ana@email.com", "2024001"));

        assertThat(resposta).isEqualTo(new AlunoResponse(1L, "Ana Souza", "ana@email.com", "2024001"));
    }

    @Test
    @DisplayName("Caso 7 - recusa cadastro com RA repetido e não salva")
    void naoDeveCadastrarAlunoComRaRepetido() {
        when(repository.existsByRa("2024001")).thenReturn(true);
        AlunoRequest request = new AlunoRequest("Ana Souza", "ana@email.com", "2024001");

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe aluno com este RA");

        verify(repository, never()).save(any());
    }
}
