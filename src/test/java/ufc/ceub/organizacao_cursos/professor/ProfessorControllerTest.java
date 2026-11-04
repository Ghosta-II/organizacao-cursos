package ufc.ceub.organizacao_cursos.professor;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ufc.ceub.organizacao_cursos.exception.RecursoNaoEncontradoException;
import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Testes unitários da camada HTTP de professores.
 * O service é um mock: aqui se verifica status, validação e corpo da resposta.
 */
@WebMvcTest(ProfessorController.class)
class ProfessorControllerTest {

    private static final String PROFESSOR_VALIDO = """
            {"nome": "Carlos Fodao", "email": "carloss@ceub.br"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfessorService service;

    @Test
    @DisplayName("Caso 1 - POST /professores válido responde 201 com o professor criado")
    void deveResponder201AoCadastrar() throws Exception {
        when(service.criar(new ProfessorRequest("Carlos Fodao", "carloss@ceub.br")))
                .thenReturn(new ProfessorResponse(1L, "Carlos Fodao", "carloss@ceub.br"));

        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROFESSOR_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Carlos Fodao"))
                .andExpect(jsonPath("$.email").value("carloss@ceub.br"));
    }

    @Test
    @DisplayName("Caso 2 - POST /professores com e-mail repetido responde 400 com a mensagem da regra")
    void deveResponder400AoCadastrarEmailRepetido() throws Exception {
        when(service.criar(new ProfessorRequest("Carlos Fodao", "carloss@ceub.br")))
                .thenThrow(new RegraNegocioException("Já existe professor com este e-mail"));

        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROFESSOR_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Já existe professor com este e-mail"))
                .andExpect(jsonPath("$.detalhes", nullValue()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Caso 3 - POST /professores com dados inválidos responde 400 com detalhes por campo")
    void deveResponder400AoCadastrarDadosInvalidos() throws Exception {
        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "email": "abc"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes", containsInAnyOrder(
                        "nome: nome é obrigatório",
                        "email: e-mail inválido")));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Caso 4 - GET /professores/1 responde 200 com o professor")
    void deveResponder200AoBuscar() throws Exception {
        when(service.buscarPorId(1L))
                .thenReturn(new ProfessorResponse(1L, "Carlos Fodao", "carloss@ceub.br"));

        mockMvc.perform(get("/professores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Carlos Fodao"))
                .andExpect(jsonPath("$.email").value("carloss@ceub.br"));
    }

    @Test
    @DisplayName("Caso 5 - GET /professores/999 responde 404 com a mensagem de não encontrado")
    void deveResponder404AoBuscarInexistente() throws Exception {
        when(service.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Professor não encontrado"));

        mockMvc.perform(get("/professores/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Professor não encontrado"))
                .andExpect(jsonPath("$.detalhes", nullValue()));
    }

    @Test
    @DisplayName("Caso 13 - DELETE /professores/1 com cursos vinculados responde 400")
    void deveResponder400AoRemoverComCursos() throws Exception {
        doThrow(new RegraNegocioException("Professor possui cursos vinculados"))
                .when(service).remover(1L);

        mockMvc.perform(delete("/professores/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Professor possui cursos vinculados"));
    }

    @Test
    @DisplayName("Caso 21 - DELETE /professores/1 sem cursos vinculados responde 204 sem corpo")
    void deveResponder204AoRemover() throws Exception {
        doNothing().when(service).remover(1L);

        mockMvc.perform(delete("/professores/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).remover(1L);
    }

    @Test
    @DisplayName("Extra - POST com JSON quebrado responde 400 de requisição malformada")
    void deveResponder400AoReceberJsonMalformado() throws Exception {
        mockMvc.perform(post("/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Requisição malformada: verifique o JSON enviado"))
                .andExpect(jsonPath("$.detalhes", nullValue()));

        verifyNoInteractions(service);
    }
}
