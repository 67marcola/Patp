# Validation: CRUD de gerenciamentos - FAIL

**Verdict: FAIL.** Os gates passam e 40 dos 41 critérios têm evidência suficiente. GER-38 tem cobertura parcial: o sensor retirou Remover etapa da sequência Tab e tanto Vitest quanto o E2E continuaram passando. A implementação atual usa botão HTML operável; a falha comprovada está na capacidade dos testes de detectar essa regressão.

**Data:** 2026-10-05, America/Sao_Paulo.  
**Spec:** `.specs/features/gerenciamentos/spec.md`.  
**Diff:** `5d8beb9..70497e74f84e4195ba7aa1be517f3d84c28dd46d`, branch `testes`.  
**Verificador:** agente independente fresco, autor diferente do verificador. Auditoria frontend auxiliar somente de leitura; gates, sensor e parecer executados pelo verificador.  
**Rodada:** 1. Não houve correção de fonte ou testes nesta verificação.

## Gates e isolamento

Todos os comandos de execução rodaram numa worktree temporária detached no HEAD commitado. Não foram usados `target`, `node_modules` ou servidores da árvore real. O Maven recebeu `JAVA_HOME=C:\Program Files\Java\jdk-25.0.2` e esse `bin` antes dos shims no PATH. A aplicação normal e o MySQL configurado não foram iniciados ou acessados.

| Gate | Comando, no diretório correspondente da scratch | Resultado |
| --- | --- | --- |
| Backend build | `mvn.cmd -B clean verify`, em `sistema` | Exit 0; 97 testes, zero falhas, erros ou skips; JAR recompilado do zero |
| Dependências frontend | `npm.cmd ci`, em `frontend` | Exit 0; lockfile usado; 98 pacotes instalados; zero vulnerabilidades reportadas |
| Frontend | `npm.cmd test` | Exit 0; 4 arquivos, 65 testes, zero falhas/skips |
| Build frontend | `npm.cmd run build` | Exit 0; 22 módulos; bundle produzido |
| Lint | `npm.cmd run lint` | Exit 0; oxlint em fontes, e2e, scripts e configurações |
| Navegador | `npm.cmd run test:e2e` | Exit 0; 1 E2E Edge real; CRUD completo, reload e igualdade das etapas |
| Spec estrutural | `python .agents/skills/tlc-spec-driven/scripts/validate_spec.py gerenciamentos --root . --strict` | Exit 0; zero erros/avisos |
| Tasks estrutural | `python .agents/skills/tlc-spec-driven/scripts/validate_tasks.py gerenciamentos --root .` | Exit 0; zero erros/avisos |

O E2E usou `frontend/scripts/isolated-system.mjs:79`: H2 em memória explícito e backend na porta 18082; Vite na 4173. `frontend/playwright.config.js:12` impede reutilizar servidor existente. Não houve retry automático do teste. Após o runner, ambas as portas estavam sem listener. O screenshot mobile produzido pelo gate foi inspecionado: cartão e controles legíveis, sem corte horizontal; a assertion de largura está em `frontend/e2e/gerenciamentos.spec.js:66` e `:88`.

Evidências brutas deste run permanecem em `%TEMP%/creral-verifier-gerenciamentos-d76c5bd6bf084be49a693dab55dddfd3/`: `maven-gate.log`, `vitest-gate.log`, `npm-ci.log`, `build-gate.log`, `lint-gate.log`, `e2e-gate.log`, `sensor-results.json` e logs M1–M9. A worktree `head` foi removida com caminho absoluto previamente validado dentro de TEMP. Os processos auxiliares iniciados pelo runner encerraram.

Antes da scratch, o porcelain completo da árvore real tinha 131 linhas. Depois da remoção, antes deste relatório/lição, tinha as mesmas 131 linhas, byte a byte nos arquivos de captura: SHA-256 `A3725C459F445E8A74EBDE8282CDF138D5EEAA8DCEE522C376BC345D964F694C` em `porcelain-before.txt` e `porcelain-after.txt`. Nenhum artefato preexistente da árvore real foi apagado, restaurado ou limpo. As únicas escritas reais do verificador são este relatório e a lição produzida pelo script.

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
| GER-10, consulta de qualquer usuário por ID, arquivado/sem criador | HTTP 200, dados e estado, inclusive legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:142`–`:143` — terceiro recebe 200 + `arquivado=true` + `podeAdministrar=false`; `:141` — criador vazio; `frontend/e2e/gerenciamentos.spec.js:97`/`:98` — 200 + DTO arquivado com ID, nome, descrição, criador, versão 2; `GerenciamentoPersistenceTests.java:50`/`:51` preserva nome legado 180 e criador nulo | PASS |
| GER-11, ID ausente consultado/administrado | HTTP 404, `Gerenciamento não encontrado.` | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:282` e `:285` — `isNotFound()` + `jsonPath("$.erro").value("Gerenciamento não encontrado.")`, GET e PUT editar/arquivar/restaurar | PASS |
| GER-12, edição válida por criador/admin com versão atual | HTTP 200, valores persistidos, nova versão | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:172` — admin 200, nome novo, criador nulo, versão 1; `:186`/`:187` — criador 200, nome Novo, descrição Editada, versão 1; `:203`/`:204` — nome/descrição persistidos; `:215` — edição idêntica também versão 1 | PASS |
| GER-13, terceiro administra | HTTP 403, `Você não tem permissão para administrar este gerenciamento.` | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:157` — `isForbidden()` + `value(PERMISSAO_ERRO)` literal de `:27`, para editar/arquivar/restaurar; `:159`–`:161` nome, estado e versão preservados; `:257` precedência sobre arquivado | PASS |
| GER-14, funcionário administra quadro sem criador | Mesmo HTTP 403 e texto; sem autoria inferida | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:154` inclui dono null; `:157` — `isForbidden()` + mensagem exata; `:159`–`:161` estado inalterado; `:173` — admin não inventa criador | PASS |
| GER-15, editar nome/descrição | Só nome/descrição/versão mudam; preservar criador/estado/conteúdo | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:185` tenta falsificar criador/estado; `:187` mantém `arquivado=false` e criador original; `:188` — `assertThat(conteudoPersistido()).isEqualTo(conteudo)`; helper `sistema/src/test/java/com/patp/sistema/ApiIntegrationSupport.java:78`/`:79` compara todas as colunas das quatro tabelas com `select * ... order by id` | PASS |
| GER-16, versão antiga em mudança aplicável | HTTP 409, `Gerenciamento alterado por outro usuário. Atualize e tente novamente.`; sem sobrescrever | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:217` e `:231` — `isConflict()` + `value(VERSAO_ERRO)` literal de `:29`, editar/arquivar/restaurar; `:218`/`:219` mantém nome/versão; formatos inválidos 400 em `:243` e estado intacto em `:245`/`:246` | PASS |
| GER-17, arquivar atual por criador/admin | HTTP 204, estado arquivado persistido | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:190` — `isNoContent()` + corpo vazio; `:191` — `isArquivado().isTrue()`; admin em quadro alheio/null em `:309`/`:310`; versão 1 em `:311` | PASS |
| GER-18, restaurar atual por criador/admin | HTTP 204, estado ativo persistido | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:197` — `isNoContent()`; `:201` — `isArquivado().isFalse()`; admin alheio/null em `:314`/`:316`, versão 2 em `:317` | PASS |
| GER-19, repetir estado aplicado com versão válida, até antiga | HTTP 204, nenhum efeito em dados/versão | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:194` — arquivar repetido versão 0 retorna 204; `:195` — versão permanece 2; `:199` — restaurar repetido versão 0 retorna 204; `:202` — versão permanece 3; `:203`–`:206` metadados e conteúdo preservados | PASS |
| GER-20, arquivar/restaurar | Preservar IDs, atributos e vínculos de etapas/demandas/comentários/históricos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:312`/`:321` — `conteudoPersistido().isEqualTo(antes)` após cada estado, admin alheio/null; `:206` ciclo criador; helper `sistema/src/test/java/com/patp/sistema/ApiIntegrationSupport.java:78`/`:79` cobre todas as colunas; `frontend/e2e/gerenciamentos.spec.js:113` — etapas finais iguais às iniciais nos campos ID/nome/setor/ordem | PASS |
| GER-21, conteúdo válido em arquivado | Todas as 12 rotas: HTTP 409, `Gerenciamento arquivado. Restaure-o antes de alterar.`, sem gravar | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:259` — quadro; `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:55` — POST/PUT/DELETE etapa; `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43` — seis rotas; `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:38` e `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:38` — comentário/histórico. Todas `isConflict()` + texto exato, snapshots iguais em `:260`, Etapa `:56`, Processo `:44`, Comentário/Histórico `:39`. POST com vínculo falso em Processo `:77`/`:78` também 409 sem gravação | PASS |
| GER-22, mutação disputa com arquivar | Commit conteúdo antes do arquivo ou recusa sem escrita se arquivo primeiro | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:121` — segunda operação bloqueia antes do commit; `:125` — status `arquivoPrimeiro ? 409 : 204`; `:129` — count `arquivoPrimeiro ? 0 : 1`; `:131` nome Concorrente; `:133`/`:134` arquivado e versão 1. Duas ordens coordenadas por latches/transações reais. Guard comum e READ_COMMITTED nas quatro famílias inspecionados | PASS |
| GER-23, sessão ausente/inválida | HTTP 401 em rotas protegidas; sem escrita | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:105` — `isUnauthorized()` para 12 rotas e ambas as sessões; `:106`–`:108` snapshots/metadados intactos; `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:100`/`:102` — GET 401; `sistema/src/test/java/com/patp/sistema/AutenticacaoResponseTests.java:38` — HTTP real 401; `:41`/`:42` — bytes UTF-8 decodificados e textos exatos | PASS |
| GER-24, papel administrativo enviado em cadastro comum | Persistir FUNCIONARIO; sem privilégio por payload | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:28` envia ADMINISTRADOR; `:30` — `jsonPath("$.papel").value("FUNCIONARIO")`; `:34` — SQL papel `isEqualTo("FUNCIONARIO")` | PASS |
| GER-25, cadastro com ID existente/escolhido | HTTP 400, não criar/substituir conta | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:44`/`:56` — `isBadRequest()`; `:45` — count 1; `:47`–`:49` nome/email/hash preservados; `:57` — escolhido count zero | PASS |
| GER-26, seleção administrativa confiável | Só papel persistido pelo servidor concede administração | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:64` promoção SQL fictícia; `:68` — `/me` papel ADMINISTRADOR na sessão anterior; `:78` — legado FUNCIONARIO; `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:146` — `podeAdministrar=true` após promoção, contraposto a `:143`; cadastro falsificado permanece FUNCIONARIO em Usuario `:30`/`:34` | PASS |
| GER-27, respostas diretas/aninhadas do CRUD | Nenhuma senha/hash divulgado | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:31`/`:92` — `$..senha` `doesNotExist()`; `:36`/`:94` — resposta `doesNotContain(hashPersistido)`; `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:46`, `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:47`, `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:43`, `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:44` — senha aninhada ausente; modelo tem único campo de senha WRITE_ONLY | PASS |
| GER-28, abrir lista/alternar/abrir-voltar | Ativos inicialmente; Arquivados selecionável; preservar filtro | `frontend/src/pages/Gerenciamentos.test.jsx:45` — Ativos `aria-pressed=true`; `:46` — URL `?arquivado=false`; `:60` — Arquivados `aria-pressed=true` após voltar; `:61` — Restaurar disponível | PASS |
| GER-29, consultar arquivado | `Arquivado — somente consulta`, nenhum controle de alteração | `frontend/src/pages/Quadro.test.jsx:21` — texto literal `toBe`; `:23`/`:37` — Criar processo `toBeNull()`; `:39` — todas requests GET; `frontend/e2e/gerenciamentos.spec.js:82`/`:85` — texto visível e controle `toHaveCount(0)` em Edge | PASS |
| GER-30, controles administrativos | Editar/Arquivar só ativo administrável; Restaurar só arquivado administrável | `frontend/src/pages/Gerenciamentos.test.jsx:72`/`:73` — presença `toBe(permitido)` na matriz criador/admin/terceiro/funcionário legado; `:74` — Restaurar ausente em ativos; `:82` — Restaurar `toBe(permitido)`; `:83`/`:84` — editar/arquivar ausentes em arquivados; `:225`–`:227` permissão fresca atualiza botões | PASS |
| GER-31, cancelar edição/confirmação | Retornar sem mutação; foco na origem após confirmação | `frontend/src/pages/CriarGerenciamento.test.jsx:117`/`:118` — voltar uma vez e fetch nunca; `frontend/src/pages/Gerenciamentos.test.jsx:96`–`:98` — diálogo ausente, `document.activeElement===origem`, zero PUT; `:288`–`:294` usa origem atual conectada após navegação | PASS |
| GER-32, operação pendente/cliques adicionais | Um único envio da operação | `frontend/src/pages/CriarGerenciamento.test.jsx:126`/`:127` duas submissões; `:128` — fetch uma vez; `:129` botão disabled; `frontend/src/pages/Gerenciamentos.test.jsx:119` double click; `:120` — um PUT; `:121` confirmação disabled. Exercita guardas comuns criar/editar e arquivar/restaurar | PASS |
| GER-33, mutação/ comunicação falha | Erro correspondente, rascunho preservado, sem sucesso/retry criação | `frontend/src/pages/CriarGerenciamento.test.jsx:143` — alerta exato 400/403/409; `:144`/`:145` nome Rascunho e descrição Postes; `:146`/`:147` sem atualizar/voltar; `:154` texto exato de comunicação; `:155`–`:158` rascunho, uma request, zero callbacks; `frontend/src/pages/Gerenciamentos.test.jsx:241`–`:243` cartão mantido e uma tentativa | PASS |
| GER-34, mutação confirmada + recarga falha | `Alteração salva; não foi possível atualizar a lista.`; retry só consulta | `frontend/src/pages/Gerenciamentos.test.jsx:147` — alerta `toBe` literal; `:150` retry; `:151` vazio correto; `:152` um único PUT; `:153` alerta removido; `:205`–`:207` edição; `:270`–`:275` formulário pendente durante recarga | PASS |
| GER-35, confirmação + GET bem sucedido/reload | Exibir valores/estado persistidos após recarregar página | `frontend/e2e/gerenciamentos.spec.js:38`/`:39` reload criação; `:47` — DTO completo exato; `:59`–`:61` reload nome/descrição editados; `:93`/`:98` reload + arquivado versão 2; `:104`/`:108` reload + restaurado versão 3, campos originais mantidos; `:113` igualdade das etapas | PASS |
| GER-36, estados da listagem | Distinguir carregamento/vazio/erro; retry apenas GET | `frontend/src/pages/Gerenciamentos.test.jsx:44` — status Carregando; `:48` — heading vazio; `:49` status ausente; `:160` alerta Consulta indisponível; `:161` vazio ausente no erro; `:164` resultado após retry; `:165` todas requests GET | PASS |
| GER-37, solicitar arquivar | Confirmação nome + preservação + Cancelar/Arquivar | `frontend/src/pages/Gerenciamentos.test.jsx:92` — dialog nome Arquivar Instalações?; `:93` texto preservação; `:94` Cancelar; `:104` Arquivar exato; `frontend/e2e/gerenciamentos.spec.js:72`–`:77` ambos controles acionados por teclado | PASS |
| GER-38, todos os controles do CRUD | Alcançar/operar todos pelo teclado | `frontend/src/pages/CriarGerenciamento.test.jsx:164`, `:166`, `:168`, `:170`, `:172` — foco Voltar/nome/descrição/Cancelar/Salvar; `:173`/`:174` Enter atualiza; `frontend/src/pages/Quadro.test.jsx:24`–`:27` voltar; E2E `frontend/e2e/gerenciamentos.spec.js:13` — `toBeFocused()` antes de Enter nos controles principais. **Faltam** Remover etapa e retries: Form `:95`, Lista `:150`/`:163` e Quadro `:55` só `user.click`; M9 sobreviveu à suíte e ao E2E | GAP |
| GER-39, campos do CRUD | Rótulos identificáveis por tecnologia assistiva | `frontend/src/pages/CriarGerenciamento.test.jsx:51`/`:52` — `getByLabelText` nome/descrição; `:93`/`:94` — nome/setor etapa; `:166`/`:168` — foco nos campos identificados; `frontend/e2e/gerenciamentos.spec.js:31`–`:35` — todos resolvidos por `getByLabel` | PASS |
| GER-40, exibir erro | Região acessível de alerta com erro correspondente | `frontend/src/pages/CriarGerenciamento.test.jsx:143`, `frontend/src/pages/Gerenciamentos.test.jsx:132`, `:147`, `:160`, `:241`, `frontend/src/pages/Quadro.test.jsx:52` — `findByRole("alert")` + `textContent.toBe(mensagemExata)` | PASS |
| GER-41, arquivar/restaurar confirmado | Remover cartão do filtro anterior, manter filtro e vazio se último | `frontend/src/pages/Gerenciamentos.test.jsx:105`/`:109` — Nenhum gerenciamento ativo/arquivado exatos; `:106`/`:110` — filtro atual `aria-pressed=true`; `frontend/e2e/gerenciamentos.spec.js:78`/`:79` e `:100`/`:101` — mesmos outcomes reais | PASS |

**Saldo:** 40 PASS, 1 GAP (GER-38), zero lacunas de precisão da spec. A cobertura parcial do teclado não foi convertida em PASS pela presença de botões nativos.

## Payloads, conjunction e integridade dos testes

| Contrato | Evidência exata |
| --- | --- |
| Criação | `frontend/src/pages/CriarGerenciamento.test.jsx:45` — body `toEqual({ nome: "Instalações", descricao: "", etapas: [] })`; server `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:39`–`:49` confere nome, descrição, criador, estado, versão e persistência juntos |
| Edição | `frontend/src/pages/CriarGerenciamento.test.jsx:58`/`:59` — body `{ nome: "Corrigido", descricao: "Postes", versao: 2 }` e método PUT; `frontend/src/pages/Gerenciamentos.test.jsx:192` — versão fresca 7; servidor mantém identidade/estado/snapshot em GerenciamentoApi `:187`/`:188` |
| Etapas iniciais | `frontend/src/pages/CriarGerenciamento.test.jsx:110` — array exato com nome Planejamento, setor Técnico e ordem 1; `:198` strings de 255 sem truncar; servidor GerenciamentoApi `:373`–`:376` confere campos e vínculo persistidos |
| Arquivar/restaurar | `frontend/src/services/api.test.js:62`–`:65` — sucesso 204 sem JSON, rota correspondente, PUT e body `{ versao: 2 }`; server GerenciamentoApi `:309`–`:321` verifica status, versão, estado, criador e snapshot |
| Rede | `frontend/src/services/api.test.js:84`–`:87` — erro exato, status 0 e uma tentativa; formulário não anuncia sucesso nem chama retorno em Form `:154`–`:158` |

Não há dependência exclusiva de `toHaveBeenCalled` para resultados de domínio ou payload. O servidor usa MockMvc e JDBC reais em H2. A falha parcial usa constraint real e compara zero pai/filhos após rollback. A disputa usa duas transações, latches, ordem de commit e resultado persistido, não um sleep como único oráculo. O helper de snapshot compara IDs, timestamps, valores e FKs, não só contagens. M6 confirma empiricamente que os testes distinguem o lock do pai.

No commit base havia 1 teste Java de contexto e nenhum teste frontend/E2E, conforme `git ls-tree` e fonte; o base não foi executado para evitar sua configuração de banco normal. O teste existente foi preservado e fortalecido com assertion de URL H2 em `sistema/src/test/java/com/patp/sistema/SistemaApplicationTests.java:22`. Totais atuais: 97 Java (+96), 65 Vitest (+65), 1 E2E (+1). Nenhum teste foi removido/enfraquecido; busca no diff/fontes não encontrou `.skip`, `.only`, `.todo`, `@Disabled` ou `SPEC_DEVIATION`.

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
| CriarGerenciamento.test.jsx, 20 | GER-03/04/06/07/31/32/33/38/39/40; payload, limites, cancelamento, envio pendente, erros e rótulos |
| Gerenciamentos.test.jsx, 25 | GER-28/30/31/32/33/34/36/37/38/40/41; filtros, matriz de permissões, confirmação, foco, navegação/recarga e respostas antigas |
| Quadro.test.jsx, 5 | GER-29/38/40; readonly, dados/consulta, teclado voltar e 500/404/rede |
| api.test.js, 15 | GER-08/09/10/11/16/17/18/23/33/34/35; T10 methods/routes/token/body/204/erro/status/sem retry |
| gerenciamentos.spec.js, 1 | GER-01/02/06/17/18/20/28/29/31/35/37/38/41 e T13; navegador real, reload, teclado, preservação e overflow |

Todos os testes do diff têm uma âncora de AC, contrato, edge case ou Done when. As camadas de domínio e rotas têm resultados positivos, limites, recusas e persistência; a única lacuna per-layer encontrada está nos controles secundários de teclado.

## Sensor de discriminação

Profundidade expandida para auth, integridade e concorrência: seis mutações backend e três frontend, uma de cada vez na scratch, fonte original restaurada byte a byte em `finally`. Nenhuma falha de compilação foi contada como kill. Antes de remover a scratch, `git diff --quiet HEAD -- sistema/src/main frontend/src` confirmou todas as fontes restauradas. Um preflight de M9 sem substituição foi descartado e não contado; a execução abaixo usou o patch correto.

| Mutante | Fonte:linha e alteração comportamental | Gate e evidência da discriminação | Resultado |
| --- | --- | --- | --- |
| M1 | `sistema/src/main/java/com/patp/sistema/service/UsuarioService.java:29`, cadastro atribui ADMINISTRADOR em vez de FUNCIONARIO | `mvn.cmd -B -Dtest=UsuarioIdentityTests test`: 7 testes, 1 falha; `UsuarioIdentityTests.java:30` esperava FUNCIONARIO, recebeu ADMINISTRADOR | KILLED |
| M2 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:136`, `podeAdministrar` retorna sempre true | GerenciamentoApiTests: 48 testes, 5 falhas; `GerenciamentoApiTests.java:139` flag esperada false recebeu true; `:157` 403 recebeu 200 | KILLED |
| M3 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:69`, criação persiste criador null | GerenciamentoApiTests: 48 testes, 3 falhas; `GerenciamentoApiTests.java:41` não encontra `$.criador.id`; criador perde edição em `:337` | KILLED |
| M4 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:163`, comparação de versão desativada com `if(false)` | GerenciamentoApiTests: 48 testes, 4 falhas; `GerenciamentoApiTests.java:217`/`:231` esperavam 409, receberam 200/204 | KILLED |
| M5 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoGuard.java:34`, guarda arquivado desativada | Etapa/Processo/Comentario/HistoricoArchiveTests: 35 testes, 15 falhas; status 409 passa a 200, por exemplo `ComentarioArchiveTests.java:38` | KILLED |
| M6 | `sistema/src/main/java/com/patp/sistema/service/GerenciamentoGuard.java:22`, `buscarComLock(id)` trocado por `findById(id)` | EtapaArchiveTests: 6 testes, 2 falhas; `EtapaArchiveTests.java:121` esperava TimeoutException antes de liberar commit, segunda operação terminou sem esperar | KILLED |
| M7 | `frontend/src/pages/Gerenciamentos.jsx:178` e `:184`, condições de permissão substituídas por true | `npm.cmd test`: 65 testes, 4 falhas; `Gerenciamentos.test.jsx:72`/`:73`/`:82` exibem controles proibidos | KILLED |
| M8 | `frontend/src/pages/Gerenciamentos.jsx:8`, aviso de sucesso com GET falho vira `Operação falhou.` | `npm.cmd test`: 65 testes, 3 falhas; `Gerenciamentos.test.jsx:147`, `:205`, `:274` exigem aviso literal GER-34 | KILLED |
| M9 | `frontend/src/pages/CriarGerenciamento.jsx:110`, adicionar `tabIndex={-1}` ao botão Remover etapa | `npm.cmd test`: exit 0, 65/65; `npm.cmd run test:e2e`: exit 0, 1/1 Edge real. Botão sai da navegação Tab; `CriarGerenciamento.test.jsx:95` continua clicando e E2E não visita esse controle | **SURVIVED** |

**Resultado:** 9 mutações válidas, 8 KILLED, 1 SURVIVED. Sensor FAIL. O sobrevivente não prova defeito na versão original; prova ausência de regressão detectável para parte de GER-38.

## Tarefas, qualidade e edge cases

Todas as 15 tarefas estão marcadas pelo autor como concluídas, sem checkbox bloqueado. Os commits atômicos previstos estão presentes no diff. A evidência independente permite aceitar os resultados das tarefas abaixo, com ressalva de teclado onde aplicável.

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
| T11 | ba84bda | PARCIAL: controles básicos cobertos; remover etapa sem evidência de teclado |
| T12 | 7e0ad22 | PARCIAL: administração/estados/foco cobertos; repetir consulta sem evidência de teclado |
| T15 | 7c158af | PASS: navegação durante recarga/origem atual |
| T13 | aa981e6 | PARCIAL: E2E e readonly passam; retry do Quadro sem evidência de teclado |

| Princípio | Conferência |
| --- | --- |
| Escopo e mudanças cirúrgicas | PASS: código funcional restrito ao CRUD, identidade necessária, guardas e testes; alterações de estilo sustentam os controles; sem implementação antecipada dos requisitos de etapas/demandas |
| Simplicidade/padrões | PASS: DTO limita metadados, guarda único coordena escrita, transações nas famílias existentes, componentes React e cliente HTTP seguem estrutura do projeto |
| Abstrações/segredos | PASS: sem generalização desnecessária; senha WRITE_ONLY e DTO de criador; erros internos genéricos; nenhuma credencial de produção lida/impressa |
| Assertions conforme spec | PASS para 40 ACs; GER-38 GAP, sem tolerar outcome aproximado |
| Coverage por camada | PARCIAL: domínio/rotas completos; controles secundários teclado precisam teste |
| Guidelines documentadas | `.agents/skills/tlc-spec-driven/references/coding-principles.md` e `.specs/features/gerenciamentos/tasks.md`; `docs/OPERACAO.md` para isolamento. Nenhum AGENTS.md adicional encontrado |

Edge cases confirmados: limites UTF-16 e trim JS; nomes iguais independentes; criador nulo sem privilégio/exceção; arquivo vazio e com conteúdo; recusas sem escrita; estado repetido sem versão extra; rede sem sucesso/retry automático; leituras de etapas/processos/comentários/históricos arquivados; legado sem truncar/autoria inferida. GER-15/20 comparam efeitos próprios da operação, sem exigir reversão de escritas independentes.

A semântica ativa preexistente foi preservada. `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:119`–`:121` registra a exclusão ativa preexistente que falha por integridade referencial, mas agora sem histórico parcial; ela não foi apresentada como CRUD de demandas concluído.

## MySQL histórico e UAT

O MySQL não foi reexecutado nesta rodada. `.specs/features/gerenciamentos/mysql-validation.md` é evidência histórica separada, no commit backend `99a692e`. Não houve alteração de fonte backend entre esse commit e o HEAD auditado. O verificador leu o resumo existente `%TEMP%/creral-crud-mysql-suite.log`: 96 testes, zero falhas/erros/skips, BUILD SUCCESS. Também leu `legacy-migration-result.json` na pasta histórica identificada pelo relatório: `restartPersistence`, `oldColumnsAfterRestart` e `utf8AuthErrors` são PASS. Isso sustenta o edge de restart/schema legado, sem atribuir ao verificador uma execução MySQL nova nem validar o banco real.

**UAT humano: PENDENTE.** Nenhum usuário confirmou testes de aceitação humana nesta rodada ou no material recebido. O E2E automático não equivale a UAT humano.

## Lacuna priorizada e tarefa de correção

1. **Major de verificação, GER-38/M9:** os controles secundários não são visitados por Tab/Enter. Fonte: `frontend/src/pages/CriarGerenciamento.jsx:110` (Remover etapa), `frontend/src/pages/Gerenciamentos.jsx:154` (Tentar atualizar), `frontend/src/pages/Quadro.jsx:42` (Tentar novamente). Evidências atuais somente por clique: Form `:95`, Lista `:150`/`:163`, Quadro `:55`. M9 sobrevive a todas as suítes frontend existentes.

**Fix task proposto:** adicionar testes de comportamento para alcançar cada controle faltante pela sequência Tab, exigir foco no elemento certo e ativar por Enter. Para remover, conferir desaparecimento da etapa e payload sem ela. Para retry, conferir novo GET, atualização da tela e zero mutações adicionais. Manter todas as assertions atuais. Where: os três arquivos `.test.jsx` e E2E somente se necessário. Done when: suites passam na fonte original, M9 falha por assertion de foco/ação e mutantes equivalentes de tabIndex nos dois retries também são detectados. Não modificar funcionalidade original para acomodar testes. Reexecutar gates em HEAD novo isolado e renovar o relatório independente.

Nenhuma correção foi feita pelo verificador. Recomendações de rastreabilidade, sem edição de `spec.md`/`tasks.md`: GER-01–37 e GER-39–41 podem ser marcados verificados; GER-38 precisa fix e re-verificação; a feature permanece incompleta. O ciclo de correção/re-verificação está na primeira de até três rodadas.

**Completion gate:** `python .agents/skills/tlc-spec-driven/scripts/validate_state.py gerenciamentos --root C:\Users\Marco\Desktop\sistema-grupo`, exit 1, recusa o veredito FAIL e impede declarar a feature concluída. A primeira tentativa exigiu adequar o título ao marcador `Validation` reconhecido pelo parser; o veredito permaneceu FAIL.  
**Lição:** `lessons.py add --feature gerenciamentos --signal surviving_mutant --source M9 --scope frontend` criou L-001, candidate, recorrência 1. Texto: `Exercite todos os controles do CRUD com Tab, confira o foco e ative por Enter; cliques não comprovam operação por teclado.` M9 e a cobertura parcial GER-38 têm a mesma causa, registrada uma única vez.
