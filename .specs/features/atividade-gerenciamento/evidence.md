# Evidências de implementação da atividade

Estado: em andamento. Tarefas Implemented não significam Verified; Verifier independente posterior a T18.

## T1 — entidade

10/10/2026: Maven -B test-compile exit0, BUILD SUCCESS,54 fontes principais/45 fontes de testes. Nenhum teste executado neste gate, conforme matriz de entidade. validate_tasks exit0, aviso esperado Tests:none conferido.

| Critério | Evidência | Resultado esperado | Cobertura |
| --- | --- | --- | --- |
| FK obrigatória ao quadro; sem FK/cascade ao alvo/autor | Atividade.java:24 `@ManyToOne(optional = false)`; :34–44 IDs/nomes escalares | Identificação independente do alvo/autor | Inspeção de mapeamento e compile |
| Valores longos e precisão | Atividade.java:46 `TIMESTAMP(6)`; :49 `LONGTEXT` | Micros e JSON longo | Compile; persistência real atribuída a T2 |
| Vínculo único | Atividade.java:52 `@Column(name = "historico_id", unique = true)` | Vínculo escalar único | Compile; persistência real atribuída a T2 |

Reverse-map: sem testes novos em T1, conforme matriz; nenhuma asserção especulativa. Adequação A/B/C/D: entidade sem lógica, gate compile aprovado; comportamento e rollback serão provados no writer T2. Sem desvio da spec ou alteração de testes antigos.

## T2 — writer transacional

10/10/2026: gate Java completo `mvn.cmd -B test` exit0,853 testes (840 anteriores+13 novos),0 falhas/erros/skips. Log TEMP/atividade-T2-java.log. Fixtures recebem limpeza de atividades e snapshot adicional sem alterar outcomes antigos. Writer MANDATORY, whitelist sem texto de comentário, JSON largo e histórico validado no mesmo quadro. Identidade composta tipo/ID conserva quadros/etapas com IDs iguais. Ajuste mecânico no design: nomes existentes prazoGeral/observacoes.

Todos os caminhos abaixo são `sistema/src/test/java/com/patp/sistema/AtividadeRegistroTests.java`.

| Critério / AC | file:line + asserção | Resultado da spec |
| --- | --- | --- |
| ACT-15 snapshots, autor e alvo | :33 `assertThat(salvo.getGerenciamento().getId()).isEqualTo(q.getId())`; :34 tipo ETAPA_CRIADA/alvo ETAPA; :35 alvoId=e.id/nome Etapa; :36 autorId=u.id/nome Autor | Snapshots completos independentes do estado futuro |
| Horário micros, vínculo ausente | :37 `assertThat(salvo.getDataHora()).isEqualTo(LocalDateTime.of(2026,1,2,3,4,5,123456000))`; :38 `getHistoricoId().isNull()` | Horário preciso e vínculo opcional |
| ACT-29 sem segredo | :40 `assertThat(salvo.getAlteracoesJson()).doesNotContain("senha","hash-ficticio","email")` | Somente valores de atividade |
| Transação existente | :44 `assertThatThrownBy(...).isInstanceOf(IllegalTransactionStateException.class)`; :46 `atividades.count().isZero()` | Não iniciar transação independente |
| Null/literal/ordem | :54 tamanho2; :55 campo nome; :56 anterior literal espaços; :57 novo Novo; :58 `anterior.isNull().isTrue()`; :59 novo literal espaços | Valores persistidos sem normalização inventada |
|10000 caracteres | :67 `assertThat(...anterior...).isEqualTo(antes)`; :68 `assertThat(...novo...).isEqualTo(depois)` | Literais completos á/ç10000 |
| Edição vazia | :73 `assertThat(a.getAlteracoesJson()).isEqualTo("[]")` e count1 | Lista vazia aceita |
| Remoção/snapshot futuro | :80 nomes Antes/Autor; :81 anterior4; :82 novo null | Registro preservado após remoção/renomeação |
| ACT-23 unicidade e vínculo | :89 DataIntegrityViolationException; :91 `conteudoComAtividade().isEqualTo(antes)`; :92 historicoId=h.id | Não vincular duas vezes |
| Mesmo quadro | :97 exceção com mensagem exata; :99 `conteudoComAtividade().isEqualTo(antes)` | Recusar histórico de outro quadro |
| ACT-17 falha do evento | :108 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Ação/histórico/atividade revertidos |
| ACT-17 falha posterior | :116 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Evento já inserido revertido |
| Comentário sem mensagem | :120 IllegalArgumentException mensagem exata; :123 snapshot igual | Sem alterações informativas/texto |
| Ordem/alvos/campos criação | :128 `assertThat(diferencas(...)).containsExactly(...)` com IDs3/8, nomes/setor/ordem e anteriores null | Ordenar por ID e omitir null inicial |
| Edge quadro+etapa mesmoID | :136 `assertThat(diferencas(...)).containsExactly(...)` com6 alterações explícitas | Identidade composta, ambos completos |

| Reverse-map (teste e asserção) | AC / Done-when | Manter |
| --- | --- | --- |
| persisteTodosOsSnapshotsComMicrosEAutorReal :33–40 valores/snapshots exatos | ACT-15/29, precisão e persistência T2 | Sim |
| exigeTransacaoExistenteSemGravar :44/46 exceção+zero | Transação existente T2 | Sim |
| diferencasConservamNullEspacosEOrdemFixa :54–59 valores literais | Edge null/espaços e diferenças T2 | Sim |
| gravaAntesDepoisLiteralDeDezMilCaracteres :67/68 igualdade literal | Edge10000 | Sim |
| edicaoIdenticaPersisteListaVazia :73 JSON[]/count1 | Diferenças T2, ACT-16 | Sim |
| remocaoEAlteracaoPosteriorNaoMudamIdentificacaoHistorica :80–82 nomes/antes4/depoisnull | ACT-15, snapshot sem FK | Sim |
| vinculoHistoricoUnicoReverteSegundaGravacao :89/91/92 unicidade/preservação/vínculo | ACT-17/23 | Sim |
| recusaVinculoDeOutroQuadroSemMudancas :97/99 exceção/preservação | Vínculo mesmo quadro T2 | Sim |
| falhaDoEventoReverteAcaoEHistorico :108 snapshot igual | ACT-17 | Sim |
| falhaPosteriorReverteEventoJaGravado :116 snapshot igual | ACT-17 | Sim |
| comentarioNaoPermiteMensagemNemAlteracoes :120/123 recusa/preservação | ACT-29, texto excluído T2 | Sim |
| criacaoOrdenaTodasAsPosicoesPorIdSemNullInventado :128 lista exata | Diferenças ordenadas T2 | Sim |
| criacaoPreservaQuadroEEtapaComMesmoId :136 lista exata | Edge snapshots iniciais ACT-01 | Sim |

Adequação A/B/C/D aprovada: resultados específicos e snapshots completos; nenhuma asserção somente de chamada, teste excluído/ignorado ou novo requisito. Padrões JUnit/H2+ApiIntegrationSupport do projeto; Verifier permanece pendente.

## T3 — criação do quadro

10/10/2026: `mvn.cmd -B test` exit0,860 testes,0 falhas/erros/skips (7 novos). Log TEMP/atividade-T3-java.log. Sem mudança de outcomes anteriores; spy falha na persistência para provar rollback real. Arquivo abaixo: `sistema/src/test/java/com/patp/sistema/AtividadeQuadroCriacaoTests.java`.

| Critério / AC | file:line + asserção | Esperado |
| --- | --- | --- |
| ACT-01/15 evento único/autor/alvo/tempo | :35 `atividades.count().isEqualTo(1)`; :36 tipo QUADRO_CRIADO/alvo QUADRO; :37 IDs quadro; :38 nome Quadro novo/autorID/u.id/autorCriador; :39 `dataHora.isBetween(inicio,now+1s)` e nano%1000 zero | Uma ação com snapshots e tempo servidor |
| ACT-19 versão e iniciais | :40 `criado.versao().isZero()`; :41 tamanho6/3; :42 nome Quadro novo; :43 descrição literal; :44 false | Versão atual e valores reais |
| Edge trabalho inicial e finais automáticas | :46–51 ID/tipo/nome/campo/anteriornull/novo Trabalho/Setor/7; :53 count etapas3/2; :54 `alteracoesJson.doesNotContain("Concluídos","Cancelados")` | Snapshot iniciais fornecidas, sem falsas ações de finais |
| ACT-18 autoria forjada ignorada | :61 `autorId.isEqualTo(u.getId())`, nomeReal; :62 tipo QUADRO_CRIADO e semForjado | Autor/tipo do serviço |
| ACT-18 validação/sessão | :66 mensagem nome exata+:67 snapshot igual; :71 mensagem etapa+:72 snapshot igual; :76 sessão inválida+:77 snapshot igual | Recusas sem escrita |
| ACT-17 rollback | :82 `assertThatThrownBy(...).hasMessage("Falha do diário")`; :83 `conteudoComAtividade().isEqualTo(antes)` | Sem quadro/etapas/evento parcial |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| criacaoUnicaConservaQuadroEtapasIniciaisAutorEHorario false/true :35–54 valores completos | ACT-01/15/19 e edge iniciais/finais | Sim |
| payloadForjadoNaoEscolheAutorOuTipo :61/62 autor/tipo | ACT-18 | Sim |
| nomeInvalidoConservaTodaPersistencia :66/67 | ACT-18 validação | Sim |
| etapaInvalidaConservaTodaPersistencia :71/72 | ACT-18 validação | Sim |
| sessaoInvalidaConservaTodaPersistencia :76/77 | ACT-18 sessão | Sim |
| falhaDoDiarioReverteQuadroEIniciaisEFinais :82/83 | ACT-17 | Sim |

Adequação A/B/C/D aprovada: sete cenários, contagens completas, snapshots/rollback e valores específicos; guideline matriz JUnit/H2 existente. Feature em andamento, Verifier pendente.

## T4 — edição e arquivo do quadro

10/10/2026: gate Java completo exit0,873 testes,0 falhas/erros/skips,13 novos. Log TEMP/atividade-T4-java.log. Captura após guard.bloquear; getter `Gerenciamento.getMarcadorArquivado()` com JsonIgnore conserva o marcador bruto null sem mudar isArquivado/DTO. Sem alteração de outcomes antigos.

Arquivo: `sistema/src/test/java/com/patp/sistema/AtividadeQuadroAlteracaoTests.java`.

| Critério / AC | file:line + asserção | Esperado |
| --- | --- | --- |
| ACT-02/15/19 edição/criador/admin | :33 `assertThat(r.versao()).isEqualTo(1L)`/count1; :34 tipoQUADRO_EDITADO; :35 autorID/nome; :36 alvoNovo; :37 tamanho2/camponome; :38 anteriorQuadro/novoNovo; :39 campodescricao/anteriorDescrição; :40 novo literal espaços | Dois campos diferentes com autor real e versão1 |
| ACT-16 edição idêntica | :44 versão1/count1; :45 `assertThat(atividades.findAll().get(0).getAlteracoesJson()).isEqualTo("[]")` | Um evento vazio preservando incremento |
| ACT-03/38 arquivo/repetição/restauração | :50 `assertThat(conteudoComAtividade()).isEqualTo(antes)` após repetição; :51 versão2; :52 tipos exatos ARQUIVADO/RESTAURADO; :54–56 tamanho1/campoarquivado/false->true/true->false | Repetição sem evento/mudança, dois eventos reais |
| Edge null literal | :61 `assertThat(c.get(0).get("anterior").isNull()).isTrue()` e novo true | Null anterior não normalizado |
| ACT-18 cinco recusas | :74 `assertThatThrownBy(...).hasMessage(mensagem)` com mensagens literais switch:66–72; :75 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Permissão/versão/arquivo/nome/sessão sem escrita |
| ACT-17 duas falhas | :81 `assertThatThrownBy(...).hasMessage("Falha do diário")`; :82 snapshot igual | Rollback edição e arquivo |
| Captura depois de lock | :91 TimeoutException com latches; :93 tamanho2; :94 `assertThat(c.get(0).get("anterior").asText()).isEqualTo("Primeiro")`/novoSegundo/tamanho1; :95 versão2 | Segunda edição observa primeira confirmada |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| edicaoMostraAntesDepoisAutorRealEVersao false/true :33–40 | ACT-02/15/19 | Sim |
| edicaoIdenticaTemListaVaziaEIncrementoAtual :44/45 | ACT-16/19 | Sim |
| arquivoRestauracaoERepeticaoConservamEventosEVersoes :50–56 | ACT-03/38/19 | Sim |
| marcadorLegadoNullPreservaAnteriorLiteral :61 | Edge valores reais/null | Sim |
| recusasNaoAlteramDadosOuAtividade cinco casos :74/75 | ACT-18 | Sim |
| falhaDoDiarioReverteEdicaoOuArquivo dois casos :81/82 | ACT-17 | Sim |
| antesDepoisUsaEdicaoCommitadaAntesDoLock :91/93–95 | ACT-02, lock obrigatório | Sim |

Adequação A/B/C/D aprovada:13 cenários, valores/versão/dados completos e prova coordenada de lock, sem asserções apenas de chamadas; padrões locais JUnit/H2. Verifier pendente.

## T5 — criação de etapa

10/10/2026: gate Java completo exit0,887 testes,0 falhas/erros/skips,14 novos. Primeiro gate882 PASS; revisão de adequação acrescentou cinco branches ACT-18, novo gate887 PASS. Log TEMP/atividade-T5-java.log. Sem outcome antigo alterado. Snapshot escalar da sequência após lock e antes de normalização; finais criadas automaticamente não são ações adicionais. garantirPar não altera ordens das finais existentes, regra preservada.

Arquivo abreviado C=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaCriacaoTests.java`, S=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaSupport.java`.

| Critério / AC | file:line + asserção | Esperado |
| --- | --- | --- |
| ACT-04/15 evento completo único | S:21 `assertThat(atividades.count()).isEqualTo(1)`; S:23–27 tipo/alvoTipo/alvoID/nome/quadro/autorID/nome/historicoNull/dataMicros; S:28 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))` | JSON inteiro com cada campo/valor esperado explícito |
| ACT-07 início/meio/fim | C:30 evento(expected :24–29 com nome/setor/ordem e cada reordenação); C:31 versão1; C:32 `assertThat(etapas.findById(a.getId()).orElseThrow().getOrdem()).isEqualTo(posicao==1?2:1)`; C:33 B=3/2; C:34 Nova=posição | Todos os efeitos anteriores/novos e estado persistido |
| Edge primeira/finais | C:39–40 evento três campos iniciais; C:41 `assertThat(etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(q.getId())).hasSize(3)` | Uma atividade, três etapas, sem falsas ações finais |
| Lacunas/empates | C:47–50 evento expected A5->1/B5->3/C12->4/Nova null->2; C:51–54 ordens1/3/4/2 | Todos os afetados registrados, sem reconstruir antes |
| ACT-18 permissão/versão/arquivo | C:61 `assertThatThrownBy(...).hasMessage(mensagem)`; C:62 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Três recusas conservam dados/atividade |
| ACT-18 nome/setor/posição/sessão/ausência | C:74 `assertThatThrownBy(...).hasMessage(mensagem)`; C:76 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Cinco erros exatos, zero efeitos |
| ACT-17 rollback | C:81 `assertThatThrownBy(...).hasMessage("Falha do diário")`; C:82 snapshot igual | Etapa, finais, normalização e versão revertidos |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| criaInicioMeioFimComTodasAsPosicoesAfetadas três casos C:30–34/S:21–28 | ACT-04/07/15/19 | Sim |
| primeiraEtapaSemEventosFalsosParaFinaisAutomaticas C:39–41/S:28 | ACT-04, edge finais | Sim |
| lacunasEEmpatesRegistramTodasAsOrdensAnterioresReais C:47–54/S:28 | ACT-07 | Sim |
| recusasConservamTodosOsDados três casos C:61/62 | ACT-18 | Sim |
| validacaoSessaoEQuadroAusenteNaoGeramEvento cinco casos C:74/76 | ACT-18 | Sim |
| falhaDoDiarioReverteEtapaNormalizacaoFinaisEVersao C:81/82 | ACT-17 | Sim |

Adequação A/B/C/D aprovada:14 cenários; JSON completo de cada alteração e posições de todos afetados, snapshot completo nas recusas. Nenhum teste só de chamada, nenhum teste sem vínculo à spec. Matriz JUnit/H2 seguida; Verifier pendente.

## T6 — edição de etapa

12/10/2026: gate Java completo `mvn.cmd -B test` exit0,904 testes (887 anteriores+17 novos),0 falhas/erros/skips. Log TEMP/atividade-T6-java.log. Trabalho parcial de10/10 preservado e auditado; gate anterior não foi executado porque a revisão automática interrompeu a chamada por limite de uso. Nenhum outcome antigo ou novo alterado na retomada. Captura escalar após lock e antes de editar/normalizar.

E=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaEdicaoTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaSupport.java`.

| Critério / AC | file:line + asserção | Resultado esperado / cobertura |
| --- | --- | --- |
| ACT-05/15 campos nome/setor | S:28 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`; E:25 esperado nome A->Novo; E:26 setor Engenharia->Novo setor; E:30 `assertThat(alvo.getNome()).isEqualTo(campo.equals("nome")?"Novo":"A")` e setor correspondente | JSON inteiro com tipo/ID/nome/campo/anterior/novo exatos; somente campo diferente; coberto |
| ACT-07 todas posições | S:28 JSON inteiro; E:27 esperado A1->3/B2->1/C3->2; E:31 `assertThat(alvo.getOrdem()).isEqualTo(campo.equals("ordem")?3:1)`; E:32 B1/2; E:33 C2/3 | Antes/depois de todos os afetados e persistência completa; coberto |
| ACT-15/19 evento único/autor/versão | S:21 `assertThat(atividades.count()).isEqualTo(1)`; S:23–27 tipo/alvo/quadro/autor/histórico/horário; E:29 `assertThat(r.gerenciamento().versao()).isEqualTo(1L)` | Autor da sessão, alvo atual, uma atividade e incremento atual; coberto |
| ACT-16 idêntica | S:28 JSON igual à lista vazia passada E:38; E:38 `assertThat(r.gerenciamento().versao()).isEqualTo(1L)` | Evento de edição com[] e versão1; coberto |
| Edge lacunas/empates | S:28 JSON inteiro de esperado E:44 A7->1/B7->2/C19->3; E:45–47 `getOrdem().isEqualTo(1/2/3)` | Cada posição real anterior e nova; coberto |
| ACT-15 nomes preservados | E:53 `assertThat(preservado.getAlvoNome()).isEqualTo("Primeiro")`; E:54 `assertThat(preservado.getAutorNome()).isEqualTo("Dono")` e JSON literal igual; E:55 anterior A/novo Primeiro; E:56 count2/versão2 | Evento anterior não muda após renomeações; coberto |
| ACT-18 recusas | E:75 `assertThatThrownBy(...).hasMessage(mensagem)` com10 mensagens específicas E:65–73; E:77 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Permissão/versão/arquivo/final/outroquadro/ausência/nome/setor/posição/sessão sem efeitos; coberto |
| ACT-17 rollback | E:82 `assertThatThrownBy(...).hasMessage("Falha do diário")`; E:83 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Nome/setor/ordens/finais/versão/atividade revertidos; coberto |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| registraSomenteCamposAlteradosETodasAsPosicoes três casos E:29–33/S:28 | ACT-05/07/15/19 | Sim |
| edicaoIdenticaAceitaListaVaziaEIncremento E:38/S:28 | ACT-16/19 | Sim |
| normalizaLacunasEEmpatesMesmoSemAlterarNomeSetor E:43–47/S:28 | ACT-07/edge | Sim |
| renomeacaoPosteriorNaoMudaEventoAnteriorOuAutor E:53–56 | ACT-15 | Sim |
| recusasConservamTodaPersistencia dez casos E:75/77 | ACT-18 | Sim |
| falhaDoDiarioReverteNomeSetorSequenciaEVersao E:82/83 | ACT-17 | Sim |

Adequação A/B/C/D aprovada:17 cenários com valores da spec, payload completo, todas posições e snapshots completos nas falhas; sem asserções só de chamada. Matriz JUnit/H2 seguida, sem SPEC_DEVIATION. T6 Implemented; Verifier da feature pendente.

## T7 — remoção de etapa e fechamento do lote1

12/10/2026: `mvn.cmd -B package` exit0,921 testes (904 anteriores+17 novos),0 falhas/erros/skips,BUILD SUCCESS com jar. O snapshot da etapa removida e a sequência anterior são capturados depois do lock; gravação usa somente escalares após delete/confirmar. Sem alteração de outcomes antigos/novos, sem SPEC_DEVIATION.

R=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaRemocaoTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaSupport.java`.

| Critério / AC | file:line + asserção | Resultado esperado / cobertura |
| --- | --- | --- |
| ACT-06/15 identificação após remoção | R:32 `assertThat(etapas.findById(removida.getId())).isEmpty()`; S:24 `assertThat(a.getAlvoId()).isEqualTo(alvo.getId())` e `assertThat(a.getAlvoNome()).isEqualTo(alvo.getNome())`; S:28 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))` | ID/nome/setor/ordem antigos conservados mesmo sem etapa; coberto |
| Campos de remoção exatos | S:28 JSON inteiro; R:85–88 esperado nome antigo->null/setor Engenharia->null/ordem anterior->null | Cada propriedade alvoTipo/alvoId/alvoNome/campo/anterior/novo da lista verificada; coberto |
| ACT-07 todos afetados no início/meio/fim | S:28 esperado R:26–29; R:30 `assertThat(etapas.findById(e.getId()).orElseThrow().getOrdem()).isEqualTo(i+1<posicao?i+1:i)` | Todas as posições anteriores/novas e persistidas; coberto |
| ACT-19 finais e versão | R:33 `assertThat(r.gerenciamento().versao()).isEqualTo(1L)`; R:34 ordem final99; R:35 ordem final100 | Incremento atual e finais existentes preservadas; coberto |
| Última etapa/sem alvo persistido | R:40 `assertThat(etapas.findById(e.getId())).isEmpty()` e S:28 JSON inteiro de remoção; R:41 categorias exatas CONCLUIDA/CANCELADA | Sem trabalho restante, atividade intacta, finais automáticas sem falsas ações; coberto |
| Edge lacunas/empates | S:28 esperado R:45–46 removidaB7->null/A7->1/C20->2; R:47 ausênciaB; R:48 ordens1/2 | Valores reais antes e posições de todos afetados; coberto |
| ACT-18 demandas ativas/arquivadas | R:53 `assertThatThrownBy(...).hasMessage("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.")`; R:54 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Dois estados recusados sem atividade; coberto |
| ACT-18 duas finais | R:59 `assertThatThrownBy(...).hasMessage("Etapas finais obrigatórias não podem ser alteradas.")`; R:60 snapshot igual | CONCLUIDA/CANCELADA protegidas; coberto |
| ACT-18 demais recusas | R:75 `assertThatThrownBy(...).hasMessage(mensagem)` com mensagens R:67–73; R:76 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Permissão/versão/arquivo/sessão/outroquadro/ausências sem efeitos; coberto |
| ACT-17 rollback | R:81 `assertThatThrownBy(...).hasMessage("Falha do diário")`; R:82 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Etapa removida, sequência, finais, versão, histórico e diário revertidos; coberto |
| Evento único/autoria | S:21 `assertThat(atividades.count()).isEqualTo(1)`; S:23 tipoETAPA_REMOVIDA/alvoETAPA; S:25 quadroID; S:26 autorID/nome; S:27 históricoNull/horárioMicros | Um registro automático do autor da sessão; coberto |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| removeInicioMeioFimPreservandoAlvoEPosicoesDeTodosAfetados três casos R:30–35/S:21–28 | ACT-06/07/15/19 | Sim |
| removeUnicaEtapaMantendoDiarioSemAlvoPersistido R:40/41/S:28 | ACT-06/edge finais automáticas | Sim |
| remocaoNormalizaLacunasEEmpatesSemPerderAntes R:47/48/S:28 | ACT-07/edge | Sim |
| demandaAtivaOuArquivadaImpedeRemocaoSemEvento dois casos R:53/54 | ACT-18 e bloqueio ocupada | Sim |
| finaisNaoPodemSerRemovidasENaoGeramEvento dois casos R:59/60 | ACT-18/proteção finais | Sim |
| recusasNaoAlteramPersistencia sete casos R:75/76 | ACT-18 | Sim |
| falhaDoDiarioReverteRemocaoSequenciaFinaisEVersao R:81/82 | ACT-17 | Sim |

Adequação A/B/C/D aprovada:17 cenários, payload completo e valores da spec, todas posições e snapshots integrais nas falhas, sem testes de chamadas ou requisitos inventados. Matriz JUnit/H2 seguida. Referências de mapeamento T1 corrigidas mecanicamente nesta revisão para suas linhas exatas, sem modificar código/outcomes.

### Gate de fim da fase1 em12/10/2026

Executados sequencialmente: Maven package/test921 PASS; `npm.cmd test`1000/1000 em25 arquivos PASS,sem skips; `npm.cmd run lint` exit0; `npm.cmd run build -- --outDir <TEMP novo>` exit0. Vite gerou index.html/CSS/JS em `C:/Users/Marco/AppData/Local/Temp/atividade-T7-build-a345c50709ab4478b9a15b15d1432a00`, sem alterar dist real. Aviso normal de outDir externo sem limpeza; artefatos gerados e saída0 conferidos. Logs TEMP/atividade-T7-{java,vitest,lint,build}.log. Nenhuma aplicação iniciada contra MySQL real, nenhum push/deploy/upgrade/limpeza de gerados.

Lote1 T1–T7 Implemented:81 novos casos Java além dos840 anteriores. T8–T18 permanecem pendentes; mysql/browser e Verifier final pertencem às próximas fases. Nenhum ACT Verified ou feature concluída neste fechamento.

Gates documentais antes do commit T7: validate_spec --strict exit0,zero erros/avisos; validate_tasks exit0,zero erros/aviso esperado Tests:none T1 conferido na matriz; check_commit OK e git diff --check exit0. Arquivos de código/testes/documentos do lote conferidos explicitamente; gerados fora dos commits.

## T8 — cadastro de demanda

12/10/2026: `mvn.cmd -B test` exit0,934 testes (921+13 novos),0 falhas/erros/skips. Gate repetido após fortalecer horário. Log TEMP/atividade-T8-java.log. Rotas moderna/antiga conservam versões1/0; helper bruto conserva null e campo arquivado termina ordem da demanda. Sem SPEC_DEVIATION.
C=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaCadastroTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaSupport.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-08/15/23 evento/vínculo | S:23 `assertThat(atividades.count()).isEqualTo(1)`; S:25–28 tipo/alvo/quadro/autor; S:32–36 count1,ID vinculado,acaoCRIACAO,autor e demanda exatos | Um evento e um histórico da sessão,ID/número snapshot; coberto |
| Horário servidor/micros | S:29 `assertThat(a.getDataHora().getNano()%1000).isZero()`; S:30 `assertThat(a.getDataHora()).isBetween(inicio,java.time.LocalDateTime.now())` | Horário real da ação,micros; coberto |
| ACT-08 valores normalizados/10000 | S:31 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`; C:27–29 todos12 campos; C:31 observações literal10000 | JSON inteiro com cada propriedade das alterações e ordem; coberto |
| ACT-15 preservação futura | C:33 `assertThat(preservado.getAlvoNome()).isEqualTo("N-1")`,autorLeitor e JSON igual | Identificação/autor não reconstruídos; coberto |
| Null/opcionais | C:40/S:31 JSON exato somente seis campos; C:41 `assertThat(p.getResponsavel()).isNull()` e demais opcionais/encerramento | Ausências não inventadas; coberto |
| ACT-19 rotas/versões | C:26 status200/201; C:31 `assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(legado?0L:1L)` | Versões atuais mantidas; coberto |
| ACT-18 recusas | C:53 status/erro exatos sete casos; C:54 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | duplicidade/arquivo/versão/sessão/validação/semtrabalho/forjado sem escrita; coberto |
| ACT-17 rollback | C:60 `andExpect(status().isInternalServerError())` e `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Todas tabelas/versão revertidas nas duas rotas; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| cadastroCompletoNasDuasRotasTemValoresLiteraisAutorRealEVinculo dois casos C:26–33/S:23–36 | ACT-08/15/19/23,edge10000 | Sim |
| opcionaisNullOuVaziosNaoInventamMudancas dois casos C:39–41/S:31 | ACT-08,edge null/normalização | Sim |
| recusasConservamTodasAsTabelas sete casos C:53–54 | ACT-18 | Sim |
| falhaDoDiarioReverteCadastroHistoricoEVersao dois casos C:60 | ACT-17 | Sim |

A/B/C/D PASS: outcomes exatos,payload integral,sem testes rasos/fora da spec; matriz JUnit/H2. Outcomes antigos/novos não alterados. T8 Implemented,Verifier pendente.

## T9 — edição informativa de demanda

12/10/2026: `mvn.cmd -B test` exit0,965 testes (934+31 novos),0 falhas/erros/skips. Log TEMP/atividade-T9-java.log. Antes capturado após lock; somente campos informativos diferentes. Sem outcomes modificados/SPEC_DEVIATION.
E=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaEdicaoTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaSupport.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-09 todos campos/datas/10000 literal | E:31 esperado um campo; S:31 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`; E:34 `assertThat(json.readTree(json.writeValueAsString(salvo)).get(campo).asText()).isEqualTo(novo)` | Cada campo normalizado com antes/depois exatos,10000 preservados; coberto |
| Null/limpeza | E:40/S:31 JSON completo seis alterações para null; E:41 `assertThat(salvo.getResponsavel()).isNull()` e cinco outros | Seis opcionais limpos sem inventar valores; coberto |
| ACT-16 idêntica | E:46/S:31 lista[] e `assertThat(jdbc.queryForList("select * from processos")).isEqualTo(antes)` | Edição aceita com evento vazio,versão1; coberto |
| ACT-09/19 número/ciclo | E:32 número/status/etapa exatos; E:53 status/etapa/datas/motivo literais | Encerramentos Concluido/Cancelado e número preservados; coberto |
| ACT-15/19/23 metadados/versão/vínculo | S:23–36 evento único,alvo/autor/quadro/hora/vínculo/açãoEDICAO; E:33 `getVersao().isEqualTo(1L)` | Autor real,micros/intervalo,vínculo sem duplicar e incremento atual; coberto |
| ACT-18 recusas | E:61 status/erro exatos e `assertThat(conteudoComAtividade()).isEqualTo(antes)` | número/ciclo/arquivo/versão/permissão/sessão/validação/status recusados integralmente; coberto |
| ACT-17 rollback | E:66 `andExpect(status().isInternalServerError())` e snapshot integral igual | Edição,histórico,atividade e versão revertidos nas duas rotas; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| cadaCampoNasDuasRotasTemAntesDepoisReal14 casos E:31–34/S:23–36 | ACT-09/15/19/23,10000/normalização | Sim |
| limpezaDosSeisOpcionaisRegistraNull2 casos E:40–41 | ACT-09/null | Sim |
| identicaTemAlteracoesVaziasEIncrementoAtual2 casos E:46 | ACT-16/19 | Sim |
| encerramentoPermaneceIntegral2 casos E:52–53 | ACT-09/19 | Sim |
| recusasPreservamTodaPersistencia9 casos E:61 | ACT-18 | Sim |
| falhaReverteEdicaoHistoricoEVersao2 casos E:66 | ACT-17 | Sim |

A/B/C/D PASS: todos campos/payload inteiros,outcomes da spec,sem testes rasos/fora do alcance; matriz JUnit/H2 seguida. Implemented,Verifier pendente.
