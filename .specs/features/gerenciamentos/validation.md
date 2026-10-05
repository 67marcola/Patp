# Validation: CRUD de gerenciamentos - PASS

**Verdict: PASS técnico.** Os 41 critérios têm evidência de resultado, todos os gates do HEAD novo passam e a lacuna de teclado da rodada 1 foi corrigida por T16. Os quatro mutantes novos foram detectados por assertions de foco. UAT humano continua pendente.

**Data:** 2026-10-05, America/Sao_Paulo.

**Spec:** `.specs/features/gerenciamentos/spec.md`.

**Diff:** `5d8beb9..811842ab66c554bbf87506716b7ac13c259ad0d5`, branch `testes`.

**Verificador:** agente independente, autor diferente do verificador. Auditoria auxiliar somente de leitura; gates, sensor e parecer executados pelo verificador.

**Rodada:** 2/3. Nenhuma fonte ou teste real foi modificado pelo verificador.

## Gates e isolamento

Todos os gates foram repetidos numa worktree temporária detached no HEAD `811842a`. O Maven recompilou do zero, com `JAVA_HOME=C:\Program Files\Java\jdk-25.0.2` e seu bin antes dos shims Oracle. npm usou o lockfile via ci. Nenhum artefato de target/node_modules da árvore real foi reutilizado. A aplicação normal e o MySQL configurado não foram iniciados ou acessados.

| Gate | Comando na scratch | Resultado |
| --- | --- | --- |
| Backend build | `mvn.cmd -B clean verify`, em sistema | Exit 0; 97 testes; zero falhas, erros ou skips; JAR recompilado |
| Dependências | `npm.cmd ci`, em frontend | Exit 0; 98 pacotes; zero vulnerabilidades reportadas |
| Frontend | `npm.cmd test` | Exit 0; 4 arquivos, 71 testes; zero falhas/skips |
| Build frontend | `npm.cmd run build` | Exit 0; 22 módulos; bundle produzido |
| Lint | `npm.cmd run lint` | Exit 0 |
| Navegador | `npm.cmd run test:e2e` | Exit 0; 1/1 E2E Edge real; CRUD/reload/preservação e remoção por teclado |
| Spec estrutural | `python .agents/skills/tlc-spec-driven/scripts/validate_spec.py gerenciamentos --root . --strict` | Exit 0; zero erros/avisos |
| Tasks estrutural | `python .agents/skills/tlc-spec-driven/scripts/validate_tasks.py gerenciamentos --root .` | Exit 0; zero erros/avisos |

O E2E usou o script isolado com H2 em memória explícito/backend 18082 e Vite 4173. `frontend/playwright.config.js:12` impede reutilizar servidor existente. Não houve retry automático. O novo E2E preserva as assertions anteriores e acrescenta a remoção de uma segunda etapa temporária por Tab/Enter, sem gravá-la. O layout e overflow já inspecionados na rodada 1 têm fonte CSS/produção idêntica; assertions de largura atuais em `frontend/e2e/gerenciamentos.spec.js:72` e `:94`.

Evidências desta rodada: `%TEMP%/creral-verifier-gerenciamentos-round2-3213e7b611254755ad065db0205172e7/`, incluindo logs de todos os gates, `sensor-results.json`, `M9-vitest.log`, `M9-e2e.log`, `M10-vitest.log`, `M11-vitest.log` e `M12-vitest.log`. A worktree `head` foi removida com caminho absoluto validado dentro de TEMP. Fontes scratch restauradas byte a byte antes do descarte; portas 18082/4173 sem listener; nenhum helper próprio ficou ativo.

Porcelain completo real capturado antes da scratch e após limpeza, antes da atualização deste relatório: 131 linhas idênticas. SHA-256 de `porcelain-before.txt` e `porcelain-after.txt`: `A3725C459F445E8A74EBDE8282CDF138D5EEAA8DCEE522C376BC345D964F694C`. Nenhum artefato real foi apagado/restaurado/limpo; nesta rodada a única escrita real do verificador é validation.md.

## Critérios ancorados na spec

Mensagens abaixo correspondem ao texto literal definido na spec. As contagens e estados persistidos são assertions em banco/repositório, não apenas chamadas a mocks. PASS significa resultado testado; GAP significa evidência insuficiente. Não foram encontrados critérios com resultado impreciso na spec.

| AC / condição | Outcome exato da spec | Arquivo:linha e expressão assertiva | Resultado |
| --- | --- | --- | --- |
| GER-01, criação autenticada válida | HTTP 201, ativo, ID próprio persistido | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:38` — `status().isCreated()`; `:43` — `jsonPath("$.arquivado").value(false)`; `:368` — `jsonPath("$.id").isNumber()`; `:370` — `assertThat(quadros.findById(id)).isPresent()`; IDs distintos em `:63` | PASS |
| GER-02, autoria falsificada pelo cliente | Criador exclusivamente da sessão | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:36` envia criador 999; `:41` — `jsonPath("$.criador.id").value(criador.getId())`; `:49` — `assertThat(salvo.getCriador().getId()).isEqualTo(criador.getId())` | PASS |
| GER-03, nome 0/>120 na criação/edição autorizada ativa | HTTP 400, `Informe um nome entre 1 e 120 caracteres.` | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:74` e `:271` — `status().isBadRequest()` + `jsonPath("$.erro").value(NOME_ERRO)`; `:26` define literal exato; `:75` — count zero; limites 1/120, trim JS e UTF-16 em `:39`, `:82`, `:84` | PASS |
| GER-04, descrição >255 | HTTP 400, `A descrição deve ter até 255 caracteres.` | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:92` e `:275` — `isBadRequest()` + `jsonPath("$.erro").value("A descrição deve ter até 255 caracteres.")`; `:93` — count zero; 255 aceitos em `:40` | PASS |
| GER-05, dois nomes iguais | IDs distintos, etapas e demandas independentes | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:62` — `hasSize(2)`; `:63` — IDs `isNotEqualTo`; `:65` — primeira etapa `hasSize(1)`; `:66` — segunda `isEmpty()`; `:340` e `:341` — demandas `containsExactly(primeira.getId())` / `containsExactly(segunda.getId())`; `:342`–`:345` preservam o segundo quadro e conteúdo | PASS |
| GER-06, descrição/etapas omitidas | Aceitar, descrição vazia, sem erro nulo | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:57` envia somente nome; `:58` — `isCreated()`; `:59` — `jsonPath("$.descricao").value("")`; `:368` repete omissão de descrição com etapas válidas | PASS |
| GER-07, etapa inicial inválida/falha ao salvar | Zero novos quadros e etapas; 400 inválido/500 falha real | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:100`–`:102` — 400 + ambos counts `isZero()`; `:111` — 500 + erro genérico exato; `:112`/`:113` — ambos counts `isZero()` após constraint falhar na segunda etapa; formatos/limites em `:124`–`:126` e `:358`–`:360` | PASS |
| GER-08, lista sem filtro | HTTP 200, somente ativos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:138` — `isOk()`; `:139` — length 1 + ID ativo; `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:47` — `listarPorEstado(false)` contém exatamente ID legado com marcador nulo | PASS |
| GER-09, filtro Arquivados | HTTP 200, somente arquivados | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:140` — `isOk()`; `:141` — length 1 + ID arquivado; `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:48` — `containsExactly(arquivo.getId())` | PASS |
| GER-10, consulta de qualquer usuário por ID, arquivado/sem criador | HTTP 200, dados e estado, inclusive legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:142`–`:143` — terceiro recebe 200 + `arquivado=true` + `podeAdministrar=false`; `:141` — criador vazio; `frontend/e2e/gerenciamentos.spec.js:103`/`:104` — 200 + DTO arquivado com ID, nome, descrição, criador, versão 2; `GerenciamentoPersistenceTests.java:50`/`:51` preserva nome legado 180 e criador nulo | PASS |
| GER-11, ID ausente consultado/administrado | HTTP 404, `Gerenciamento não encontrado.` | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:282` e `:285` — `isNotFound()` + `jsonPath("$.erro").value("Gerenciamento não encontrado.")`, GET e PUT editar/arquivar/restaurar | PASS |
| GER-12, edição válida por criador/admin com versão atual | HTTP 200, valores persistidos, nova versão | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:172` — admin 200, nome novo, criador nulo, versão 1; `:186`/`:187` — criador 200, nome Novo, descrição Editada, versão 1; `:203`/`:204` — nome/descrição persistidos; `:215` — edição idêntica também versão 1 | PASS |
| GER-13, terceiro administra | HTTP 403, `Você não tem permissão para administrar este gerenciamento.` | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:157` — `isForbidden()` + `value(PERMISSAO_ERRO)` literal de `:27`, para editar/arquivar/restaurar; `:159`–`:161` nome, estado e versão preservados; `:257` precedência sobre arquivado | PASS |
| GER-14, funcionário administra quadro sem criador | Mesmo HTTP 403 e texto; sem autoria inferida | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:154` inclui dono null; `:157` — `isForbidden()` + mensagem exata; `:159`–`:161` estado inalterado; `:173` — admin não inventa criador | PASS |
| GER-15, editar nome/descrição | Só nome/descrição/versão mudam; preservar criador/estado/conteúdo | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:185` tenta falsificar criador/estado; `:187` mantém `arquivado=false` e criador original; `:188` — `assertThat(conteudoPersistido()).isEqualTo(conteudo)`; helper `sistema/src/test/java/com/patp/sistema/ApiIntegrationSupport.java:78`/`:79` compara todas as colunas das quatro tabelas com `select * ... order by id` | PASS |
| GER-16, versão antiga em mudança aplicável | HTTP 409, `Gerenciamento alterado por outro usuário. Atualize e tente novamente.`; sem sobrescrever | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:217` e `:231` — `isConflict()` + `value(VERSAO_ERRO)` literal de `:29`, editar/arquivar/restaurar; `:218`/`:219` mantém nome/versão; formatos inválidos 400 em `:243` e estado intacto em `:245`/`:246` | PASS |
| GER-17, arquivar atual por criador/admin | HTTP 204, estado arquivado persistido | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:190` — `isNoContent()` + corpo vazio; `:191` — `isArquivado().isTrue()`; admin em quadro alheio/null em `:309`/`:310`; versão 1 em `:311` | PASS |
| GER-18, restaurar atual por criador/admin | HTTP 204, estado ativo persistido | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:197` — `isNoContent()`; `:201` — `isArquivado().isFalse()`; admin alheio/null em `:314`/`:316`, versão 2 em `:317` | PASS |
| GER-19, repetir estado aplicado com versão válida, até antiga | HTTP 204, nenhum efeito em dados/versão | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:194` — arquivar repetido versão 0 retorna 204; `:195` — versão permanece 2; `:199` — restaurar repetido versão 0 retorna 204; `:202` — versão permanece 3; `:203`–`:206` metadados e conteúdo preservados | PASS |
| GER-20, arquivar/restaurar | Preservar IDs, atributos e vínculos de etapas/demandas/comentários/históricos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:312`/`:321` — `conteudoPersistido().isEqualTo(antes)` após cada estado, admin alheio/null; `:206` ciclo criador; helper `sistema/src/test/java/com/patp/sistema/ApiIntegrationSupport.java:78`/`:79` cobre todas as colunas; `frontend/e2e/gerenciamentos.spec.js:119` — etapas finais iguais às iniciais nos campos ID/nome/setor/ordem | PASS |
| GER-21, conteúdo válido em arquivado | Todas as 12 rotas: HTTP 409, `Gerenciamento arquivado. Restaure-o antes de alterar.`, sem gravar | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:259` — quadro; `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:55` — POST/PUT/DELETE etapa; `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43` — seis rotas; `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:38` e `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:38` — comentário/histórico. Todas `isConflict()` + texto exato, snapshots iguais em `:260`, Etapa `:56`, Processo `:44`, Comentário/Histórico `:39`. POST com vínculo falso em Processo `:77`/`:78` também 409 sem gravação | PASS |
| GER-22, mutação disputa com arquivar | Commit conteúdo antes do arquivo ou recusa sem escrita se arquivo primeiro | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:121` — segunda operação bloqueia antes do commit; `:125` — status `arquivoPrimeiro ? 409 : 204`; `:129` — count `arquivoPrimeiro ? 0 : 1`; `:131` nome Concorrente; `:133`/`:134` arquivado e versão 1. Duas ordens coordenadas por latches/transações reais. Guard comum e READ_COMMITTED nas quatro famílias inspecionados | PASS |
| GER-23, sessão ausente/inválida | HTTP 401 em rotas protegidas; sem escrita | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:105` — `isUnauthorized()` para 12 rotas e ambas as sessões; `:106`–`:108` snapshots/metadados intactos; `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:100`/`:102` — GET 401; `sistema/src/test/java/com/patp/sistema/AutenticacaoResponseTests.java:38` — HTTP real 401; `:41`/`:42` — bytes UTF-8 decodificados e textos exatos | PASS |
| GER-24, papel administrativo enviado em cadastro comum | Persistir FUNCIONARIO; sem privilégio por payload | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:28` envia ADMINISTRADOR; `:30` — `jsonPath("$.papel").value("FUNCIONARIO")`; `:34` — SQL papel `isEqualTo("FUNCIONARIO")` | PASS |
| GER-25, cadastro com ID existente/escolhido | HTTP 400, não criar/substituir conta | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:44`/`:56` — `isBadRequest()`; `:45` — count 1; `:47`–`:49` nome/email/hash preservados; `:57` — escolhido count zero | PASS |
| GER-26, seleção administrativa confiável | Só papel persistido pelo servidor concede administração | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:64` promoção SQL fictícia; `:68` — `/me` papel ADMINISTRADOR na sessão anterior; `:78` — legado FUNCIONARIO; `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:146` — `podeAdministrar=true` após promoção, contraposto a `:143`; cadastro falsificado permanece FUNCIONARIO em Usuario `:30`/`:34` | PASS |
| GER-27, respostas diretas/aninhadas do CRUD | Nenhuma senha/hash divulgado | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:31`/`:92` — `$..senha` `doesNotExist()`; `:36`/`:94` — resposta `doesNotContain(hashPersistido)`; `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:46`, `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:47`, `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:43`, `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:44` — senha aninhada ausente; modelo tem único campo de senha WRITE_ONLY | PASS |
| GER-28, abrir lista/alternar/abrir-voltar | Ativos inicialmente; Arquivados selecionável; preservar filtro | `frontend/src/pages/Gerenciamentos.test.jsx:46` — Ativos `aria-pressed=true`; `:47` — URL `?arquivado=false`; `:61` — Arquivados `aria-pressed=true` após voltar; `:62` — Restaurar disponível | PASS |
| GER-29, consultar arquivado | `Arquivado — somente consulta`, nenhum controle de alteração | `frontend/src/pages/Quadro.test.jsx:22` — texto literal `toBe`; `:24`/`:38` — Criar processo `toBeNull()`; `:40` — todas requests GET; `frontend/e2e/gerenciamentos.spec.js:88`/`:91` — texto visível e controle `toHaveCount(0)` em Edge | PASS |
| GER-30, controles administrativos | Editar/Arquivar só ativo administrável; Restaurar só arquivado administrável | `frontend/src/pages/Gerenciamentos.test.jsx:73`/`:74` — presença `toBe(permitido)` na matriz criador/admin/terceiro/funcionário legado; `:75` — Restaurar ausente em ativos; `:83` — Restaurar `toBe(permitido)`; `:84`/`:85` — editar/arquivar ausentes em arquivados; `:261`–`:263` permissão fresca atualiza botões | PASS |
| GER-31, cancelar edição/confirmação | Retornar sem mutação; foco na origem após confirmação | `frontend/src/pages/CriarGerenciamento.test.jsx:118`/`:119` — voltar uma vez e fetch nunca; `frontend/src/pages/Gerenciamentos.test.jsx:97`–`:99` — diálogo ausente, `document.activeElement===origem`, zero PUT; `:324`–`:330` usa origem atual conectada após navegação | PASS |
| GER-32, operação pendente/cliques adicionais | Um único envio da operação | `frontend/src/pages/CriarGerenciamento.test.jsx:127`/`:128` duas submissões; `:129` — fetch uma vez; `:130` botão disabled; `frontend/src/pages/Gerenciamentos.test.jsx:120` double click; `:121` — um PUT; `:122` confirmação disabled. Exercita guardas comuns criar/editar e arquivar/restaurar | PASS |
| GER-33, mutação/ comunicação falha | Erro correspondente, rascunho preservado, sem sucesso/retry criação | `frontend/src/pages/CriarGerenciamento.test.jsx:144` — alerta exato 400/403/409; `:145`/`:146` nome Rascunho e descrição Postes; `:147`/`:148` sem atualizar/voltar; `:155` texto exato de comunicação; `:156`–`:159` rascunho, uma request, zero callbacks; `frontend/src/pages/Gerenciamentos.test.jsx:277`–`:279` cartão mantido e uma tentativa | PASS |
| GER-34, mutação confirmada + recarga falha | `Alteração salva; não foi possível atualizar a lista.`; retry só consulta | `frontend/src/pages/Gerenciamentos.test.jsx:148` — alerta `toBe` literal; `:151` retry; `:152` vazio correto; `:153` um único PUT; `:154` alerta removido; `:241`–`:243` edição; `:306`–`:311` formulário pendente durante recarga | PASS |
| GER-35, confirmação + GET bem sucedido/reload | Exibir valores/estado persistidos após recarregar página | `frontend/e2e/gerenciamentos.spec.js:44`/`:45` reload criação; `:53` — DTO completo exato; `:65`–`:67` reload nome/descrição editados; `:99`/`:104` reload + arquivado versão 2; `:110`/`:114` reload + restaurado versão 3, campos originais mantidos; `:119` igualdade das etapas | PASS |
| GER-36, estados da listagem | Distinguir carregamento/vazio/erro; retry apenas GET | `frontend/src/pages/Gerenciamentos.test.jsx:45` — status Carregando; `:49` — heading vazio; `:50` status ausente; `:161` alerta Consulta indisponível; `:162` vazio ausente no erro; `:165` resultado após retry; `:166` todas requests GET | PASS |
| GER-37, solicitar arquivar | Confirmação nome + preservação + Cancelar/Arquivar | `frontend/src/pages/Gerenciamentos.test.jsx:93` — dialog nome Arquivar Instalações?; `:94` texto preservação; `:95` Cancelar; `:105` Arquivar exato; `frontend/e2e/gerenciamentos.spec.js:78`–`:83` ambos controles acionados por teclado | PASS |
| GER-38, todos os controles do CRUD | Alcançar e operar todos pelo teclado | `frontend/src/pages/CriarGerenciamento.test.jsx:207`/`:210` — foco nome/descrição; `:213` — Adicionar etapa; `:216`/`:220` — foco nome/setor iniciais; `:218`/`:222` — valores digitados por teclado; `:224` — foco Remover etapa; `:225`–`:228` — Enter remove ambos campos, sem request; `:231` — payload exato sem etapa; `:236`–`:238` — Voltar/Cancelar por Tab/Enter, uma saída e zero fetch. `frontend/src/test/keyboard.js:7` exige foco antes de Enter. `frontend/src/pages/Gerenciamentos.test.jsx:192` — retry teclado em erro inicial/pós-save; `:199`–`:201` — novo GET exato e PUT total 0/1. `frontend/src/pages/Quadro.test.jsx:25`–`:28` — Tab/foco/Enter em Voltar e callback uma vez; `frontend/src/pages/Quadro.test.jsx:69`–`:75` — retry teclado, dados recuperados e exatamente dois GET. `frontend/e2e/gerenciamentos.spec.js:13` exige `toBeFocused()`; aplicada a criar/adicionar/remover/salvar/editar/arquivar/cancelar/filtros/abrir/restaurar; `:39`–`:41` removem etapa 2 por teclado no Edge. M9–M12 detectam retirada do Tab | PASS |
| GER-39, campos do CRUD | Rótulos identificáveis por tecnologia assistiva | `frontend/src/pages/CriarGerenciamento.test.jsx:52`/`:53` — `getByLabelText` nome/descrição; `:94`/`:95` — nome/setor etapa; `:167`/`:169` — foco nos campos identificados; `frontend/e2e/gerenciamentos.spec.js:31`–`:35` — todos resolvidos por `getByLabel` | PASS |
| GER-40, exibir erro | Região acessível de alerta com erro correspondente | `frontend/src/pages/CriarGerenciamento.test.jsx:144`, `frontend/src/pages/Gerenciamentos.test.jsx:133`, `:148`, `:161`, `:277`, `frontend/src/pages/Quadro.test.jsx:53` — `findByRole("alert")` + `textContent.toBe(mensagemExata)` | PASS |
| GER-41, arquivar/restaurar confirmado | Remover cartão do filtro anterior, manter filtro e vazio se último | `frontend/src/pages/Gerenciamentos.test.jsx:106`/`:110` — Nenhum gerenciamento ativo/arquivado exatos; `:107`/`:111` — filtro atual `aria-pressed=true`; `frontend/e2e/gerenciamentos.spec.js:84`/`:85` e `:106`/`:107` — mesmos outcomes reais | PASS |

**Saldo:** 41/41 PASS, zero lacunas de precisão. GER-38 foi rederivado pelos outcomes dos novos testes e pelo sensor; a remoção e ambos os retries exigem foco real antes de Enter.

## Payloads, conjunction e integridade dos testes

| Contrato | Evidência exata |
| --- | --- |
| Criação | `frontend/src/pages/CriarGerenciamento.test.jsx:46` — body `toEqual({ nome: "Instalações", descricao: "", etapas: [] })`; server `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:39`–`:49` confere nome, descrição, criador, estado, versão e persistência juntos |
| Edição | `frontend/src/pages/CriarGerenciamento.test.jsx:59`/`:60` — body `{ nome: "Corrigido", descricao: "Postes", versao: 2 }` e método PUT; `frontend/src/pages/Gerenciamentos.test.jsx:228` — versão fresca 7; servidor mantém identidade/estado/snapshot em GerenciamentoApi `:223`/`:224` |
| Etapas iniciais | `frontend/src/pages/CriarGerenciamento.test.jsx:111` — array exato com nome Planejamento, setor Técnico e ordem 1; `:199` strings de 255 sem truncar; servidor GerenciamentoApi `:374`–`:377` confere campos e vínculo persistidos |
| Arquivar/restaurar | `frontend/src/services/api.test.js:62`–`:65` — sucesso 204 sem JSON, rota correspondente, PUT e body `{ versao: 2 }`; server GerenciamentoApi `:309`–`:321` verifica status, versão, estado, criador e snapshot |
| Rede | `frontend/src/services/api.test.js:84`–`:87` — erro exato, status 0 e uma tentativa; formulário não anuncia sucesso nem chama retorno em Form `:154`–`:158` |

Não há dependência exclusiva de `toHaveBeenCalled` para resultados de domínio ou payload. O servidor usa MockMvc e JDBC reais em H2. A falha parcial usa constraint real e compara zero pai/filhos após rollback. A disputa usa duas transações, latches, ordem de commit e resultado persistido, não um sleep como único oráculo. O helper de snapshot compara IDs, timestamps, valores e FKs, não só contagens. M6 confirma empiricamente que os testes distinguem o lock do pai.

No commit base havia 1 teste Java de contexto e nenhum teste frontend/E2E, conforme `git ls-tree` e fonte; o base não foi executado para evitar sua configuração de banco normal. O teste existente foi preservado e fortalecido com assertion de URL H2 em `sistema/src/test/java/com/patp/sistema/SistemaApplicationTests.java:22`. Totais atuais: 97 Java (+96), 71 Vitest (+71), 1 E2E (+1). T16 acrescentou seis casos React. Seu diff tem somente adições nos testes (40 linhas formulário, 36 lista, 16 quadro, 6 E2E), preservando os 65 casos e todas as assertions anteriores; helper novo com 9 linhas. Nenhum teste foi removido/enfraquecido; busca no diff/fontes não encontrou `.skip`, `.only`, `.todo`, `@Disabled` ou `SPEC_DEVIATION`.

| Arquivo/classe e casos executados | Âncoras dos testes do escopo |
| --- | --- |
| SistemaApplicationTests, 1 | Done when T1: contexto e datasource exclusivamente H2 |
| UsuarioIdentityTests, 7 | GER-23–27; T2 legado de papel; conta/ID/hash preservados |
| GerenciamentoPersistenceTests, 4 | GER-08/09/10/16/17/18/20/22; T3 defaults, legado, limites antigos, projeções e lock |
| GerenciamentoApiTests, 48 | GER-01–21/23/26/27; contratos nome/descrição/ordem/versão/filtro/JSON, ID e rollback; edge homônimos, autoria nula e precedência |
| EtapaArchiveTests, 6 | GER-20/21/22; três rotas arquivadas, comportamento ativo e ambas as ordens de concorrência |
| ProcessoArchiveTests, 11 | GER-20/21/22/27; seis rotas arquivadas, POST ID proibido, vínculo persistido contra falsificação, ativo e rollback da exclusão preexistente |
| ComentarioArchiveTests, 3 | GER-20/21/22; usuário/admin arquivado e semântica ativa/dados persistidos |
| HistoricoArchiveTests, 15 | GER-20/21/22/23/27; usuário/admin arquivado, ativo/automático e 12 rotas sem sessão/inválida |
| AutenticacaoResponseTests, 2 | GER-23/T14: HTTP real, bytes UTF-8 estritos e textos exatos |
| CriarGerenciamento.test.jsx, 23 | GER-03/04/06/07/31/32/33/38/39/40; payload, limites, cancelamento, envio pendente, erros e rótulos |
| Gerenciamentos.test.jsx, 27 | GER-28/30/31/32/33/34/36/37/38/40/41; filtros, matriz de permissões, confirmação, foco, navegação/recarga e respostas antigas |
| Quadro.test.jsx, 6 | GER-29/38/40; readonly, dados/consulta, teclado voltar e 500/404/rede |
| api.test.js, 15 | GER-08/09/10/11/16/17/18/23/33/34/35; T10 methods/routes/token/body/204/erro/status/sem retry |
| gerenciamentos.spec.js, 1 | GER-01/02/06/17/18/20/28/29/31/35/37/38/41 e T13; navegador real, reload, teclado, preservação e overflow |

Todos os testes do diff têm uma âncora de AC, contrato, edge case ou Done when. As camadas de domínio e rotas têm resultados positivos, limites, recusas e persistência; os controles secundários de teclado também têm foco, ação, payload e ausência de retry de mutação demonstrados em T16.


## Sensor de discriminação

Profundidade expandida do mesmo conjunto de produção: oito kills históricos válidos, incluindo seis de auth/integridade/concorrência, e quatro mutações de teclado executadas nesta rodada. M9 foi repetido no novo HEAD; M10–M12 conferem as áreas adjacentes da correção. Cada mutação foi aplicada isoladamente e restaurada byte a byte em finally. Falhas de compilação ou de helper não foram contadas como kill.

Na rodada 1 (`70497e74f84e4195ba7aa1be517f3d84c28dd46d`), M9 retirou Remover etapa do Tab e sobreviveu aos 65 testes React e ao E2E. O relatório FAIL dessa rodada está preservado no histórico Git `811842a:.specs/features/gerenciamentos/validation.md`. T16 acrescentou testes, sem mudar produção nem remover/enfraquecer assertions. M9 agora falha no foco em React e Edge. A lacuna anterior está fechada.

Os kills M1–M8 abaixo **não foram reexecutados** nesta rodada. A equivalência foi conferida por Git: a tree de `sistema/src/main` é `ac68bec559c0dd270790c510bf58b7750f7a49ae`, e a tree de `sistema/src/test` é `309406d2f6d878d053ae64676ba09e8ff3bd354b`, iguais nos dois HEADs. Blobs produção: CriarGerenciamento.jsx `f6dc486a8ed2cf9a487723d5d237e8d0c7346307`, Gerenciamentos.jsx `8e1b338d96c9cd7112da57a2b6ea3ec3601b1b81`, Quadro.jsx `f4e0720f2912f78675fbbfbe8a68302ca7121bb1`, também iguais. T16 somente adicionou testes/helper; os oráculos anteriores de M7/M8 permanecem íntegros. Linhas da tabela histórica referem-se a 70497e7.

| Mutante histórico | Fonte:linha e alteração comportamental | Gate e evidência da rodada 1 | Resultado válido mantido |
| --- | --- | --- | --- |
| M1 | `sistema/src/main/java/com/patp/sistema/service/UsuarioService.java:29`, cadastro atribui ADMINISTRADOR em vez de FUNCIONARIO | `mvn.cmd -B -Dtest=UsuarioIdentityTests test`: 7 testes, 1 falha; `UsuarioIdentityTests.java:30` esperava FUNCIONARIO, recebeu ADMINISTRADOR | KILLED |
| M2 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:136`, `podeAdministrar` retorna sempre true | GerenciamentoApiTests: 48 testes, 5 falhas; `GerenciamentoApiTests.java:139` flag esperada false recebeu true; `:157` 403 recebeu 200 | KILLED |
| M3 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:69`, criação persiste criador null | GerenciamentoApiTests: 48 testes, 3 falhas; `GerenciamentoApiTests.java:41` não encontra `$.criador.id`; criador perde edição em `:337` | KILLED |
| M4 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:163`, comparação de versão desativada com `if(false)` | GerenciamentoApiTests: 48 testes, 4 falhas; `GerenciamentoApiTests.java:217`/`:231` esperavam 409, receberam 200/204 | KILLED |
| M5 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoGuard.java:34`, guarda arquivado desativada | Etapa/Processo/Comentario/HistoricoArchiveTests: 35 testes, 15 falhas; status 409 passa a 200, por exemplo `ComentarioArchiveTests.java:38` | KILLED |
| M6 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoGuard.java:22`, `buscarComLock(id)` trocado por `findById(id)` | EtapaArchiveTests: 6 testes, 2 falhas; `EtapaArchiveTests.java:121` esperava TimeoutException antes de liberar commit, segunda operação terminou sem esperar | KILLED |
| M7 | `frontend/src/pages/Gerenciamentos.jsx:178` e `:184`, condições de permissão substituídas por true | `npm.cmd test`: 65 testes, 4 falhas; `Gerenciamentos.test.jsx:72`/`:73`/`:82` exibem controles proibidos | KILLED |
| M8 | `frontend/src/pages/Gerenciamentos.jsx:8`, aviso de sucesso com GET falho vira `Operação falhou.` | `npm.cmd test`: 65 testes, 3 falhas; `Gerenciamentos.test.jsx:147`, `:205`, `:274` exigem aviso literal GER-34 | KILLED |

| Mutante desta rodada | Fonte:linha e alteração comportamental | Gate e assertion que detectou | Resultado |
| --- | --- | --- | --- |
| M9 | `frontend/src/pages/CriarGerenciamento.jsx:110`, adicionar `tabIndex={-1}` em Remover etapa | `npm.cmd test`: 1 falha/70 pass, `frontend/src/pages/CriarGerenciamento.test.jsx:224` esperava foco em Remover, recebeu outro botão. `npm.cmd run test:e2e`: 1 falha; `frontend/e2e/gerenciamentos.spec.js:13`, `expect(controle).toBeFocused()` recebeu inactive ao atingir Remover etapa 2 | KILLED |
| M10 | `frontend/src/pages/Gerenciamentos.jsx:154`, adicionar `tabIndex={-1}` no retry da lista | `npm.cmd test`: 2 falhas/69 pass, erro inicial e erro após mutação; `frontend/src/test/keyboard.js:7`, `expect(document.activeElement).toBe(controle)`; callers `frontend/src/pages/Gerenciamentos.test.jsx:192` | KILLED |
| M11 | `frontend/src/pages/Quadro.jsx:42`, adicionar `tabIndex={-1}` no retry do quadro | `npm.cmd test`: 1 falha/70 pass; `frontend/src/test/keyboard.js:7` esperava botão de retry, recebeu body; caller `frontend/src/pages/Quadro.test.jsx:69` | KILLED |
| M12 | `frontend/src/pages/CriarGerenciamento.jsx:102`, adicionar `tabIndex={-1}` no campo Nome da etapa | `npm.cmd test`: 1 falha/70 pass; `frontend/src/pages/CriarGerenciamento.test.jsx:216` esperava foco no nome, recebeu setor | KILLED |

**Resultado:** 4/4 mutações desta rodada KILLED, zero SURVIVED; 8/8 kills históricos mantidos pela equivalência de fonte/assertions. Total de 12 alvos atuais com discriminação comprovada. Sensor PASS. Uma tentativa inicial de helper falhou apenas ao imprimir caractere no console CP1252, depois de restaurar a fonte em finally; o helper foi reexecutado com UTF-8 e esse erro auxiliar não entrou nos resultados.

## Tarefas, qualidade e edge cases

Todas as 16 tarefas estão marcadas pelo autor como concluídas, sem checkbox bloqueado. Os commits atômicos previstos estão presentes no diff. A evidência independente permite aceitar os resultados das tarefas abaixo, incluindo a correção T16 de teclado.

| Tarefa | Commit | Conferência independente |
| --- | --- | --- |
| T1 | f8235b5 | PASS: Maven clean + H2 real |
| T2 | 3c0b888 | PASS: identidade/papel/ID/senha |
| T3 | 40dfa37 | PASS: persistência/legado/versão/projeções |
| T4 | f9e45c8 | PASS: contrato API e preservação |
| T5 | 760f0ff | PASS: etapas/arquivo/concorrência |
| T6 | f151b46 | PASS: guardas de processos/vínculos/rollback |
| T7 | 8eec6a0 | PASS: comentário ativo/arquivado |
| T8 | 89bbc04 | PASS: histórico/matriz 12 rotas |
| T14 | 99a692e | PASS: resposta real UTF-8 |
| T9 | 7030f1f | PASS: runner DOM/Edge isolados |
| T10 | b0f13a2 | PASS: HTTP/payload/erro/sem retry |
| T11 | ba84bda | PASS: formulário e remoção por teclado, completada por T16 |
| T12 | 7e0ad22 | PASS: administração/estados/foco e retries por teclado em T16 |
| T15 | 7c158af | PASS: navegação durante recarga/origem atual |
| T13 | aa981e6 | PASS: E2E, readonly e retry do Quadro por teclado em T16 |
| T16 | 811842a | PASS: seis novos casos, M9–M12 detectados e assertions anteriores preservadas |

| Princípio | Conferência |
| --- | --- |
| Escopo e mudanças cirúrgicas | PASS: código funcional restrito ao CRUD, identidade necessária, guardas e testes; alterações de estilo sustentam os controles; sem implementação antecipada dos requisitos de etapas/demandas |
| Simplicidade/padrões | PASS: DTO limita metadados, guarda único coordena escrita, transações nas famílias existentes, componentes React e cliente HTTP seguem estrutura do projeto |
| Abstrações/segredos | PASS: sem generalização desnecessária; senha WRITE_ONLY e DTO de criador; erros internos genéricos; nenhuma credencial de produção lida/impressa |
| Assertions conforme spec | PASS: 41 ACs com outcomes exatos |
| Coverage por camada | PASS: domínio/rotas e todos os controles do CRUD pelo teclado |
| Guidelines documentadas | `.agents/skills/tlc-spec-driven/references/coding-principles.md` e `.specs/features/gerenciamentos/tasks.md`; `docs/OPERACAO.md` para isolamento. Nenhum AGENTS.md adicional encontrado |

Edge cases confirmados: limites UTF-16 e trim JS; nomes iguais independentes; criador nulo sem privilégio/exceção; arquivo vazio e com conteúdo; recusas sem escrita; estado repetido sem versão extra; rede sem sucesso/retry automático; leituras de etapas/processos/comentários/históricos arquivados; legado sem truncar/autoria inferida. GER-15/20 comparam efeitos próprios da operação, sem exigir reversão de escritas independentes.

A semântica ativa preexistente foi preservada. `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:119`–`:121` registra a exclusão ativa preexistente que falha por integridade referencial, mas agora sem histórico parcial; ela não foi apresentada como CRUD de demandas concluído.

## MySQL histórico e UAT

O MySQL não foi reexecutado nesta rodada. `.specs/features/gerenciamentos/mysql-validation.md` é evidência histórica separada, no commit backend `99a692e`. Não houve alteração de fonte backend entre esse commit e o HEAD auditado. Na rodada 1, o verificador leu o resumo existente `%TEMP%/creral-crud-mysql-suite.log`: 96 testes, zero falhas/erros/skips, BUILD SUCCESS. Também leu `legacy-migration-result.json` na pasta histórica identificada pelo relatório: `restartPersistence`, `oldColumnsAfterRestart` e `utf8AuthErrors` são PASS. Isso sustenta o edge de restart/schema legado, sem atribuir ao verificador uma execução MySQL nova nem validar o banco real.

**UAT humano: PENDENTE.** Nenhum usuário confirmou testes de aceitação humana nesta rodada ou no material recebido. O E2E automático não equivale a UAT humano.


## Encerramento e lições

Sem lacunas atuais ou fix tasks adicionais. T16 fecha GER-38 com foco/ação/payload e foi testada independentemente. Recomendações de rastreabilidade, sem edição de spec/tasks pelo verificador: GER-01–41 tecnicamente verificados; conclusão técnica autorizada pelo relatório PASS. UAT humano segue pendente.

L-001 continua candidate, recorrência 1, grounded no M9 sobrevivente da rodada 1. Esta rodada não criou novo sinal de falha e não repetiu lessons.py: a mesma feature não gera segunda recorrência nem promoção artificial. A lição preservada é `Exercite todos os controles do CRUD com Tab, confira o foco e ative por Enter; cliques não comprovam operação por teclado.`

**Completion gate:** `python .agents/skills/tlc-spec-driven/scripts/validate_state.py gerenciamentos --root C:\Users\Marco\Desktop\sistema-grupo`, exit 0 após a escrita do relatório; zero erros em gerenciamentos.
