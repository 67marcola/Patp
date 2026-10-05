# Evidências da interface do CRUD de gerenciamentos

Relatório do autor, por tarefa. Não substitui o Verificador independente. Dados fictícios e testes isolados; nenhum acesso ao MySQL configurado.

## T9: runners de comportamento

Premissas: preservar o runtime, usar dependências somente de desenvolvimento e não incluir artefatos gerados existentes no commit. Arquivos: package/lock, configurações Vitest/Playwright, setup, teste do formulário e ignore somente para relatórios novos.

Gates: `npm.cmd test` PASS **1/1**; `npm.cmd run build` PASS; `npm.cmd run lint` PASS. Probe de navegador lançou e encerrou Edge **154.0.4258.53** em headless pelo canal `msedge`. Nenhum banco ou aplicação foi iniciado. Sucesso do probe valida disponibilidade, não conta como teste de requisito ou E2E.

| Adequação direta: critério | Evidência e assertion | Resultado esperado | Resultado |
| --- | --- | --- | --- |
| Cancelar formulário retorna sem mutação, GER-31/base T9 | `frontend/src/pages/CriarGerenciamento.test.jsx:19`, `expect(screen.getByRole("heading", { name: "Quadro de instalações" }).textContent).toBe("Quadro de instalações")`; `:21`, `expect(fetchSpy).not.toHaveBeenCalled()` | Volta ao quadro; nenhuma request | PASS |
| Runner significativo, lint limitado e build, Done when T9 | `frontend/package.json:9`, lint explicitamente limitado; comandos acima com exit 0 | Infra executa comportamento e compila sem varrer dependências | PASS |
| Navegador instalado, Done when T9 | `frontend/playwright.config.js:10`, `channel: "msedge"`; probe acima | Lançamento do Edge existente sem banco/dados reais | PASS |

| Adequação reversa: assertion | Âncora | Manter |
| --- | --- | --- |
| `CriarGerenciamento.test.jsx:19`, heading `.toBe("Quadro de instalações")` | GER-31, cancelamento e retorno; primeiro teste significativo T9 | Sim |
| `CriarGerenciamento.test.jsx:21`, `expect(fetchSpy).not.toHaveBeenCalled()` | GER-31, nenhum envio; contagem é o resultado exigido nesta parte | Sim |

Adequação: assertion de estado renderizado e ausência de request, sem tautologias, skips ou remoções. Guidelines adicionais ausentes; padrões da skill aplicados. T9 concluída; cobertura restante GER-28–41 será entregue nas tarefas próprias.

## T10: contrato HTTP

Premissas: DTO seguro com criador resumido, PUT para mudança de estado, versão recebida no DTO e usada sem exposição como campo. Arquivos: api.js/api.test.js e registros da tarefa. API usa `VITE_API_URL` opcional e mantém localhost:8081/api como padrão. AbortSignal suportado nas consultas; nenhum retry automático.

Gate quick: `npm.cmd test` PASS **16/16**, sendo **15** casos novos HTTP e o caso anterior preservado. Antes da implementação, 14 dos 15 casos HTTP falharam, comprovando as lacunas; depois passaram sem alterar assertions. `Response` real exercita 204 sem corpo.

| Adequação direta: critério | Evidência e assertion | Resultado esperado | Resultado |
| --- | --- | --- | --- |
| Filtro/token/consulta, T10 e GER-28 | `frontend/src/services/api.test.js:24`, `.toEqual([quadro])`; `:26`, `toHaveBeenCalledWith(...?arquivado=${arquivado}, objectContaining({method:"GET",headers:{Authorization:"Bearer sessao"},signal:controller.signal}))` | Ambos filtros e sessão; DTO intacto | PASS |
| Buscar/criar DTO e payload, T10 e GER-06/35 | `api.test.js:34`, `.toEqual(quadro)`; `:41`, `.toEqual(quadro)`; `:43`, `.toBe("POST")`; `:44`, headers `.toEqual(...)`; `:45`, `expect(JSON.parse(request.body)).toEqual(dados)` | ID/nome/descrição/criador/estado/versão/permissão e corpo completo corretos | PASS |
| Editar versão/nome/descrição, T10 e GER-35 | `api.test.js:51`, `.toEqual({...quadro,nome:"Novo nome",versao:3})`; `:52`, URL `.toBe(.../9)`; `:53`, `.toBe("PUT")`; `:54`, corpo `.toEqual(dados)` | PUT por ID e versão; retorno persistido | PASS |
| Arquivar/restaurar 204, T10 e GER-34/41 | `api.test.js:62`, `.toBeUndefined()`; `:63`, URL `.toBe(.../${acao})`; `:64`, `.toBe("PUT")`; `:65`, corpo `.toEqual({versao:2})` | Sem JSON obrigatório, rotas e versão corretas | PASS |
| Erros exatos/status e ausência de retry, GER-33/40 | `api.test.js:76`, `.rejects.toMatchObject({message:mensagem,status})`; `:78`, `.toHaveBeenCalledTimes(1)` | 400/401/403/404/409 preservados; uma chamada | PASS |
| Comunicação e erro sem JSON, GER-33/40 | `api.test.js:84`, `.rejects.toMatchObject({message:"Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.",status:0})`; `:87`, `.toHaveBeenCalledTimes(1)`; `:92`, `.rejects.toMatchObject({message:"Não foi possível concluir a operação.",status:500})` | Sem falsa garantia de rollback ou detalhe interno | PASS |
| Consulta de etapas centralizada, GER-29 | `api.test.js:100`, `.toEqual(etapas)`; `:101`, URL `.toBe(.../9/etapas)` | Valores preservados e rota existente | PASS |

| Adequação reversa: assertions | Âncora | Manter |
| --- | --- | --- |
| `api.test.js:24/26`, lista `.toEqual` e request com token/filtro/signal | T10, contrato de consulta GER-28 | Sim |
| `api.test.js:34/35/41/43/44/45`, DTO `.toEqual` e URL/método/headers/corpo | T10, GER-06/35 | Sim |
| `api.test.js:51/52/53/54`, DTO/URL/PUT/corpo | T10, GER-35 | Sim |
| `api.test.js:62/63/64/65`, retorno undefined/URL/PUT/versão | T10, GER-34/41 e contrato 204 | Sim |
| `api.test.js:76/78`, rejeição com mensagem/status e única chamada | GER-33/40; sem retries | Sim |
| `api.test.js:84/87/92`, erro de comunicação e erro seguro500 | GER-33/40; texto prescrito | Sim |
| `api.test.js:100/101`, etapas e URL | GER-29, consulta atual preservada | Sim |

Adequação: todos os campos do DTO e payload são comparados por valor; requests complementam os resultados, não os substituem. Sem testes removidos, enfraquecidos ou ignorados. Erros da UI continuam pendentes nas tarefas próprias; nenhum desvio de especificação.
## T11: criação e edição por formulário

Premissas: etapas iniciais opcionais somente na criação; edição restrita a nome/descrição/versão; não truncar valores; versão não é campo editável. Arquivos: formulário, seus testes, CSS de ajuda/estado pendente e documentação.

Gates: `npm.cmd test` PASS **35/35**, sendo **20** casos de formulário e **15** HTTP; build e lint PASS. Os 16 primeiros casos novos falharam antes da implementação por lacunas concretas (rótulos, validação, edição e envios); assertions preservadas. Depois foram acrescentados 3 casos dos limites das etapas. Teste anterior mantido.

| Adequação direta: critério | Evidência e assertion | Resultado prescrito | Resultado |
| --- | --- | --- | --- |
| GER-06, criação opcional | `frontend/src/pages/CriarGerenciamento.test.jsx:45`, `expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({nome:"Instalações",descricao:"",etapas:[]})`; `:46`, `expect(atualizar).toHaveBeenCalledWith(quadro)` | Nome trim, descrição vazia, sem etapas, DTO retornado | PASS |
| GER-15/35, edição | `CriarGerenciamento.test.jsx:51`, nome `.toBe("Instalações")`; `:52`, descrição `.toBe("Postes")`; `:53`, botão de etapas `.toBeNull()`; `:58`, corpo `.toEqual({nome:"Corrigido",descricao:"Postes",versao:2})`; `:59`, método `.toBe("PUT")` | Campos atuais, sem etapas na edição, somente payload permitido | PASS |
| GER-03/04, limites recusados e aceitos | `CriarGerenciamento.test.jsx:71`, alerta `.toBe(mensagem)`; `:72/73`, valores `.toBe(nome/descricao)`; `:74`, `expect(fetchMock).not.toHaveBeenCalled()`; `:83`, corpo `.toEqual({nome,descricao,etapas:[]})` | Branco/121/256 recusados; nome1/120 e descrição0/255 enviados integralmente | PASS |
| GER-07, etapas opcionais/retirada antes de criar | `CriarGerenciamento.test.jsx:91`, alerta `.toBe("Preencha o nome e o setor das etapas com até 255 caracteres.")`; `:92`, nenhuma request; `:96`, campo `.toBeNull()`; `:99`, etapas `.toEqual([])`; `:110`, etapas `.toEqual([{nome:"Planejamento",setor:"Técnico",ordem:1}])` | Configuração inválida não envia; válida conserva os três campos; remover a última é permitido | PASS |
| GER-07, limites de etapa | `CriarGerenciamento.test.jsx:185`, alerta exato; `:186`, valor `.toBe("a".repeat(256))`; `:187`, nenhuma request; `:198`, etapas `.toEqual([{nome:"a".repeat(255),setor:"b".repeat(255),ordem:1}])` | Nome/setor256 recusados sem perder texto; 255 aceitos | PASS |
| GER-31/32, cancelar/pendente | `CriarGerenciamento.test.jsx:117`, `expect(voltar).toHaveBeenCalledTimes(1)`; `:118`, nenhuma request; `:128`, `expect(fetchMock).toHaveBeenCalledTimes(1)`; `:129`, botão pendente `.disabled.toBe(true)`; `:131`, DTO com versão3 recebido | Cancelar não grava; dois submits pendentes resultam em uma mutação | PASS |
| GER-33/40, HTTP/rede | `CriarGerenciamento.test.jsx:143`, alerta `.toBe(mensagem)`; `:144/145`, nome/descrição `.toBe("Rascunho"/"Postes")`; `:146/147`, sem callback de sucesso/retorno; `:154`, alerta com texto de comunicação exato; `:155`, nome preservado; `:156`, uma request; `:157/158`, sem sucesso/retorno | 400/403/409 e rede preservam campos e anunciam erro sem retry | PASS |
| GER-38/39, teclado/rótulos | `CriarGerenciamento.test.jsx:164/166/168/170/172`, `expect(document.activeElement).toBe(screen.getByRole(...)/screen.getByLabelText(...))`; `:174`, DTO após Enter | Tab percorre Voltar/nome/descrição/Cancelar/Salvar; Enter envia | PASS |

| Adequação reversa: assertions | Âncora | Manter |
| --- | --- | --- |
| `CriarGerenciamento.test.jsx:44/45/46`, retorno1/corpo completo/DTO; `:57`, DTO | GER-06/15/35, coordenação do formulário | Sim |
| `CriarGerenciamento.test.jsx:51/52/53/58/59`, valores/ausência de etapas/payload/PUT | GER-15/35 | Sim |
| `CriarGerenciamento.test.jsx:71/72/73/74/82/83`, alerta/valores/ausência de request/DTO/payload | GER-03/04 e campos preservados | Sim |
| `CriarGerenciamento.test.jsx:91/92/96/98/99/109/110`, alerta/ausência de request/campo retirado/DTO/etapas | GER-06/07 e etapas iniciais | Sim |
| `CriarGerenciamento.test.jsx:117/118/128/129/131`, cancelar/único envio/pendente/DTO | GER-31/32 | Sim |
| `CriarGerenciamento.test.jsx:143/144/145/146/147/154/155/156/157/158`, alerta/valores/callbacks ausentes/única request | GER-33/40 | Sim |
| `CriarGerenciamento.test.jsx:164/166/168/170/172/174`, foco/DTO ao Enter | GER-38/39 | Sim |
| `CriarGerenciamento.test.jsx:185/186/187/197/198`, alerta/valor preservado/nenhum envio/DTO/payload255 | GER-07, limites de contrato | Sim |

Adequação: saída, campos e todos os valores dos payloads comparados; callbacks são evidência complementar de conclusão/ausência de sucesso, não prova de persistência. Sem assertions modificadas/removidas, skips ou ampliação do CRUD de etapas. Padrões da skill seguidos, sem guidelines locais adicionais. T11 concluída.
