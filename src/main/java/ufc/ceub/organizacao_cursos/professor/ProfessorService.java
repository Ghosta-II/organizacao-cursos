package ufc.ceub.organizacao_cursos.professor;

import java.util.List;

import org.springframework.stereotype.Service;

import ufc.ceub.organizacao_cursos.curso.CursoRepository;
import ufc.ceub.organizacao_cursos.exception.RecursoNaoEncontradoException;
import ufc.ceub.organizacao_cursos.exception.RegraNegocioException;

/**
 * Regras de negócio de professores.
 */
@Service
public class ProfessorService {

    private final ProfessorRepository repository;
    private final CursoRepository cursoRepository;

    public ProfessorService(ProfessorRepository repository, CursoRepository cursoRepository) {
        this.repository = repository;
        this.cursoRepository = cursoRepository;
    }

    /**
     * Cadastra um novo professor. O e-mail não pode se repetir.
     */
    public ProfessorResponse criar(ProfessorRequest request) {
        validarEmailDisponivel(request.email());
        Professor professor = new Professor(request.nome(), request.email());
        return ProfessorResponse.de(repository.save(professor));
    }

    /**
     * Lista todos os professores cadastrados.
     */
    public List<ProfessorResponse> listar() {
        return repository.findAll().stream()
                .map(ProfessorResponse::de)
                .toList();
    }

    /**
     * Busca um professor pelo id.
     */
    public ProfessorResponse buscarPorId(Long id) {
        return ProfessorResponse.de(obterOuFalhar(id));
    }

    /**
     * Atualiza nome e e-mail de um professor existente.
     * Se o e-mail for alterado, o novo e-mail também não pode se repetir.
     */
    public ProfessorResponse atualizar(Long id, ProfessorRequest request) {
        Professor professor = obterOuFalhar(id);
        if (!professor.getEmail().equals(request.email())) {
            validarEmailDisponivel(request.email());
        }
        professor.setNome(request.nome());
        professor.setEmail(request.email());
        return ProfessorResponse.de(repository.save(professor));
    }

    /**
     * Remove um professor pelo id.
     * Não é permitido remover professor que possua cursos vinculados.
     */
    public void remover(Long id) {
        Professor professor = obterOuFalhar(id);
        if (cursoRepository.existsByProfessorId(id)) {
            throw new RegraNegocioException("Professor possui cursos vinculados");
        }
        repository.delete(professor);
    }

    // Busca a entidade ou lança 404 quando o id não existe.
    private Professor obterOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));
    }

    // Lança 400 quando o e-mail já está cadastrado.
    private void validarEmailDisponivel(String email) {
        if (repository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe professor com este e-mail");
        }
    }
}
