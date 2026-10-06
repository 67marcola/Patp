# Validation: etapas-finais — PASS

**Veredicto:** PASS automatizado. Os 30/30 critérios têm evidência do resultado exigido pela spec. Os gates completos passaram. O sensor conclusivo executou controle original e 7/7 mutações compiladas somente em RAM, detectadas por assertions. Todas as JVMs próprias encerraram, descartando o estado mutante; depois disso a comparação completa dos bytes de porcelain foi idêntica. UAT humano permanece pendente. A tentativa anterior de descarte de uma scratch em disco foi rejeitada automaticamente e está preservada como histórico, sem contorno.

**Data:** 2026-10-06. **Spec:** `.specs/features/etapas-finais/spec.md`.
**Diff range:** `a118606..d1f5e47`. **HEAD verificado:** `d1f5e47`.
**Verificador:** subagente fresco e independente; não foi autor de implementação ou testes.
**Escopo:** colunas oficiais, CRUD de etapas, apresentação e preparação explícita de legados. Operações de demandas, Criar processo e AD-017/018 ficam fora deste corte. Este relatório não afirma coerência permanente entre status e etapa. UAT humano permanece pendente.

## Task Completion

| Tarefa | Commit | Resultado |
| --- | --- | --- |
| T1 | 137d07b | Concluída: categoria/DTO e compatibilidade nullable |
| T2 | d160387 | Concluída: par, repetição, ordenação e duplicidade |
| T8 | a06550f | Concluída: VARCHAR(20) nullable, H2 e MySQL físico |
| T3 | 3c1791e | Concluída: criação atômica e entrada recusada |
| T4 | 0c846ae | Concluída: proteção/intervalos/snapshot/versão |
| T5 | efcdbdf | Concluída: plano, aplicação, CLI e preservação |
| T6 | 0cf7f29 | Concluída: interface e payload de trabalhos |
| T7 | d1f5e47 | Concluída: três E2E e documentação operacional |

Oito commits de implementação/teste separados. T8 registra em tasks.md sua formalização administrativa posterior ao commit corretivo, sem reescrever histórico; a correção, os testes e a evidência pertencem a a06550f. Nenhum código, teste, STATE ou commit foi alterado pelo Verificador.

## Spec-Anchored Acceptance Criteria

Cada PASS abaixo exige o resultado definido na spec, não apenas uma chamada ou presença de teste. Os caminhos Java começam em `sistema/src/test/java/com/patp/sistema/`. As assertions de snapshot SQL usam todas as colunas e os registros, ordenados por ID; `ApiIntegrationSupport.java:76` mostra a composição das tabelas.

| AC | Resultado exigido pela spec | Evidência file:line e assertion exata | Resultado |
| --- | --- | --- | --- |
| FIN-01 | Sem trabalhos: exatamente CONCLUIDA/Concluídos e CANCELADA/Cancelados, IDs distintos, Setor null | `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java:32` — `assertThat(sem).hasSize(2)`; linhas39–43: nomes/categorias `containsExactly`, setores `containsExactly(null,null)`, IDs `isNotNull().isNotEqualTo`. `frontend/e2e/autocadastro.spec.js:92` — `expect(etapasIniciais).toEqual(finaisEsperadas)` com nomes, IDs, ordem, categoria, setor e quantidade0. | PASS |
| FIN-02 | Preservar N trabalhos recebidos e acrescentar somente duas finais próprias | `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java:33` — `hasSize(4)`; linhas34–37 `containsExactly("Concluídos","Cancelados")`, setores Campo/Projeto, ordens3/7, categorias TRABALHO/TRABALHO; linhas39–43 verificam o par/vínculo. `frontend/e2e/gerenciamentos.spec.js:70` — `toEqual` dos três objetos com todos os campos. | PASS |
| FIN-03 | IDs das finais distintos entre quadros, vínculo ao respectivo quadro | `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java:43` — `containsExactly` dos IDs do quadro; linha45 `doesNotContainAnyElementsOf` entre conjuntos. `EtapasFinaisServiceTests.java:47`–49 repetem vínculo e separação inclusive quadro sem criador. | PASS |
| FIN-04 | Categoria final ou ID existente inicial: 400, zero salvamentos | `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java:53` cobre CONCLUIDA, CANCELADA e ID realmente existente; linha62 `status().isBadRequest()`; linhas63–64 `conteudoPersistido().isEqualTo(antes)` e tabela gerenciamentos igual. | PASS |
| FIN-05 | Falha em etapa reverte quadro e todas as etapas | `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java:69` impõe falha na segunda final após trabalho/primeira final; linha73 exige500/erro e linhas74–75 `quadros.count().isZero()`, `etapas.count().isZero()`. A anotação transacional cobre todas as gravações em `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:48`. | PASS |
| FIN-06 | Categoria SQLnull/homônimo retorna TRABALHO sem gravação/inferência | `sistema/src/test/java/com/patp/sistema/EtapaCategoriaTests.java:52` — categoriaTRABALHO; linhas48–53 preservam ID/nome/Setor/ordem/contagem; linhas54–55 snapshot igual e colunaSQL `isNull()`. `EtapasFinaisServiceTests.java:72` preserva os três trabalhos por todas as colunas. | PASS |
| FIN-07 | Snapshot retorna categoria, IDs, campos e contagens reais | `sistema/src/test/java/com/patp/sistema/EtapaCategoriaTests.java:48`–53 exige ID/nome/Setor/ordem/categoria/contagem1. `EtapaFinaisApiTests.java:72`–75 exige IDs finais e contagens2/1 após demandas reais; categorias finais em61/63. `frontend/e2e/etapas.spec.js:100` compara objetos completos e reload em170. | PASS |
| FIN-08 | Trabalhos por ordem/ID antes de CONCLUIDA e CANCELADA | `sistema/src/test/java/com/patp/sistema/EtapasFinaisServiceTests.java:69` — IDs `containsExactly(b,c,a)` com empate/lacuna, mesmo finais com ordem0; linhas70–71 categorias TRABALHO×3,CONCLUIDA,CANCELADA. `frontend/e2e/etapas.spec.js:168` exige a ordem visual completa. | PASS |
| FIN-09 | Criador/admin editam final:409, erro exato, nenhum registro/versão muda | `sistema/src/test/java/com/patp/sistema/EtapaFinaisApiTests.java:34` parametriza criador/admin; linha44 `isConflict()` e `$.erro.value("Etapas finais obrigatórias não podem ser alteradas.")`; percorre as duas finais, linha48 `estado().isEqualTo(antes)`, incluindo gerenciamentos/versão. | PASS |
| FIN-10 | Remover final vazia:409, mesmo erro, zero alteração/versão | `sistema/src/test/java/com/patp/sistema/EtapaFinaisApiTests.java:45`–48 — DELETE das duas finais sem demandas, criador/admin, conflito/erro exato e estado integral igual. | PASS |
| FIN-11 | CRUD trabalho mantém par após trabalhos, mesmos IDs/campos | `sistema/src/test/java/com/patp/sistema/EtapaFinaisApiTests.java:69`–86 cobre criar/editar/remover até zero trabalhos; IDs/contagens em72–75, snapshot SQL das finais `isEqualTo(finaisAntes)` em84 e categorias exatas em86. `frontend/e2e/etapas.spec.js:138`,147,160,184,196,226 comparam trabalhos seguidos do mesmo `finaisEsperadas`; reload170 e258 preserva objetos. | PASS |
| FIN-12 | Criação aceita1..N+1; edição1..N; finais não contam | `sistema/src/test/java/com/patp/sistema/EtapaApiTests.java:355`–363 parametriza criação1/2/3 com N2 e exige posição/ordens; `EtapaFinaisApiTests.java:70` aceitaN+1 com par existente e linha78 aceitaN na edição. `frontend/e2e/etapas.spec.js:78` exige opções de criação N+1, linha176 opções de edição1/2/3. | PASS |
| FIN-13 | Posição invadindo finais ou Setor vazio:400, estado/versão intactos | `sistema/src/test/java/com/patp/sistema/EtapaFinaisApiTests.java:90` cobre criar/editar posição final e Setor branco; linha101 `isBadRequest()` e erro exato de posição/Setor; linha102 snapshot igual incluindo versões. | PASS |
| FIN-14 | Homônimos continuam trabalho, CRUD/permissões/validações anteriores | `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java:34`–37 exige homônimosTRABALHO com campos recebidos. `EtapaFinaisApiTests.java:58`–86 edita/remove Concluídos/Cancelados como trabalho. `frontend/src/pages/QuadroEtapas.test.jsx:498`–505 exige ausência de indicação final, Setor/posição/Remover/editor. Guardas comuns são preservadas em `EtapaService.java:114`–118 e testesFIN-19. | PASS |
| FIN-15 | Finais oficiais com indicação e sem Setor/Editar/Remover/posição configurável | `frontend/src/pages/QuadroEtapas.test.jsx:47`–53 exige ID real, texto exato, ausência de Setor/posição/botões e contagens3/1; helper chamado em445/470 inclusive vazias. `frontend/e2e/etapas.spec.js:104`–109 exige ID, indicação, quantidade0 e ausência dos controles no navegador, inclusive reload/arquivo. | PASS |
| FIN-16 | Editor usa apenas trabalhos, Nome/Setor obrigatórios | `frontend/src/pages/QuadroEtapas.test.jsx:455`–462 exige posição1, mensagens exatas de Nome/Setor e só GET; linhas478/482 exigem opções1/2/3 criação e1/2 edição com finais. `frontend/e2e/etapas.spec.js:78` e176 conferem opções reais. | PASS |
| FIN-17 | Só finais mostra duas colunas e “Nenhuma etapa de trabalho cadastrada” | `frontend/src/pages/QuadroEtapas.test.jsx:441`–445 — texto exato, lista `toEqual(["Concluídos","Cancelados"])`, helper final completo. `frontend/e2e/etapas.spec.js:121`–123 e `gerenciamentos.spec.js:157`–171 verificam criação e remoção/reload. | PASS |
| FIN-18 | Formulário informa finais automáticas e envia somente trabalhos | `frontend/src/pages/CriarGerenciamento.test.jsx:57`–58 exige explicação; linhas69–72 `JSON.parse(body).toEqual` somente trabalhos homônimos nome/Setor/ordem, sem IDs/categoria finais. Linha48 exige `etapas:[]` sem trabalhos. `frontend/e2e/autocadastro.spec.js:70`–72 verifica payload real vazio. | PASS |
| FIN-19 | Arquivo409, conta sem administração403, versão obsoleta409, zero mutação | `sistema/src/test/java/com/patp/sistema/EtapaApiTests.java:228`–229 exige403/estado igual paraPOST/PUT/DELETE; linhas246–247 exige409 arquivo criador/admin; linhas387–388 exige409 versão antiga/estado igual. `frontend/src/pages/QuadroEtapas.test.jsx:107`–116 controla permissões/arquivo; linhas543–548 impedem reenvio ao final após refresh. `frontend/e2e/etapas.spec.js:241`–258 exige arquivo só consulta e dados iguais. | PASS |
| FIN-20 | Plano inventaria todos quadros/finais ausentes/status sem qualquer escrita | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:119` exige todos IDs na ordem; linhas120–136 zeram efeitos e verificam nome/arquivo/criador/ausências/status exatos/refs; linha137 `estado().isEqualTo(antes)` nas seis tabelas. MySQL `legacy-check.py:49` exige snapshot integral igual. | PASS |
| FIN-21 | Aplicação cria ausentes em ativos/arquivados/sem criador | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:91`–108 monta três perfis+parcial; linhas152/154 exige7 criações com distribuição2/2/2/1/0/0, linhas181–184 exige par/nomes/Setor/IDs distintos por quadro. Quadro vazio sem criador linhas192–198. MySQL `legacy-check.py:65`–66 exige par em cada um dos três quadros. | PASS |
| FIN-22 | Somente status exatos Concluido/Cancelado trocam referência para final do mesmo quadro | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:153` exige8 movimentos; linhas169–172 exige categoria correspondente e quadro igual, outros etapaID igual. Linha156 compara todos campos exceto etapa_id. MySQL `legacy-check.py:68`–74 compara campos, destino e quadro. | PASS |
| FIN-23 | Preservar IDs/status/campos/datas/motivos e comentários/históricos | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:156` — `semColuna("processos","etapa_id").isEqualTo(dadosDemandas)`; linhas162–164 comentários/históricos/usuários inteiros iguais. Fixture57–79 inclui datas/motivos ausentes e contraditórios. MySQL `legacy-check.py:57` e68 exige mesmos registros/colunas. | PASS |
| FIN-24 | Preservar campos/IDs/ordem/Setor trabalhos e estado/nome/descrição/criador quadros | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:157` — quadro integral exceto versão igual; linhas158–160 cada etapa prévia `select *` igual, inclusive homônimos/ordens9/9/20/categoriaSQLnull. MySQL `legacy-check.py:59` e63 compara todos campos antigos, incluindo Setornull. | PASS |
| FIN-25 | Status null/desconhecidos: etapa preservada, inventário exato, nenhuma data/motivo criado | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:128`–130 exige os oito valores exatos, incluindo null, lowercase/acento/espaço; linhas156/172 exige campos/datas/motivos iguais e etapaID igual para desconhecidos. MySQL `legacy-check.py:68`/74 e inventário CLI UTF8 confirmam null/acento. | PASS |
| FIN-26 | Repetição:zero criações/movimentos, IDs/dados/versões intactos | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:205`–213 — efeitos0, ausências/refs0 por quadro, snapshot seis tabelas inteiro igual. `EtapasFinaisServiceTests.java:53` confirma repetição par. MySQL `legacy-check.py:80` exige snapshot pós-repetição idêntico mesmo forçandovalidate contra create-drop. | PASS |
| FIN-27 | Duplicidade ou falha:erro explícito e rollback de toda execução | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:217`–228 cobre ambas categorias duplicadas no último quadro, mensagem exata e estado igual. Linhas236–240 impõem falha na última final, exige erro com falha_preparacao e estado integral igual após alterações em quadro anterior. `EtapasFinaisServiceTests.java:101`–103 cobre garantia do par. | PASS |
| FIN-28 | Quadro com estrutura/ref alterada incrementa versão exatamente uma vez | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:177` — `versoesAntes.get(i)+(i==5?0:1)`; linha178 versão do relatório igual à persistida; fixture tem estrutura+refs, sóestrutura, sórefs e sem mudança. Vazio linha196 exige1; repetição213 snapshot igual. MySQL `legacy-check.py:60` exige antiga+1. | PASS |
| FIN-29 | GET/startup normal não prepara/não altera legados | `sistema/src/test/java/com/patp/sistema/EtapaFinaisApiTests.java:110`–114 — duas consultas continuam com único trabalho e estado igual; `EtapaCategoriaTests.java:54`–55 mantémSQLnull. `PreparacaoEtapasFinaisTests.java:258`–268 abre contexto normal, exige estado igual, e CLIplano também igual. MySQL `legacy-check.py:39`–43 exige todos campos antigos iguais/sócoluna nullable nova e quatro trabalhos. | PASS |
| FIN-30 | Ação inválida recusa antes de contexto/DB e informa plano/aplicar | `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java:276`–280 — ausência, invalid, PLANO e URLinválida exigem `IllegalArgumentException` com mensagem exata “Informe a ação plano ou aplicar como primeiro argumento.”. `sistema/src/main/java/com/patp/sistema/PrepararEtapasFinais.java:14`–17 posiciona validação antes de SpringApplication. CLI MySQLinválida exit1 sem inicialização/stdout. | PASS |

**Resultado funcional:** 30/30 resultados correspondem à spec. Nenhuma lacuna de precisão encontrada. Pontos de fonte adicional `EtapasFinaisServiceTests.java`, `EtapaFinaisApiTests.java`, `PreparacaoEtapasFinaisTests.java` e `EtapaApiTests.java` na tabela acima estão no mesmo diretório Java explicitado.

## Payloads, conjunções e regressões

Criação confere nome/categoria/IDs/Setor/vínculo junto à cardinalidade. Snapshot confere campos e quantidade do banco, incluindo finais ocupadas, não só presença da chamada. Rejeições confere status/erro e comparação integral de registros/versão. Preparação confere simultaneamente destino do mesmo quadro, igualdade de todas as demais colunas e filhos, contagens e versão. O helper `PreparacaoEtapasFinaisTests.java:49` retira somente a coluna explicitamente permitida, etapa_id ou versao.

Payloads de interface são comparados por conteúdo: `frontend/src/pages/CriarGerenciamento.test.jsx:69` e `frontend/src/pages/QuadroEtapas.test.jsx:87`/432 comparam URL, método e corpo completo. E2E `frontend/e2e/autocadastro.spec.js:70` e `gerenciamentos.spec.js:45` conferem os payloads de criação.

Baseline247 Java →278 (+31); baseline222 Vitest →235 (+13); três cenários E2E preservados. Auditoria do diff e comparação de métodos/títulos anteriores não encontrou exclusão de cenário. Alterações em asserts anteriores incluem somente finais oficiais/categoria, contagens decorrentes e mensagem agora específica de trabalhos. Assertions de trabalhos, IDs, campos, rollback, permissões, arquivo, concorrência, versão, foco, erro e ausência de reenvio permanecem. Não há @Disabled/test.skip/test.fixme ou SPEC_DEVIATION no corte.

### Reverse mapping dos testes novos e alterados

| Superfície de testes | Critérios/edges e propósito |
| --- | --- |
| EtapaCategoriaTests,2 casos | FIN-06/07; T8: enum string e metadataVARCHAR20nullable |
| EtapasFinaisServiceTests,4 casos | FIN-01/03/08/11/14/26/27: par/quadros/repetição; homônimos/lacunas/empates; final ausente; duplicidade |
| GerenciamentoFinaisTests,5 casos | FIN-01/02/03 com/sem trabalho, FIN-04 três entradas recusadas, FIN-05 falha parcial |
| EtapaFinaisApiTests,8 casos | FIN-09/10 duas permissões; FIN-07/11/12/14 ciclo/contagens/homônimos; FIN-13 quatro inválidos; FIN-06/29 GET legado |
| PreparacaoEtapasFinaisTests,12 casos | FIN-20 plano; FIN-21–25/28 aplicação integral; FIN-21/28 vazio; FIN-26 repetição; FIN-27 ambas duplicidades/falha; FIN-29 startup/CLI; FIN-30 quatro ações inválidas |
| QuadroEtapas.test.jsx,+12 casos | FIN-15/17/19 três perfis sófinais; FIN-16/17 editorvazio; FIN-15 vazias; FIN-16 intervalos; FIN-14/15/16 homônimos; FIN-15/29 legados; FIN-15/19 quatro alvos em cache |
| CriarGerenciamento.test.jsx,+1 caso | FIN-18 explicação/payload somente trabalhos/homônimos; demais assertions existentes mantidas |
| EtapaApiTests,EtapaArchiveTests,EtapaDemandConcurrencyTests,GerenciamentoApiTests, casos existentes | FIN-01/02/03/07/08/11/12/19/29 e preservação dos contratos anteriores; ajustes de listas conferem finais exatas |
| Três E2E existentes | FIN-01/02/03/07/08/11/15/16/17/18/19/29: criação/CRUD/teclado/reload/arquivo/finais reais, com toda regressão de cadastro/login/quadro preservada |

Não há teste novo sem associação a AC, edge ou Donewhen. Testes antigos fora do diff integram o gate de regressão, sem redefinir o escopo das demandas.

## Gate Check

Gates executados independentemente pelo Verificador, com Java25.0.2, Maven3.9.16, H2 de teste e Edge. Nenhum Maven concorrente no target. Build escreveu apenas em TEMP novo; frontend/dist preexistente foi preservado.

| Comando | Resultado exato | Evidência |
| --- | --- | --- |
| sistema: mvn.cmd -B verify | exit0;278 testes,0 failures,0 errors,0 skipped; BUILD SUCCESS | TEMP audit/java-verify.log:328 e337 |
| frontend: npm.cmd test | exit0;11 arquivos,235 testes aprovados | TEMP audit/vitest.log |
| npm.cmd run build -- --outDir TEMP/audit/build | exit0 | TEMP audit/build.log |
| npm.cmd run lint | exit0 | TEMP audit/lint.log |
| npm.cmd run test:e2e | exit0;3/3 Edge/H2,0 skips, retries0 da configuração | TEMP audit/e2e.log:15,29,43; frontend/playwright.config.js:8 |

**TEMP audit:** `C:/Users/Marco/AppData/Local/Temp/etapas-finais-verifier-47a7e6f8d5fc4be6bfd27345677d01c9`. Logs completos preservados. Os três E2E usaram os helpers próprios, portas18082/4173, H2 efêmero e dados fictícios. Portas8081/5173 e banco/configuração normal não foram operados.

### MySQL físico já executado e encerrado pelo root

Evidência revisada sem reabrir MySQL: `C:/Users/Marco/AppData/Local/Temp/creral-etapas-finais-3b48b05a57314180a8f952ea6fa43d07/mysql-evidence.md`, mysql-selected-verify.log:792/801, legacy-check.py e os cinco snapshotsJSON. MySQL8.0.43:30 testes selecionados aprovados,0 failures/errors/skips; coluna físicaVARCHAR20nullable. Schema antigo tinha17 registros em6 tabelas. Startup preservou todos campos antigos; plano não escreveu; aplicação criou6 finais/moveu2 refs; demais campos e filhos preservados; versões4/8/2→5/9/3; repetição snapshot igual/0efeitos. As assertions do script foram conferidas em legacy-check.py:39/43/49/57/59/60/63/66/68/71/72/74/80. LogsCLI CP1252 têm cópiasUTF8 para distinguir Concluído de Concluido. CLI força validate apesar de create-drop fornecido. Nenhum mysqld estrangeiro foi operado.

## Discrimination Sensor

### Sensor inicial em arquivos, histórico

Profundidade expandida de integridade de dados:7 mutações comportamentais. Scratch nova copiou somente pom.xml, src/main/java e src/test, sem node_modules, target/dist ou application.properties normal. Cada fonte foi restaurada em bytes após a mutação. Logs e resultados ficam fora da scratch.

| Mutação em cópia | Fonte original file:line | Fault e resultado exato discriminado | Testes/exit | Resultado |
| --- | --- | --- | --- | --- |
| M1 | sistema/src/main/java/com/patp/sistema/service/EtapasFinaisService.java:31 | Criar apenasCONCLUIDA; `EtapasFinaisServiceTests.java:40`/86 exige2, `GerenciamentoFinaisTests.java:32` exige2; rollback73 também discrimina500 vs201 |9 executados,5 failures,0 errors/skips; exit1 | KILLED, compilação válida |
| M2 | sistema/src/main/java/com/patp/sistema/service/EtapaService.java:83 e102 | Retirar guardas de edição/remoção finais; `EtapaFinaisApiTests.java:44`:409 esperado,400 obtido |8 executados,2 failures,0 errors/skips; exit1 | KILLED, compilação válida |
| M3 | sistema/src/main/java/com/patp/sistema/service/EtapaService.java:131 | Incluir finais na sequência; `EtapaFinaisApiTests.java:101`:400 esperado,201/200 obtidos; linha84 detecta alteração de campos finais |8 executados,3 failures,0 errors/skips; exit1 | KILLED, compilação válida |
| M4 | sistema/src/main/java/com/patp/sistema/service/PreparacaoEtapasFinaisService.java:105 | Reconhecer lowercase por equalsIgnoreCase; `PreparacaoEtapasFinaisTests.java:131` exige2 refs,153 exige8 movimentos |12 executados,2 failures,0 errors/skips; exit1 | KILLED, compilação válida |
| M5 | sistema/src/main/java/com/patp/sistema/service/PreparacaoEtapasFinaisService.java:66 | Limpar motivoCancelamento após trocar referência; `PreparacaoEtapasFinaisTests.java:156` exige igualdade de todos campos legados excetoetapa_id |12 executados,1 failure,0 errors/skips; exit1 | KILLED, compilação válida |
| M6 | sistema/src/main/java/com/patp/sistema/service/PreparacaoEtapasFinaisService.java:74 | Trocar FORCE_INCREMENT por PESSIMISTIC_WRITE; `PreparacaoEtapasFinaisTests.java:177`/196/272 exige+1 |12 executados,3 failures,0 errors/skips; exit1 | KILLED, compilação válida |
| M7 | sistema/src/main/java/com/patp/sistema/service/EtapaService.java:102 | Retirar só guardaDELETE, preservandoPUT; `EtapaFinaisApiTests.java:47`:409 esperado,200 obtido |8 executados,2 failures,0 errors/skips; exit1 | KILLED, compilação válida |

Comandos: M1 `mvn.cmd -B -Dtest=EtapasFinaisServiceTests,GerenciamentoFinaisTests test`; M2/M3/M7 `-Dtest=EtapaFinaisApiTests`; M4/M5/M6 `-Dtest=PreparacaoEtapasFinaisTests`. Todos executados no backend scratch, H2. Não se contou falha de compilação como kill. Resultado de discriminação:7 killed,0 survived.

### Isolamento e limitação de descarte

Antes de criar a scratch, `git status --porcelain` foi capturado como bytes completos em porcelain-before.bin. O root manteve freeze de escritas. O comando de descarte novo usava PowerShell nativo `Remove-Item -LiteralPath`, precedido de resolução/checagens do alvo audit/scratch dentro da pasta TEMP única. O executor rejeitou o comando antes de iniciá-lo: `CreateProcess Rejected; blocked by policy`. Nenhum descarte ocorreu, nem houve retry ou contorno.

Antes de escrever este relatório, os bytes completos foram capturados novamente em porcelain-after-blocked-cleanup.bin:8600 bytes em ambos, SHA256 `1ce25dc73720590a4011b3856568a950f03b5b191026a2cb3ccd01176cf34aa2`, comparação de bytes `True`. Isso comprova que o sensor não modificou a árvore real, inclusive STATE e artefatos históricos. **Essa primeira tentativa não equivale ao ciclo completo de descarte e comparação.** O sensor novo abaixo concluiu o ciclo por outro mecanismo de isolamento, sem operar a scratch cujo descarte foi recusado. A scratch preservada está em TEMP audit/scratch. Fontes restauradas, logs/sensor-results.json/sensor-isolation.json preservados; não foram tentados stash/reset nem limpeza histórica.

### Sensor final em RAM, conclusivo

Novo ambiente isolado: `C:/Users/Marco/AppData/Local/Temp/etapas-finais-ram-6cecccc8b93e4264aefc18773d7c7a0e`. Esse sensor usa fontes originais da árvore real e bytecode de testes do gate independente, sem ler ou alterar a scratch bloqueada. O harness/logs permanecem como evidência; nenhum arquivo de fonte completa mutada ou classe mutada foi escrito nesse ambiente.

O classpath foi extraído da propriedade surefire.test.class.path no XML do gate, conferindo todos os caminhos existentes e target/test-classes primeiro. O recurso de teste é H2 mem/creral-tests, driverH2, create-drop, server.port0. Foi acrescentado somente junit-platform-launcher6.0.3, compatível com engine/jupiter6.0.3 já presentes. Nenhuma configuração normal ou credencial foi inspecionada pelo Verificador.

O próprio JDK executou RamSensor.java em source-file mode. `RamSensor.java:11` usa SimpleJavaFileObject com string de fonte; linha16 mantém a saída em ByteArrayOutputStream; linhas19–23 interceptam CLASS_OUTPUT e demais saídas, sem delegação de escrita ao filesystem. Linha60 chama JavaCompiler com --release17/-proc:none/classpath comprovado. Linhas28–36 carregam todas as classes com.patp.sistema por child-first; linha66 exige que a classe compilada tenha esse loader. Linhas68–76 executam os testes atuais pelo JUnit Platform, exigindo29 testes iniciados, zero skipped/aborted/containersFailed e AssertionError positivo para cada kill. A JVM própria termina em cada variante, descartando fontes/classes mutantes e os contextos de teste da memória. Fontes compiladas de Preparacao incluem seus três records, todos na mesma saída RAM.

Controle original pelo mesmo mecanismo:29/29 testes aprovados,0 failures/skips/aborts/containerfails, compilação=true, loaderChild, exit0. São4 do par,5 da criação,8 API e12 preparação. Após essa prova, as mesmas sete alterações comportamentais do histórico foram recriadas separadamente em RAM, uma JVM por variante. Nenhuma assertion ou teste foi alterado.

| Variante | Testes iniciados | Testes com falha | Falhas por AssertionError | Saídas de classes em RAM | Exit | Resultado |
| --- | --- | --- | --- | --- | --- | --- |
| CONTROL original |29|0|0|1|0|PASS|
| M1 par incompleto |29|13|9|1|1|KILLED|
| M2 guardas finais omitidas |29|2|2|1|1|KILLED|
| M3 intervalo inclui finais |29|3|3|1|1|KILLED|
| M4 lowercase reconhecido |29|2|2|4|1|KILLED|
| M5 motivo legado limpo |29|1|1|4|1|KILLED|
| M6 versão sem incremento |29|3|3|4|1|KILLED|
| M7 apenas guardaDELETE omitida |29|2|2|1|1|KILLED|

Todas as variantes compilaram, usaram loaderChild e tiveram0 skips/aborts/containerfails. M1 também produziu quatro erros de domínio pela ausência de Cancelada; são explicitamente separados das nove falhas por assertion que comprovam seu kill. Compilação inválida, bootstrap inválido ou descoberta incompleta não foram contados como kill.

Logs CONTROL.log e M1.log–M7.log contêm RAM_RESULT e as falhas completas. run-ram.py usa Popen.wait/poll e registra cada PID próprio encerrado em ram-results.json. CONTROL/M1 retornaram antes das demais variantes; o runner final também retornou após M7. Não há .class no diretório RAM. Toda fonte completa mutada e bytecode mutante desse novo sensor existiram apenas em memória das JVMs encerradas.

Antes do controle, foram capturados bytes completos em TEMP audit/ram-porcelain-before.bin. Depois de todos os processos próprios retornarem e antes de reescrever este relatório, foram capturados bytes completos em ram-porcelain-after.bin:8647 bytes em ambos; comparação integral True; SHA256 `e7f812fc22c589f52439af653d60dc7ae2a0530a0235a6b3723ca0e7068f07ed`. ram-isolation.json registra descarte por término de JVM e zero arquivos.class. O root corroborou independentemente o mesmo baseline e manteve freeze. STATE, target, dist, node_modules e demais arquivos reais não mudaram durante esse sensor.

**Resultado conclusivo:**7/7 mutações compiladas mortas,0 sobreviventes; controle29/29PASS; estado mutante RAM descartado pelo término dos processos; isolamento pós-descarte confirmado byte-a-byte. A scratch anterior permanece preservada porque a revisão automática recusou a exclusão. Este segundo sensor é um novo teste independente, não uma tentativa de executar a exclusão recusada.

## Code Quality e Edge Cases

Mudanças cirúrgicas nas entidades/DTO/serviços/repositórios existentes; nenhum frameworknovo. CLI própria sem endpoint público, ddlvalidate forçado e main normal do pacote preservado. Guard/permissões/arquivo/versão existentes permanecem. Interface usa categoria do servidor e não sintetiza finais. As rotas têm happy/edge/error e testes de estado persistido/rollback; domínio tem evidência individual para os30ACs. Diretrizes: skill/coding-principles.md e matriz de tasks.md; não foi encontrado AGENTS/CONTRIBUTING adicional aplicável.

Edges comprovados: finais vazias, quadro sem trabalho, quadros independentes, homônimos, categoriaSQLnull, ordens empatadas/com lacunas, versão antiga/futura, arquivo/terceiro/admin, manutenção em arquivo/semcriador, statusnull/lowercase/acento/espaço, datas/motivos ausentes/contraditórios, finais parciais/duplicadas, falha parcial em último quadro, repetição e inicialização normal.

## Ranked Gaps / Fix Plan

Nenhum gap funcional, mutante sobrevivente ou gap de precisão. O sensor final em RAM resolveu a limitação de encerramento sem operar a scratch bloqueada. UAT humano segue pendente, separado dos gates automatizados. Ações sobre demandas e decisões futuras não são declaradas prontas.

A revisão automática rejeitou o descarte da primeira scratch com reason blocked by policy. Ela continua preservada; não houve nova tentativa de exclusão nem contorno. Não há ação de código/testes para corrigir.

Não há sinal das categorias de distilação de lessons.md (surviving mutant, failed/uncovered AC, spec-precision gap, SPEC_DEVIATION). Nenhuma lesson foi fabricada para o bloqueio resolvido por novo mecanismo de teste.

## Deterministic Gates

Scripts executados depois do relatório PASS, usando python e --root .:

| Script | Resultado |
| --- | --- |
| .agents/skills/tlc-spec-driven/scripts/validate_spec.py etapas-finais --root . | exit0;0 erros,0 warnings |
| .agents/skills/tlc-spec-driven/scripts/validate_tasks.py etapas-finais --root . | exit0;0 erros,0 warnings |
| .agents/skills/tlc-spec-driven/scripts/validate_state.py etapas-finais --root . | exit0;0 erros |

Somente validation.md foi escrito pelo Verificador; STATE/spec/tasks não foram alterados por ele. O root começou seus metadados de fechamento depois da comparação final e do fim do freeze, sem afetar o baseline comprovado.

## Summary

**Overall: PASS automatizado.** 30/30 ACs comprovados;0 gaps de precisão;7/7 mutações RAM mortas;0 sobreviventes; controle29/29PASS e ciclo de descarte/isolamento em memória completo. Gates independentes278 Java,235 Vitest,3 E2E, buildTEMP/lint aprovados; MySQL físico30PASS revisado.

**UAT humano:**pendente. **Operações de demandas:**fora deste corte. A scratch histórica permanece preservada após rejeição automática de descarte; não foi tentado contorno.
