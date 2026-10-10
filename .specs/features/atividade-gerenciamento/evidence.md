# Evidências de implementação da atividade

Estado: em andamento. Tarefas Implemented não significam Verified; Verifier independente posterior a T18.

## T1 — entidade

10/10/2026: Maven -B test-compile exit0, BUILD SUCCESS,54 fontes principais/45 fontes de testes. Nenhum teste executado neste gate, conforme matriz de entidade. validate_tasks exit0, aviso esperado Tests:none conferido.

| Critério | Evidência | Resultado esperado | Cobertura |
| --- | --- | --- | --- |
| FK obrigatória ao quadro; sem FK/cascade ao alvo/autor | Atividade.java:25 `@ManyToOne(optional = false)`; :34–46 IDs/nomes escalares | Identificação independente do alvo/autor | Inspeção de mapeamento e compile |
| Valores longos e precisão | Atividade.java:51 `TIMESTAMP(6)`; :54 `LONGTEXT` | Micros e JSON longo | Compile; persistência real atribuída a T2 |
| Vínculo único | Atividade.java:57 `@Column(name = "historico_id", unique = true)` | Vínculo escalar único | Compile; persistência real atribuída a T2 |

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
