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
