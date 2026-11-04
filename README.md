# Organização de Cursos

API REST para gestão de professores, cursos e matrículas de alunos.

## Como rodar

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
.\mvnw.cmd spring-boot:run
```

## Como rodar com Docker

```bash
docker build -t organizacao-cursos .
docker run --rm -p 8080:8080 organizacao-cursos
```

A imagem publicada no Docker Hub pode ser usada direto:

```bash
docker run --rm -p 8080:8080 <usuario-dockerhub>/organizacao-cursos:latest
```

## Publicação no Docker Hub (CI)

O workflow `.github/workflows/ci.yml` compila, roda os testes e gera a imagem. Em push para `main` ou `develop` (ou execução manual em Actions → CI → Run workflow) a imagem é publicada como `<usuario>/<repositorio>:<sha do commit>`; em `main` também recebe a tag `latest`. Em pull request a imagem só é gerada, sem publicar.

Configuração necessária em Settings → Secrets and variables → Actions:

| Tipo | Nome | Valor |
|------|------|-------|
| Secret | `DOCKERHUB_USERNAME` | usuário do Docker Hub |
| Secret | `DOCKERHUB_TOKEN` | Personal Access Token do Docker Hub com permissão **Read & Write** |
| Variable | `DOCKERHUB_REPOSITORY` | nome do repositório no Docker Hub (padrão: `organizacao-cursos`) |

## Documentação da API

Com a aplicação rodando, acesse: http://localhost:8080/swagger-ui.html

## CENÁRIOS DE TESTE

Cenários de teste das 3 funcionalidades da API, com endpoint, método HTTP, dados enviados, status esperado e resposta esperada.

Observações:

- As respostas foram copiadas da execução real da API, exceto as dos cenários 500, que seguem o padrão de erro da API. Nos campos `timestamp` e `dataMatricula`, o valor foi substituído por `...`, pois muda a cada execução.
- Os dados ficam em memória (H2) e somem quando a aplicação reinicia. Os cenários dependem uns dos outros. Para reproduzi-los, inicie a aplicação do zero e execute na ordem: 1 a 12, 30 a 40, 16 a 26, 13, 41 a 51, 27, 52, 28, 14, 54 e 55. Os cenários 15, 29 e 53 (500) não são executados pelo Swagger.
- Nas respostas de erro, o campo `detalhes` é `null`, exceto nos erros de validação, em que traz a lista `"campo: motivo"`. A ordem dos itens dessa lista pode variar entre execuções.
- Respostas `204` não têm corpo.

### 1. GESTÃO DE PROFESSORES

| # | Cenário | Endpoint | Método | Dados enviados | Status esperado | Resposta esperada |
|---|---------|----------|--------|----------------|-----------------|-------------------|
| 1 | Cadastrar professor válido | `/professores` | POST | `{"nome":"Carlos Fodao","email":"carloss@ceub.br"}` | 201 | `{"id":1,"nome":"Carlos Fodao","email":"carloss@ceub.br"}` |
| 2 | Cadastrar professor com e-mail já existente | `/professores` | POST | `{"nome":"Carlos Fodao","email":"carloss@ceub.br"}` | 400 | `{"status":400,"mensagem":"Já existe professor com este e-mail","detalhes":null,"timestamp":"..."}` |
| 3 | Cadastrar professor sem nome e com e-mail inválido | `/professores` | POST | `{"nome":"","email":"abc"}` | 400 | `{"status":400,"mensagem":"Dados inválidos","detalhes":["nome: nome é obrigatório","email: e-mail inválido"],"timestamp":"..."}` |
| 4 | Listar professores | `/professores` | GET | — | 200 | `[{"id":1,"nome":"Carlos Fodao","email":"carloss@ceub.br"}]` |
| 5 | Buscar professor existente | `/professores/1` | GET | — | 200 | `{"id":1,"nome":"Carlos Fodao","email":"carloss@ceub.br"}` |
| 6 | Buscar professor inexistente | `/professores/999` | GET | — | 404 | `{"status":404,"mensagem":"Professor não encontrado","detalhes":null,"timestamp":"..."}` |
| 7 | Cadastrar um segundo professor | `/professores` | POST | `{"nome":"Maria Lima","email":"maria@ceub.br"}` | 201 | `{"id":2,"nome":"Maria Lima","email":"maria@ceub.br"}` |
| 8 | Atualizar professor | `/professores/2` | PUT | `{"nome":"Maria Lima Santos","email":"maria.santos@ceub.br"}` | 200 | `{"id":2,"nome":"Maria Lima Santos","email":"maria.santos@ceub.br"}` |
| 9 | Atualizar professor com e-mail de outro professor | `/professores/2` | PUT | `{"nome":"Maria Lima Santos","email":"carloss@ceub.br"}` | 400 | `{"status":400,"mensagem":"Já existe professor com este e-mail","detalhes":null,"timestamp":"..."}` |
| 10 | Atualizar professor inexistente | `/professores/999` | PUT | `{"nome":"Maria Lima Santos","email":"maria.santos@ceub.br"}` | 404 | `{"status":404,"mensagem":"Professor não encontrado","detalhes":null,"timestamp":"..."}` |
| 11 | Atualizar professor com dados inválidos | `/professores/2` | PUT | `{"nome":"","email":"abc"}` | 400 | `{"status":400,"mensagem":"Dados inválidos","detalhes":["nome: nome é obrigatório","email: e-mail inválido"],"timestamp":"..."}` |
| 12 | Remover professor inexistente | `/professores/999` | DELETE | — | 404 | `{"status":404,"mensagem":"Professor não encontrado","detalhes":null,"timestamp":"..."}` |
| 13 | Remover professor com cursos vinculados | `/professores/1` | DELETE | — | 400 | `{"status":400,"mensagem":"Professor possui cursos vinculados","detalhes":null,"timestamp":"..."}` |
| 14 | Remover professor sem cursos vinculados | `/professores/1` | DELETE | — | 204 | Sem corpo |
| 15 | Falha interna ao cadastrar professor | `/professores` | POST | `{"nome":"Carlos Fodao","email":"carloss@ceub.br"}` | 500 | `{"status":500,"mensagem":"Erro interno no servidor. Tente novamente mais tarde.","detalhes":null,"timestamp":"..."}` |

### 2. GESTÃO DE CURSOS

| # | Cenário | Endpoint | Método | Dados enviados | Status esperado | Resposta esperada |
|---|---------|----------|--------|----------------|-----------------|-------------------|
| 16 | Cadastrar curso válido | `/cursos` | POST | `{"nome":"Engenharia de Software","cargaHoraria":3600,"professorId":1}` | 201 | `{"id":1,"nome":"Engenharia de Software","cargaHoraria":3600,"professorId":1,"professorNome":"Carlos Fodao"}` |
| 17 | Cadastrar curso com professor inexistente | `/cursos` | POST | `{"nome":"Engenharia de Software","cargaHoraria":3600,"professorId":999}` | 400 | `{"status":400,"mensagem":"Professor informado não existe","detalhes":null,"timestamp":"..."}` |
| 18 | Cadastrar curso com carga horária negativa | `/cursos` | POST | `{"nome":"Engenharia de Software","cargaHoraria":-10,"professorId":1}` | 400 | `{"status":400,"mensagem":"Dados inválidos","detalhes":["cargaHoraria: carga horária deve ser positiva"],"timestamp":"..."}` |
| 19 | Listar cursos | `/cursos` | GET | — | 200 | `[{"id":1,"nome":"Engenharia de Software","cargaHoraria":3600,"professorId":1,"professorNome":"Carlos Fodao"}]` |
| 20 | Buscar curso existente | `/cursos/1` | GET | — | 200 | `{"id":1,"nome":"Engenharia de Software","cargaHoraria":3600,"professorId":1,"professorNome":"Carlos Fodao"}` |
| 21 | Buscar curso inexistente | `/cursos/999` | GET | — | 404 | `{"status":404,"mensagem":"Curso não encontrado","detalhes":null,"timestamp":"..."}` |
| 22 | Atualizar curso | `/cursos/1` | PUT | `{"nome":"Engenharia de Software","cargaHoraria":3200,"professorId":1}` | 200 | `{"id":1,"nome":"Engenharia de Software","cargaHoraria":3200,"professorId":1,"professorNome":"Carlos Fodao"}` |
| 23 | Atualizar curso com professor inexistente | `/cursos/1` | PUT | `{"nome":"Engenharia de Software","cargaHoraria":3200,"professorId":999}` | 400 | `{"status":400,"mensagem":"Professor informado não existe","detalhes":null,"timestamp":"..."}` |
| 24 | Atualizar curso inexistente | `/cursos/999` | PUT | `{"nome":"Engenharia de Software","cargaHoraria":3200,"professorId":1}` | 404 | `{"status":404,"mensagem":"Curso não encontrado","detalhes":null,"timestamp":"..."}` |
| 25 | Atualizar curso com dados inválidos | `/cursos/1` | PUT | `{"nome":"","cargaHoraria":0,"professorId":1}` | 400 | `{"status":400,"mensagem":"Dados inválidos","detalhes":["cargaHoraria: carga horária deve ser positiva","nome: nome é obrigatório"],"timestamp":"..."}` |
| 26 | Remover curso inexistente | `/cursos/999` | DELETE | — | 404 | `{"status":404,"mensagem":"Curso não encontrado","detalhes":null,"timestamp":"..."}` |
| 27 | Remover curso com matrículas | `/cursos/1` | DELETE | — | 400 | `{"status":400,"mensagem":"Curso possui matrículas ativas","detalhes":null,"timestamp":"..."}` |
| 28 | Remover curso sem matrículas | `/cursos/1` | DELETE | — | 204 | Sem corpo |
| 29 | Falha interna ao cadastrar curso | `/cursos` | POST | `{"nome":"Engenharia de Software","cargaHoraria":3600,"professorId":1}` | 500 | `{"status":500,"mensagem":"Erro interno no servidor. Tente novamente mais tarde.","detalhes":null,"timestamp":"..."}` |

### 3. MATRÍCULA (ALUNOS E VÍNCULO ALUNO-CURSO)

| # | Cenário | Endpoint | Método | Dados enviados | Status esperado | Resposta esperada |
|---|---------|----------|--------|----------------|-----------------|-------------------|
| 30 | Cadastrar aluno válido | `/alunos` | POST | `{"nome":"Ana Souza","email":"ana@email.com","ra":"2024001"}` | 201 | `{"id":1,"nome":"Ana Souza","email":"ana@email.com","ra":"2024001"}` |
| 31 | Cadastrar aluno com RA já existente | `/alunos` | POST | `{"nome":"Ana Souza","email":"ana@email.com","ra":"2024001"}` | 400 | `{"status":400,"mensagem":"Já existe aluno com este RA","detalhes":null,"timestamp":"..."}` |
| 32 | Cadastrar aluno sem nome, com e-mail inválido e sem RA | `/alunos` | POST | `{"nome":"","email":"x","ra":""}` | 400 | `{"status":400,"mensagem":"Dados inválidos","detalhes":["email: e-mail inválido","nome: nome é obrigatório","ra: RA é obrigatório"],"timestamp":"..."}` |
| 33 | Listar alunos | `/alunos` | GET | — | 200 | `[{"id":1,"nome":"Ana Souza","email":"ana@email.com","ra":"2024001"}]` |
| 34 | Buscar aluno existente | `/alunos/1` | GET | — | 200 | `{"id":1,"nome":"Ana Souza","email":"ana@email.com","ra":"2024001"}` |
| 35 | Buscar aluno inexistente | `/alunos/999` | GET | — | 404 | `{"status":404,"mensagem":"Aluno não encontrado","detalhes":null,"timestamp":"..."}` |
| 36 | Cadastrar um segundo aluno | `/alunos` | POST | `{"nome":"Bruno Lima","email":"bruno@email.com","ra":"2024002"}` | 201 | `{"id":2,"nome":"Bruno Lima","email":"bruno@email.com","ra":"2024002"}` |
| 37 | Atualizar aluno | `/alunos/1` | PUT | `{"nome":"Ana Souza Lima","email":"ana.lima@email.com","ra":"2024001"}` | 200 | `{"id":1,"nome":"Ana Souza Lima","email":"ana.lima@email.com","ra":"2024001"}` |
| 38 | Atualizar aluno com RA de outro aluno | `/alunos/1` | PUT | `{"nome":"Ana Souza Lima","email":"ana.lima@email.com","ra":"2024002"}` | 400 | `{"status":400,"mensagem":"Já existe aluno com este RA","detalhes":null,"timestamp":"..."}` |
| 39 | Atualizar aluno inexistente | `/alunos/999` | PUT | `{"nome":"Ana Souza Lima","email":"ana.lima@email.com","ra":"2024001"}` | 404 | `{"status":404,"mensagem":"Aluno não encontrado","detalhes":null,"timestamp":"..."}` |
| 40 | Atualizar aluno com dados inválidos | `/alunos/1` | PUT | `{"nome":"","email":"x","ra":""}` | 400 | `{"status":400,"mensagem":"Dados inválidos","detalhes":["ra: RA é obrigatório","email: e-mail inválido","nome: nome é obrigatório"],"timestamp":"..."}` |
| 41 | Matricular aluno em curso | `/matriculas` | POST | `{"alunoId":1,"cursoId":1}` | 201 | `{"id":1,"alunoId":1,"alunoNome":"Ana Souza Lima","cursoId":1,"cursoNome":"Engenharia de Software","professorNome":"Carlos Fodao","dataMatricula":"..."}` |
| 42 | Matricular aluno já matriculado no curso | `/matriculas` | POST | `{"alunoId":1,"cursoId":1}` | 400 | `{"status":400,"mensagem":"Aluno já matriculado neste curso","detalhes":null,"timestamp":"..."}` |
| 43 | Matricular aluno inexistente | `/matriculas` | POST | `{"alunoId":999,"cursoId":1}` | 400 | `{"status":400,"mensagem":"Aluno informado não existe","detalhes":null,"timestamp":"..."}` |
| 44 | Matricular aluno em curso inexistente | `/matriculas` | POST | `{"alunoId":1,"cursoId":999}` | 400 | `{"status":400,"mensagem":"Curso informado não existe","detalhes":null,"timestamp":"..."}` |
| 45 | Matricular sem informar aluno e curso | `/matriculas` | POST | `{"alunoId":null,"cursoId":null}` | 400 | `{"status":400,"mensagem":"Dados inválidos","detalhes":["cursoId: curso é obrigatório","alunoId: aluno é obrigatório"],"timestamp":"..."}` |
| 46 | Listar matrículas | `/matriculas` | GET | — | 200 | `[{"id":1,"alunoId":1,"alunoNome":"Ana Souza Lima","cursoId":1,"cursoNome":"Engenharia de Software","professorNome":"Carlos Fodao","dataMatricula":"..."}]` |
| 47 | Listar matrículas de um aluno | `/matriculas/aluno/1` | GET | — | 200 | `[{"id":1,"alunoId":1,"alunoNome":"Ana Souza Lima","cursoId":1,"cursoNome":"Engenharia de Software","professorNome":"Carlos Fodao","dataMatricula":"..."}]` |
| 48 | Listar matrículas de aluno inexistente | `/matriculas/aluno/999` | GET | — | 404 | `{"status":404,"mensagem":"Aluno não encontrado","detalhes":null,"timestamp":"..."}` |
| 49 | Listar matrículas de um curso | `/matriculas/curso/1` | GET | — | 200 | `[{"id":1,"alunoId":1,"alunoNome":"Ana Souza Lima","cursoId":1,"cursoNome":"Engenharia de Software","professorNome":"Carlos Fodao","dataMatricula":"..."}]` |
| 50 | Listar matrículas de curso inexistente | `/matriculas/curso/999` | GET | — | 404 | `{"status":404,"mensagem":"Curso não encontrado","detalhes":null,"timestamp":"..."}` |
| 51 | Cancelar matrícula inexistente | `/matriculas/999` | DELETE | — | 404 | `{"status":404,"mensagem":"Matrícula não encontrada","detalhes":null,"timestamp":"..."}` |
| 52 | Cancelar matrícula | `/matriculas/1` | DELETE | — | 204 | Sem corpo |
| 53 | Falha interna ao matricular aluno | `/matriculas` | POST | `{"alunoId":1,"cursoId":1}` | 500 | `{"status":500,"mensagem":"Erro interno no servidor. Tente novamente mais tarde.","detalhes":null,"timestamp":"..."}` |

### 4. ERROS GERAIS (TODAS AS ROTAS)

| # | Cenário | Endpoint | Método | Dados enviados | Status esperado | Resposta esperada |
|---|---------|----------|--------|----------------|-----------------|-------------------|
| 54 | JSON malformado | `/professores` | POST | `{"nome": }` | 400 | `{"status":400,"mensagem":"Requisição malformada: verifique o JSON enviado","detalhes":null,"timestamp":"..."}` |
| 55 | Rota inexistente | `/rota-que-nao-existe` | GET | — | 404 | `{"status":404,"mensagem":"Recurso não encontrado","detalhes":null,"timestamp":"..."}` |

### CENÁRIOS DE FALHA INTERNA (500)

Os cenários 15, 29 e 53 não podem ser provocados pelo Swagger. Simulação controlada em ambiente de teste: a camada de serviço é substituída por um mock que lança uma exceção inesperada. Verifica-se que a API responde 500 com a mensagem genérica, sem expor stack trace ou detalhes técnicos.

### ALUNOS NÃO POSSUEM REMOÇÃO

O histórico acadêmico é permanente: a API não oferece `DELETE` em `/alunos`. Isso pode ser conferido no Swagger, onde a seção de alunos lista apenas cadastrar, listar, buscar por id e atualizar.
