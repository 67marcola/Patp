# Cadastro e leitura de demandas: evidências

## T3: contrato frontend de snapshot e cadastro

PASS. Gate `frontend npm.cmd test`:377 testes em11arquivos, zero falhas/skips;235anteriores preservados e142novos. Gate repetido após fortalecer mensagens das assertions de campos incompletos, com377/377 novamente. Nenhum Maven/E2E/build ou artefato anterior alterado por esta tarefa.

Assumptions: validação comum nas cinco rotas de snapshot;14campos exatos, opcionaisnull/status desconhecido/texto antigo longo permitidos. Files: api.js/api.test.js e fixtures de Quadro.test/QuadroEtapas.test/App.test/Gerenciamentos.test, além de status/evidência. Success: rota/Bearer/novecampos, saída inteira/incompleta/inconsistente/204, erros/abort sem retry e baseline preservada.

### Check A: resultados e suficiência

Prefixo: `frontend/src/services/`; A=api.test.js. Os objetos comparados por `toEqual` contêm valores explícitos dos14campos em A:108–111, e dosnovecampos de cadastro em A:173–175.

| AC / critério | file:line e assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| CAD-30 rota/Bearer/novecampos | A:183 `expect(await criarDemanda("sessao", 9, dadosDemanda)).toEqual(configuracao)`; A:184 `expect(fetchSpy).toHaveBeenCalledExactlyOnceWith(...)` com URL demanda,POST,Bearer e `body: JSON.stringify(dadosDemanda)` | UmPOST com novecampos e snapshot integral | Sim |
| CAD-36 todasrotas/snapshot completo/legados/vazio | A:196 `expect(await executar()).toEqual(legado)`; A:199 `expect(await executar()).toEqual(vazio)` nascinco rotas | Preservar14campos, datasISO/status desconhecido/texto10001, arraysvazios | Sim |
| CAD-36 metadata/listas/IDs/vínculo/counts/flat | A:226 `await expect(executar()).rejects.toMatchObject({ name: "ApiError", status, message: ... })`; A:228 `expect(fetchSpy).toHaveBeenCalledTimes(1)` |19 formas inseguras rejeitadas nascinco rotas, mensagemERRO_COMUNICACAO exata e sem retry | Sim |
| CAD-36 presença/tipo de todos14campos | A:235/A:248 `await expect(buscarConfiguracaoEtapas("sessao", 9)).rejects.toMatchObject({ status: 200, message: ... })` para14ausentes e14tipos/datas inválidos | Campos incompletos, IDs string e datas impossíveis não confirmam sucesso | Sim |
| CAD-33/36 HTTP204 | A:255 `await expect(executar()).rejects.toMatchObject({ status: 204, message: ... })`; A:257 chamadaúnica |204 não confirma nenhuma dascinco rotas | Sim |
| CAD-33 HTTP/rede/JSON | A:262 `rejects.toMatchObject({ message: "Mensagem do servidor", status })`; A:270 `rejects.toMatchObject({ status: falha === "rede" ? 0 : 201, message: ... })`; A:263/A:272 chamadaúnica |400/401/404/409/500 preservados; rede0/JSON201 incertos semPOST repetido | Sim |
| Abort/isolation infraestruturaCAD-35 | A:280 `rejects.toBe(erro)`; A:281 `expect(fetchSpy.mock.calls[0][1].signal).toBe(controller.signal)`; A:282 chamadaúnica |AbortError original e AbortSignal preservados, semretry | Sim |
| Fixtures/baseline | api.test.js:108 array comduas demandas; QuadroEtapas.test.jsx:19 `demandas: colunas.flatMap(...)`; App/Quadro/Gerenciamentos fixtures vazios |Counts2/3/1 correspondem aos cartões de fixtures testonly;235cenários/assertions anteriores preservados | Sim |

### Check C: necessidade

| file:line e assertion | AC / critério | Manter |
| --- | --- | --- |
| A:183 snapshot `toEqual`; A:184 POST completo `toHaveBeenCalledExactlyOnceWith` | CAD-30 contratoemitido/retornado | Sim |
| A:196 legado `toEqual`; A:199 vazio `toEqual` | CAD-36 validade sem limites retroativos | Sim |
| A:226 `rejects.toMatchObject`; A:228 chamadaúnica,19casos×5rotas | CAD-36 integridade eCAD-33 noretry | Sim |
| A:235/A:248 `rejects.toMatchObject` | CAD-36 todos14campos/types/datas | Sim |
| A:255 erro204; A:257 chamadaúnica | CAD-33/36 ausência de confirmação | Sim |
| A:262 mensagem/status; A:270 statusrede/JSON; A:263/A:272 chamadaúnica | CAD-33 erros semretry | Sim |
| A:280 `rejects.toBe(erro)`; A:281 signal; A:282 chamadaúnica | CAD-35 abort eCAD-36 contrato GET | Sim |

Adequação PASS: cadacritério T3 temresultado/assertion/localização; `toEqual` confere cada valor do payload/snapshot, não apenas chamada. Testes inversos detectam contratos incorretos; nenhum teste semrequisito, assertion prévia enfraquecida ou cenário removido. Convenções de localização/nome/runner existentes seguidas. Sem SPEC_DEVIATION. Resumo de contagem anterior preservado para T5.

## T1: snapshot com demandas persistidas

PASS. Gate `sistema mvn.cmd -B verify`, JDK25.0.2: 284 testes, zero falhas/erros/skips. Baseline278 preservada e seis casos novos em DemandaConsultaTests. Log: `C:/Users/Marco/AppData/Local/Temp/creral-demandas-backend-20261006/T1-verify.log`. Nenhum banco configurado operado. Não houve alteração dos testes anteriores.

Assumptions: os 14 campos e a compatibilidade de categoria SQLnull vêm do design. Leitura preserva valores antigos sem trim, limites retroativos ou escrita. Files: DemandaResponse, ConfiguracaoEtapasResponse, EtapaService, DemandaConsultaTests e status/evidência desta feature. Success: snapshot filtrado/ordenado, campos completos, contagens da mesma lista, vazio, ativo/arquivo/legados, CRUD e espera de commit.

### Check A: cobertura suficiente e resultados da especificação

Prefixo dos arquivos de teste: `sistema/src/test/java/com/patp/sistema/`. Evidências de campos usam DemandaConsultaTests.java.

| AC / critério | file:line e assertion | Resultado definido | Coberto |
| --- | --- | --- | --- |
| CAD-21 quadro/filtro/ordem | DemandaConsultaTests.java:50 `jsonPath("$.gerenciamento.id").value(quadro.getId())`; :51 `jsonPath("$.demandas.length()").value(2)`; :66 `jsonPath("$.demandas[1].id").value(minima.getId())` | Somente duas demandas do quadro, ordem de ID | Sim |
| CAD-21 id | DemandaConsultaTests.java:52 `jsonPath("$.demandas[0].id").value(completa.getId())` | ID persistido | Sim |
| CAD-21 numeroProcesso | DemandaConsultaTests.java:53 `jsonPath("$.demandas[0].numeroProcesso").value(completa.getNumeroProcesso())` | Número persistido | Sim |
| CAD-21 pessoa | DemandaConsultaTests.java:54 `jsonPath("$.demandas[0].pessoa").value("Pessoa")` | Pessoa persistida | Sim |
| CAD-21 responsavel | DemandaConsultaTests.java:55 `jsonPath("$.demandas[0].responsavel").value("Ana")` | Responsável persistido | Sim |
| CAD-21 status | DemandaConsultaTests.java:56 `jsonPath("$.demandas[0].status").value("Em andamento")` | Status literal persistido | Sim |
| CAD-21 prioridade | DemandaConsultaTests.java:57 `jsonPath("$.demandas[0].prioridade").value("Urgente")` | Prioridade persistida | Sim |
| CAD-21 dataEmissao | DemandaConsultaTests.java:58 `jsonPath("$.demandas[0].dataEmissao").value("2020-02-03")` | Data ISO persistida | Sim |
| CAD-21 prazoEtapa | DemandaConsultaTests.java:59 `jsonPath("$.demandas[0].prazoEtapa").value("2020-02-04")` | Data ISO persistida | Sim |
| CAD-21 prazoGeral | DemandaConsultaTests.java:60 `jsonPath("$.demandas[0].prazoGeral").value("2020-02-05")` | Data ISO persistida | Sim |
| CAD-21 dataConclusao | DemandaConsultaTests.java:61 `jsonPath("$.demandas[0].dataConclusao").value("2020-02-06")` | Data antiga preservada | Sim |
| CAD-21 dataCancelamento | DemandaConsultaTests.java:62 `jsonPath("$.demandas[0].dataCancelamento").value("2020-02-07")` | Data antiga preservada | Sim |
| CAD-21 motivoCancelamento | DemandaConsultaTests.java:63 `jsonPath("$.demandas[0].motivoCancelamento").value("Legado")` | Motivo antigo preservado | Sim |
| CAD-21 observacoes | DemandaConsultaTests.java:64 `jsonPath("$.demandas[0].observacoes").value("<b>Texto antigo</b>\nOutra linha")` | Texto literal persistido | Sim |
| CAD-21 etapaId/flat | DemandaConsultaTests.java:65 `jsonPath("$.demandas[0].etapaId").value(segunda.getId())`; :73 `propertyNames()).containsExactlyInAnyOrder(...)` com os14 nomes explícitos | EtapaID, exatamente14 campos sem cadeia de entidades | Sim |
| CAD-21 opcionaisnull/vazio | DemandaConsultaTests.java:76 `snapshot.get("demandas").get(1).get(campo).isNull()).as(campo).isTrue()` para nove opcionais; :85 `jsonPath("$.demandas.length()").value(0)` | Null explícito e [] vazio | Sim |
| CAD-22 counts/ordem | DemandaConsultaTests.java:68 `jsonPath("$.etapas[0].id").value(primeira.getId())`; :69/:71 `quantidadeDemandas).value(1)`; :85 `quantidadeDemandas).value(0)` | Cada count corresponde aos cartões da etapa; empate por ID | Sim |
| CAD-22 categorias oficiais | DemandaConsultaTests.java:119 `jsonPath("$.etapas[2].categoria").value("CONCLUIDA")`; :120 `jsonPath("$.etapas[3].categoria").value("CANCELADA")` | Finais oficiais ordenadas após trabalhos | Sim |
| CAD-23 ativo/arquivo/legados | DemandaConsultaTests.java:98 `jsonPath("$.gerenciamento.arquivado").value(arquivado)`; :101 status desconhecido; :102 espaços; :103 texto10001; :107 `Map.of(...).isEqualTo(antes)` | Valores antigos e todas tabelas/versão idênticos | Sim |
| CAD-24 pósCRUD | DemandaConsultaTests.java:116/:125/:128 `jsonPath("$.demandas[0].id").value(demanda.getId())`; :126/:130 counts1 | Criar/editar/excluir etapa conserva cartão persistido/count | Sim |
| CAD-25 snapshot apóscommit | DemandaConsultaTests.java:151 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; :153 versão1; :154 Confirmada; :155 count1; :157 Depois; :158 etapaId | GET aguarda commit e retorna versão/etapa/cartão/count atuais | Sim |

### Check C: necessidade, mapa inverso

| file:line e assertion | AC / resultado correspondente | Manter |
| --- | --- | --- |
| DemandaConsultaTests.java:50 `gerenciamento.id).value(quadro.getId())`; :51 `demandas.length()).value(2)`; :52 `id).value(completa.getId())`; :66 `id).value(minima.getId())` | CAD-21 filtro e ordem | Sim |
| DemandaConsultaTests.java:53 `numeroProcesso).value(completa.getNumeroProcesso())`; :54 `pessoa).value("Pessoa")`; :55 `responsavel).value("Ana")`; :56 `status).value("Em andamento")`; :57 `prioridade).value("Urgente")` | CAD-21 campos literais | Sim |
| DemandaConsultaTests.java:58 `dataEmissao).value("2020-02-03")`; :59 `prazoEtapa).value("2020-02-04")`; :60 `prazoGeral).value("2020-02-05")`; :61 `dataConclusao).value("2020-02-06")`; :62 `dataCancelamento).value("2020-02-07")` | CAD-21 datas ISO | Sim |
| DemandaConsultaTests.java:63 `motivoCancelamento).value("Legado")`; :64 `observacoes).value("<b>Texto antigo</b>\nOutra linha")`; :65/:67 `etapaId).value(...)`; :73 `propertyNames()).containsExactlyInAnyOrder(...)` | CAD-21 flat, vínculos e conteúdo | Sim |
| DemandaConsultaTests.java:76 `isNull()).as(campo).isTrue()`; :84 `demandas).isArray()`; :85 `demandas.length()).value(0)` e `quantidadeDemandas).value(0)` | CAD-21/22 opcionais e vazio | Sim |
| DemandaConsultaTests.java:68/:70 `etapas[n].id).value(...)`; :69/:71 `quantidadeDemandas).value(1)`; :119/:120 `categoria).value(...)` | CAD-22 sequência/counts/finais | Sim |
| DemandaConsultaTests.java:98 `arquivado).value(arquivado)`; :99 `versao).value(arquivado ? 1 : 0)`; :100 `categoria).value("TRABALHO")`; :101/:102/:103/:104 valores antigos; :105/:106 null; :107 `Map.of(...).isEqualTo(antes)` | CAD-23 arquivo, SQLnull e preservação semwrite | Sim |
| DemandaConsultaTests.java:116/:117/:118/:124/:125/:126/:128/:129/:130 IDs, vínculo, nome/count | CAD-24 cartões preservados após três mutações de etapa | Sim |
| DemandaConsultaTests.java:148/:150 `await(...).isTrue()`; :151 `TimeoutException`; :153/:154/:155/:156/:157/:158 estado póscommit | CAD-25 sincronização e snapshot coerente | Sim |

Adequação PASS: cada AC/edge T1 tem resultado e file:line; 14 campos conferidos por valor, teste de concorrência verifica espera e estado. Nenhum teste sem requisito, assertions vazias, mocks de resultado ou teste prévio removido/atenuado. Convenções de docs/OPERACAO.md e testes de integração existentes seguidas. Sem SPEC_DEVIATION.

## T2: cadastro validado na primeira etapa

PASS. Gate `sistema mvn.cmd -B verify`, JDK25.0.2: 390 testes, zero falhas/erros/skips, baseline278 preservada. Novos casos T2: DemandaCadastroTests92 e DemandaCadastroConcurrencyTests14; T1consulta6 preservados. Log: `C:/Users/Marco/AppData/Local/Temp/creral-demandas-backend-20261006/T2-verify.log`:191/253/406/416. Maven encerrado. Não houve MySQL configurado, push/deploy ou alteração das ações futuras.

Assumptions: nove campos estritos no novo endpoint; datas stringISO/null por deserializer local; trim e limites UTF-16, prioridade livre, prazos invertidos/passados válidos. Legado mantém200/entidade/sem versão; criação começa na primeira TRABALHO e recusa estado final. Files: ProcessoService/ProcessoRepository, GerenciamentoRepository/Guard, DemandaController/CriarDemandaRequest/DataDemandaDeserializer, DemandaCadastroTests/ConcurrencyTests, somente fixture autorizada em EtapaDemandConcurrencyTests e docs da feature. Success: todos CAD-01–20, rollback e races reais, >=baseline278 no gate completo.

### Decisão e diagnóstico de unicidade

O primeiro diagnóstico funcional executou103 casos,102PASS/1FAIL: Igual/igual em quadros distintos completava o segundo cadastro antes do commit do primeiro (`T2-diagnostic.log`:83/84/123). Igual/Igual detectou UNIQUE23505 e devolveu409; todos88 casos de campos/erros/rollback e7 regressões concorrentes passaram. O índice VARCHAR do H2 é case-sensitive; nenhuma URL/collation foi alterada para esconder a falha.

Correção técnica decidida: ambas rotas bloqueiam PESSIMISTIC_WRITE na primeira linha estável de gerenciamento antes do quadro alvo. O lock persiste até commit e serializa cadastros globais antes do precheckIgnoreCase. Essa linha não é excluída por endpoint atual, pode estar arquivada/sem criador e só é bloqueada, sem mutação/versão ou administração/ativo se não for o alvo. Tradeoff aceito: cadastros de quadros diferentes aguardam entre si. Ordem global→alvo; demais mutações atuais só bloqueiam o alvo e preparação percorre IDs ordenados. Busca somente leitura confirmou que controllers são os chamadores de cadastro em aplicação, sem uma mutação atual adquirir alvo e chamar cadastro depois. Nenhuma API futura foi mudada.

APIs confirmadas por javap nos JARs locais: Jackson3.1.5 JsonNode.propertyNames, JsonAnySetter2.21 e deserializers usados; Hibernate7.4.5 ConstraintViolationException.getKind()/ConstraintKind.UNIQUE; SpringTest7.0.9 MockitoSpyBean. Somente violação UNIQUE do INSERT em processos vira409. CHECK de demanda/histórico fica500 e reverte todas as tabelas/versão. O teste da garantia física desativa apenas o precheck com spy; INSERT/constraint/rollback são reais, e as assertions verificam HTTP e dados, sem usar contagem de mocks como evidência.

### Check A: cobertura suficiente e resultados da especificação

Prefixo: `sistema/src/test/java/com/patp/sistema/`. D=DemandaCadastroTests.java; C=DemandaCadastroConcurrencyTests.java. Cada referência D/C abaixo identifica o arquivo completo pelo prefixo e nome aqui definidos. FORMATO=`Dados da requisição inválidos.`; VERSAO=`Gerenciamento alterado por outro usuário. Atualize e tente novamente.`. `estado()` compara linhas de etapas/processos/comentários/históricos e todas as linhas/versões dos gerenciamentos.

| AC / critério | file:line e assertion | Resultado definido pela spec | Coberto |
| --- | --- | --- | --- |
| CAD-01 novo201/persistência/snapshot | D:41 `status().isCreated()`; D:42 `jsonPath("$.demandas.length()").value(1)` e `jsonPath("$.etapas[0].quantidadeDemandas").value(1)`; D:48 `assertThat(json.readTree(resultado.getResponse().getContentAsString()).get("demandas").get(0).get("id").asLong()).isEqualTo(demanda.getId())` |201, cartão com ID realmente persistido e count1; 14campos flat cobertos em T1 | Sim |
| CAD-02 número/pessoa trim | D:78 `assertThat(demanda.getNumeroProcesso()).isEqualTo("MiSto-1")` e `assertThat(demanda.getPessoa()).isEqualTo("João")`; D:92 `assertThat(limite.getNumeroProcesso()).isEqualTo("😀".repeat(127) + "a")` e `assertThat(limite.getPessoa()).isEqualTo("ç".repeat(255))` | Grafia preservada após trim,255 UTF-16 aceitos | Sim |
| CAD-03 responsável/prioridade | D:79 `assertThat(demanda.getResponsavel()).isEqualTo("Ana")` e `assertThat(demanda.getPrioridade()).isEqualTo("Prioridade livre")`; D:93 `assertThat(limite.getResponsavel()).isEqualTo("r".repeat(255))` e `assertThat(limite.getPrioridade()).isEqualTo("p".repeat(255))` | Opcionais com trim/limite255, texto livre | Sim |
| CAD-03 observações | D:80 `assertThat(demanda.getObservacoes()).isEqualTo("Uma\nOutra")`; D:94 `assertThat(limite.getObservacoes()).isEqualTo("o".repeat(10000))` | Trim das bordas, quebra interna e limite10000 | Sim |
| CAD-03 ausente/vazio/null | D:51 `assertThat(demanda.getResponsavel()).isNull(); assertThat(demanda.getPrioridade()).isNull(); assertThat(demanda.getObservacoes()).isNull()`; D:99 mesmas três assertions sobre `vazia` | Omitido/vazio/null vira null | Sim |
| CAD-04 três datas | D:81 `assertThat(demanda.getDataEmissao()).isEqualTo(LocalDate.of(2020, 2, 3))`; D:82 `assertThat(demanda.getPrazoEtapa()).isEqualTo(LocalDate.of(2019, 1, 1))` e `assertThat(demanda.getPrazoGeral()).isEqualTo(LocalDate.of(2018, 1, 1))`; D:52/D:100 assertions `getDataEmissao/getPrazoEtapa/getPrazoGeral().isNull()` | Datas ISO passadas/invertidas preservadas; omitido/null sem hoje | Sim |
| CAD-05 estado inicial/encerramento | D:50 `assertThat(demanda.getStatus()).isEqualTo("Em andamento")`; D:53 `assertThat(demanda.getDataConclusao()).isNull(); assertThat(demanda.getDataCancelamento()).isNull(); assertThat(demanda.getMotivoCancelamento()).isNull()` | Estado exato e três finaisnull | Sim |
| CAD-06 primeiro trabalho | D:136 `jsonPath("$.demandas[0].etapaId").value(primeira.getId())`; D:137 `assertThat(processos.findAll().get(0).getEtapa().getId()).isEqualTo(primeira.getId())`; D:138 `assertThat(jdbc.queryForList("select * from etapas order by id")).isEqualTo(antes)` | Categoria SQLnull, homônimo final, empate/lacuna por ordem/ID; sem inferir nome/escrever etapa | Sim |
| CAD-07 zero trabalho | D:148 `status().isConflict()` e `jsonPath("$.erro").value("Cadastre uma etapa de trabalho antes de criar demandas.")`; D:149 `assertThat(estado()).isEqualTo(antes)` |409 sem gravar, quadro vazio ou só final | Sim |
| CAD-08 todosauth | D:110 `status().isCreated()` e `jsonPath("$.demandas[0].numeroProcesso").value("Autorizada")`; D:111 `assertThat(processos.count()).isEqualTo(1)` e `assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsExactly(autor.getNome())` | Criador/admin/outro/quadro sem criador autorizados | Sim |
| CAD-09 sessão/quadro/arquivo | D:158 `status().isUnauthorized()` e `assertThat(estado()).isEqualTo(antes)`; D:161 `status().isNotFound()` e `jsonPath("$.erro").value("Gerenciamento não encontrado.")`; D:165 `status().isConflict()` e `jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar.")`; D:162/D:166 `assertThat(estado()).isEqualTo(antes)` |401/404/409 e estados preservados | Sim |
| CAD-10 campos inválidos nas2rotas | D:175 `status().isBadRequest()` e `jsonPath("$.erro").value(mensagem)`; D:176 `assertThat(estado()).isEqualTo(antes)` |400 com mensagens exatas dos cinco campos; null/branco/256/10001 e UTF-16 excedido, semwrite | Sim |
| CAD-11 JSON/versão/datas estritos | D:200/D:209 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:201/D:210 `assertThat(estado()).isEqualTo(antes)` |400 para JSON/null-body/versão stringfloatbool; em cada data array/número/bool/vazio/branco/data impossível | Sim |
| CAD-12 desconhecidos | D:221 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:222 `assertThat(estado()).isEqualTo(antes)` |400 para ID/etapa/status/três finais/desconhecido, mesmo null | Sim |
| CAD-13 globalprecheck/UNIQUE físico | D:242/D:255 `status().isConflict()` e `jsonPath("$.erro").value("Já existe uma demanda com esse número.")`; D:243/D:256 `assertThat(estado()).isEqualTo(antes)` |409 global nas2rotas com caixa diferente; garantia UNIQUE real quando precheck não encontra | Sim |
| CAD-14 versão | D:230 `status().is(versao == 999 ? 409 : 400)`; D:231 `jsonPath("$.erro").value(versao == 999 ? VERSAO : "Informe uma versão válida do gerenciamento.")`; D:232 `assertThat(estado()).isEqualTo(antes)` | Ausente/negativa400; obsoleta409; semwrite | Sim |
| CAD-15 incremento/histórico | D:54 `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L)`; D:55 `assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly("CRIACAO")`; D:56 descricao exata; D:57 autorOutro; D:58 processoIdpersistido; D:59 countdataHora1 | Versão+1 e exatamenteum CRIACAO/Processo criado./autor da sessão/ID/data na mesma transação | Sim |
| CAD-16 rollback real | D:267 `status().isInternalServerError()` e `jsonPath("$.erro").value("Não foi possível concluir a operação.")`; D:268 `assertThat(estado()).isEqualTo(antes)` | CHECK na demanda ou histórico,500; tudo reverte nas2rotas | Sim |
| CAD-17 race global ambasrotas | C:60 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; C:62 `assertThat(resposta.getStatus()).isEqualTo(409)`; C:64 `assertThat(processos.count()).isEqualTo(1)`; C:66 umCRIACAO; C:67 versão0ou1; C:68 versão perdedora0 |8 combinações novo/legado e Igual/igual aguardam commit; um registro/histórico | Sim |
| CAD-17 quadroglobal passivo | D:121 `status().is(legado ? 200 : 201)`; D:122 `assertThat(jdbc.queryForList("select * from gerenciamentos where id=?", global.getId())).isEqualTo(antes)`; D:124 versãoalvo0ou1 | Primeira linha arquivada/semcriador não impede outroativo e preserva dados/versão global | Sim |
| CAD-17 disputas com estrutura/arquivo | C:101 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; C:104 `assertThat(resposta.getStatus()).isEqualTo(esperado)`; C:111 demanda0ou1; C:112 histórico0ou1; C:113 órfãos0; C:114 versão1ou2 |6 ordens de arquivo/remoção/reordenação serializadas, com estado/erro do design e nenhuma escrita parcial | Sim |
| CAD-18 legado200/defaults/campos | D:281 `status().isOk()` e `jsonPath("$.numeroProcesso").value("Legado")`; D:282 `jsonPath("$.status").value("Em andamento")`; D:283 etapa/quadro persistidos; D:284/D:285/D:286 opcionais/datas por valor; D:287 finaisnull; D:288 versãozero; D:289 umCRIACAO | Sem versão/incremento, status omitido ou inicial, mesmos campos/defaults/transação/duplicidade | Sim |
| CAD-19 ID/estado/encerramento/vínculo | ProcessoArchiveTests.java:60 `status().isBadRequest()`; :61 `assertThat(conteudoPersistido()).isEqualTo(antes)`; :77 `status().isConflict()` e `jsonPath("$.erro").value(ERRO)`; D:297 `status().isBadRequest()` e `jsonPath("$.erro").value("O estado inicial da demanda é definido pelo sistema.")`; D:298 estadoigual | ID semupdate; vínculo persistido prevalece; estadofinal/datas/motivo400 semwrite | Sim |
| CAD-20 etapa posterior/final/inexistente | D:309 `status().isBadRequest()` e `jsonPath("$.erro").value("Novas demandas devem começar na primeira etapa de trabalho.")`; D:317 `status().isNotFound()` e `jsonPath("$.erro").value("Etapa não encontrada neste gerenciamento.")`; D:310/D:318 estadoigual | Posterior/finais400; inexistente404 | Sim |

### Payload/conjunction: histórico de criação

| Campo | file:line e assertion exata | Resultado |
| --- | --- | --- |
| acao | D:55 `assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly("CRIACAO")` | Exatamente um CRIACAO |
| descricao | D:56 `assertThat(jdbc.queryForList("select descricao from historicos", String.class)).containsExactly("Processo criado.")` | Texto exato |
| usuario | D:57 `assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsExactly("Outro")` | Autor da sessão |
| processoId | D:58 `assertThat(jdbc.queryForObject("select processo_id from historicos", Long.class)).isEqualTo(demanda.getId())` | ID salvo |
| dataHora | D:59 `assertThat(jdbc.queryForObject("select count(*) from historicos where data_hora is not null", Long.class)).isEqualTo(1L)` | Data existente no único histórico |

### Check C: necessidade, mapa inverso por cenário

| file:line e assertion | AC / resultado correspondente | Manter |
| --- | --- | --- |
| D:41 `status().isCreated()`; D:48 IDresponseigualpersistido; D:53 trêsfinaisnull; D:54 versão1; D:55 `containsExactly("CRIACAO")` | CAD-01/05/15 mínimo/defaults/transaction | Sim |
| D:78 `assertThat(demanda.getNumeroProcesso()).isEqualTo("MiSto-1")`; D:79 `assertThat(demanda.getResponsavel()).isEqualTo("Ana")`; D:82 `assertThat(demanda.getPrazoGeral()).isEqualTo(LocalDate.of(2018, 1, 1))` | CAD-02/03/04 completo/trim/prioridade livre/datas invertidas | Sim |
| D:92 `assertThat(limite.getNumeroProcesso()).isEqualTo("😀".repeat(127) + "a")`; D:94 `assertThat(limite.getObservacoes()).isEqualTo("o".repeat(10000))`; D:99 `assertThat(vazia.getObservacoes()).isNull()` | CAD-02/03/04 limitesUnicode/vazio/null | Sim |
| D:110 `status().isCreated()`; D:111 `assertThat(processos.count()).isEqualTo(1)` e autorporvalor | CAD-08 quatroatores/quadrosemcriador | Sim |
| D:121 `status().is(legado ? 200 : 201)`; D:122 `assertThat(jdbc.queryForList("select * from gerenciamentos where id=?", global.getId())).isEqualTo(antes)` | CAD-08/17/18 globalarquivado semalterar dados/versão | Sim |
| D:136 `jsonPath("$.demandas[0].etapaId").value(primeira.getId())`; D:138 `assertThat(jdbc.queryForList("select * from etapas order by id")).isEqualTo(antes)` | CAD-06 ordem/ID/categoriaSQLnull/homônimo/lacuna | Sim |
| D:148 `status().isConflict()` e mensagemzerotrabalho; D:149 `assertThat(estado()).isEqualTo(antes)` | CAD-07 vazio/sófinal semwrite | Sim |
| D:158 `status().isUnauthorized()`; D:161 `status().isNotFound()`; D:165 `status().isConflict()`; D:166 `assertThat(estado()).isEqualTo(antes)` | CAD-09 auth/404/arquivo | Sim |
| D:175 `status().isBadRequest()` e `jsonPath("$.erro").value(mensagem)`; D:176 `assertThat(estado()).isEqualTo(antes)` | CAD-10/18 inválidosnasduasrotas | Sim |
| D:200/D:209 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:201/D:210 `assertThat(estado()).isEqualTo(antes)` | CAD-11 JSON/versão/18tiposdatas | Sim |
| D:221 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:222 `assertThat(estado()).isEqualTo(antes)` | CAD-12 setepropriedadesfora docontrato | Sim |
| D:230 `status().is(versao == 999 ? 409 : 400)`; D:231 `jsonPath("$.erro").value(versao == 999 ? VERSAO : "Informe uma versão válida do gerenciamento.")` | CAD-14 ausente/negativa/obsoleta | Sim |
| D:242/D:255 `status().isConflict()` e `jsonPath("$.erro").value("Já existe uma demanda com esse número.")`; D:243/D:256 `assertThat(estado()).isEqualTo(antes)` | CAD-13/18 dupcaseglobal econstraintreal | Sim |
| D:267 `status().isInternalServerError()` e `jsonPath("$.erro").value("Não foi possível concluir a operação.")`; D:268 `assertThat(estado()).isEqualTo(antes)` | CAD-16/18 rollback por duas CHECKs/reais nas2rotas | Sim |
| D:281 `status().isOk()`; D:282 `jsonPath("$.status").value("Em andamento")`; D:288 `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero()` | CAD-18 entidade200/campos/defaults/semversão | Sim |
| D:297 `status().isBadRequest()` e `jsonPath("$.erro").value("O estado inicial da demanda é definido pelo sistema.")`; D:298 `assertThat(estado()).isEqualTo(antes)` | CAD-19 seisestados/encerramentoslegados | Sim |
| D:309 `status().isBadRequest()` e `jsonPath("$.erro").value("Novas demandas devem começar na primeira etapa de trabalho.")`; D:317 `status().isNotFound()` e erroetapa | CAD-20 posterior/finais/inexistente | Sim |
| C:60 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; C:62 `assertThat(resposta.getStatus()).isEqualTo(409)`; C:64 `assertThat(processos.count()).isEqualTo(1)` | CAD-13/17/18 oitocombinaçõesrace/caixa/2rotas | Sim |
| C:101 `TimeoutException`; C:104 `assertThat(resposta.getStatus()).isEqualTo(esperado)`; C:113 `assertThat(jdbc.queryForObject("select count(*) from processos p left join etapas e on e.id=p.etapa_id where e.id is null", Long.class)).isZero()` | CAD-17 seisdisputas estruturais/arquivo e integridade | Sim |
| EtapaDemandConcurrencyTests.java:78 `assertThat(resposta.getStatus()).isEqualTo(removerPrimeiro ? 404 : 409)`; :88 `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(removerPrimeiro ? 1L : 0L)`; :89 órfãoszero; :90 histórico0ou1 | CAD-18/20 + regressão ETA-21, mantendo todos7casos eassertions | Sim |
| ProcessoArchiveTests.java:41 `status().isConflict()`/mensagemarquivo; :60 `status().isBadRequest()`; :77 conflitovínculo; HistoricoArchiveTests.java:104 `status().isUnauthorized()` | CAD-09/19 + regressõesanteriores preservadas semalterações | Sim |

Adequação PASS: CAD-01–20 e os edges delimitados têm assertions de resultado, erros precisos e persistência/versão, com14campos de snapshot conferidos em T1 e payload de histórico campoacampo acima. Cada cenário tem requisito; nenhum teste removido/ignorado ou assertion prévia atenuada. Única fixture antiga mudou ordem de Origem para3 apenas quando criação (mover conserva1); todas as assertions e quatrocombinações concorrentes permanecem. Sem SPEC_DEVIATION. A alteração do mecanismo de unicidade foi registrada no design/context/spec antes do commit.

### Handoff para MySQL fictício

Fontes/gates backend encerrados após este gate; não iniciar Maven paralelo. Classes recomendadas: DemandaConsultaTests(6), DemandaCadastroTests(92), DemandaCadastroConcurrencyTests(14), EtapaDemandConcurrencyTests(7), ProcessoArchiveTests(11), HistoricoArchiveTests(15). Total selecionado145. O teste `falhasReaisDeDemandaOuHistoricoRevertemTudo` usa ADD CONSTRAINT CHECK e DROP CONSTRAINT em processos/historicos; a sintaxe precisa ser provada em MySQL8.0.43, sem excluir preventivamente esses quatrocasos. `restricaoUnicaFisicaRetorna409ERollbackSePrecheckNaoEncontra` deve provar UNIQUE1062/409/rollback nas2rotas, com precheck advisory desativado e INSERT real. `numeroEntreQuadrosDisputaAteCommitEConfirmaSomenteUma` tem8casos incluindo caixa/rotas; `cadastroDisputaArquivoRemocaoEReordenacaoSemEstadoParcial` tem6casos. `linhaGlobalArquivadaSemCriadorNaoImpedeCadastroNemMudaDados` tem2casos. UTF-16/emoji no limite deve persistir com charset fictício apropriado, sem mexer na grafia ou schema configurado.
