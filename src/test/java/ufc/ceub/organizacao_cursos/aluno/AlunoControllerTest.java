package ufc.ceub.organizacao_cursos.aluno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;

import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Testes unitários da camada HTTP de alunos.
 * O service é um mock: aqui se verifica status, validação e corpo da resposta.
 */
@WebMvcTest(AlunoController.class)
class AlunoControllerTest {

    private static final String ALUNO_VALIDO = """
            {"nome": "Ana Souza", "email": "ana@email.com", "ra": "2024001"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlunoService service;

    @Test
    @DisplayName("Caso 6 - POST /alunos válido responde 201 com o aluno criado")
    void deveResponder201AoCadastrar() throws Exception {
        when(service.criar(new AlunoRequest("Ana Souza", "ana@email.com", "2024001")))
                .thenReturn(new AlunoResponse(1L, "Ana Souza", "ana@email.com", "2024001"));

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALUNO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.ra").value("2024001"));
    }

    @Test
    @DisplayName("Caso 7 - POST /alunos com RA repetido responde 400 com a mensagem da regra")
    void deveResponder400AoCadastrarRaRepetido() throws Exception {
        when(service.criar(new AlunoRequest("Ana Souza", "ana@email.com", "2024001")))
                .thenThrow(new RegraNegocioException("Já existe aluno com este RA"));

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALUNO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Já existe aluno com este RA"))
                .andExpect(jsonPath("$.detalhes", nullValue()));
    }

    @Test
    @DisplayName("Caso 8 - POST /alunos com dados inválidos responde 400 com detalhes por campo")
    void deveResponder400AoCadastrarDadosInvalidos() throws Exception {
        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "email": "x", "ra": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "nome: nome é obrigatório",
                        "email: e-mail inválido",
                        "ra: RA é obrigatório")));

        verifyNoInteractions(service);
    }

    // No Swagger este caso é conferido visualmente; aqui se confere a estrutura do código.
    @Test
    @DisplayName("Caso 9 - alunos não têm endpoint DELETE nem remoção no service")
    void naoDeveExistirRemocaoDeAluno() {
        assertThat(AlunoController.class.getDeclaredMethods())
                .noneMatch(metodo -> metodo.isAnnotationPresent(DeleteMapping.class));
        assertThat(AlunoService.class.getDeclaredMethods())
                .noneMatch(metodo -> metodo.getName().equals("remover"));
    }
}
