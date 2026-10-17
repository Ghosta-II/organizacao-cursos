package ufc.ceub.organizacao_cursos.curso;

import java.util.List;

import org.springframework.stereotype.Service;

import ufc.ceub.organizacao_cursos.exception.RecursoNaoEncontradoException;
import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;
import ufc.ceub.organizacao_cursos.professor.Professor;
import ufc.ceub.organizacao_cursos.professor.ProfessorRepository;

/**
 * Regras de negócio de cursos.
 */
@Service
public class CursoService {

    private final CursoRepository repository;
    private final ProfessorRepository professorRepository;

    public CursoService(CursoRepository repository, ProfessorRepository professorRepository) {
        this.repository = repository;
        this.professorRepository = professorRepository;
    }

    /**
     * Cadastra um novo curso. O professor informado precisa existir.
     */
    public CursoResponse criar(CursoRequest request) {
        Professor professor = obterProfessorOuFalhar(request.professorId());
        Curso curso = new Curso(request.nome(), request.cargaHoraria(), professor);
        return CursoResponse.de(repository.save(curso));
    }

    /**
     * Lista todos os cursos cadastrados.
     */
    public List<CursoResponse> listar() {
        return repository.findAll().stream()
                .map(CursoResponse::de)
                .toList();
    }

    /**
     * Busca um curso pelo id.
     */
    public CursoResponse buscarPorId(Long id) {
        return CursoResponse.de(obterOuFalhar(id));
    }

    /**
     * Atualiza nome, carga horária e professor de um curso existente.
     * O professor informado precisa existir.
     */
    public CursoResponse atualizar(Long id, CursoRequest request) {
        Curso curso = obterOuFalhar(id);
        Professor professor = obterProfessorOuFalhar(request.professorId());
        curso.setNome(request.nome());
        curso.setCargaHoraria(request.cargaHoraria());
        curso.setProfessor(professor);
        return CursoResponse.de(repository.save(curso));
    }

    /**
     * Remove um curso pelo id.
     */
    public void remover(Long id) {
        Curso curso = obterOuFalhar(id);
        // TODO: impedir remoção quando houver matrículas (será implementado na funcionalidade de Matrículas)
        repository.delete(curso);
    }

    // Busca a entidade ou lança 404 quando o id não existe.
    private Curso obterOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado"));
    }

    // Busca o professor informado na requisição ou lança 400.
    // É 400 (e não 404) porque o erro está no dado enviado pelo cliente, não no recurso acessado.
    private Professor obterProfessorOuFalhar(Long professorId) {
        return professorRepository.findById(professorId)
                .orElseThrow(() -> new RegraNegocioException("Professor informado não existe"));
    }
}
