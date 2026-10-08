# Evidências da implementação

Implemented exige gate local; Verified depende do Verifier fresco após T12.

## T1: persistir marcador de arquivo

Premissas: contrato/bordas spec.md, dependências concluídas e nenhuma mudança de política. Arquivos: sistema/src/main/java/com/patp/sistema/model/Processo.java, sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java. Documentos: tasks/spec/evidence/DIAGNOSTICO e somente Handoff do STATE. Sucesso: outcomes exatos, estado persistido/DTO e gate.

Gate PASS: 5 testes, 0 falhas/erros/skips. Log `C:/Users/Marco/AppData/Local/Temp/arquivo-T1.log`. Maven test selecionado; T8 verify completo, JDK25/H2/create-drop/port0/Mockito5.23.0. Sem skip/remoção; adequação antiga autorizada somente em T4.

Check A, suficiência:

| Critério/borda | Evidência/assert exato | Outcome | Coberto |
| --- | --- | --- | --- |
| ARQ-18 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:24` — `assertThat(salvo.isArquivado()).isEqualTo(Boolean.TRUE.equals(valor));` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:25` — `assertThat(json.readTree(json.writeValueAsString(salvo)).get("arquivado").asBoolean()).isEqualTo(Boolean.TRUE.equals(valor));` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:28` — `assertThat(jdbc.queryForMap("select * from processos where id=?",p.getId())).isEqualTo(esperado);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:34` — `assertThat(p.isArquivado()).isFalse();` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:35` — `assertThat(new Processo().isArquivado()).isFalse();` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:36` — `assertThat(p).extracting("numeroProcesso","pessoa").containsExactly("NOVO","Cliente");` | valor literal/estado exigido pelo critério, expresso no assert | Sim |

Check C, necessidade:

| Evidência/assert | Critério/borda | Manter |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:24` — `assertThat(salvo.isArquivado()).isEqualTo(Boolean.TRUE.equals(valor));` | ARQ-18 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:25` — `assertThat(json.readTree(json.writeValueAsString(salvo)).get("arquivado").asBoolean()).isEqualTo(Boolean.TRUE.equals(valor));` | ARQ-18 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:28` — `assertThat(jdbc.queryForMap("select * from processos where id=?",p.getId())).isEqualTo(esperado);` | ARQ-18 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:34` — `assertThat(p.isArquivado()).isFalse();` | ARQ-18 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:35` — `assertThat(new Processo().isArquivado()).isFalse();` | ARQ-18 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:36` — `assertThat(p).extracting("numeroProcesso","pessoa").containsExactly("NOVO","Cliente");` | ARQ-18 | Sim |

Adequação A/B/C/D PASS: outcomes/campos por valor, nenhuma tautologia; casos ligados a critérios/bordas. Padrão ApiIntegrationSupport, docs/OPERACAO.md/pom.xml. Sem SPEC_DEVIATION. Status Implemented; revisão independente pendente.

## T2: estender snapshot

Premissas: contrato/bordas spec.md, dependências concluídas e nenhuma mudança de política. Arquivos: sistema/src/main/java/com/patp/sistema/dto/ConfiguracaoEtapasResponse.java, sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java. Documentos: tasks/spec/evidence/DIAGNOSTICO e somente Handoff do STATE. Sucesso: outcomes exatos, estado persistido/DTO e gate.

Gate PASS: 2 testes, 0 falhas/erros/skips. Log `C:/Users/Marco/AppData/Local/Temp/arquivo-T2.log`. Maven test selecionado; T8 verify completo, JDK25/H2/create-drop/port0/Mockito5.23.0. Sem skip/remoção; adequação antiga autorizada somente em T4.

Check A, suficiência:

| Critério/borda | Evidência/assert exato | Outcome | Coberto |
| --- | --- | --- | --- |
| ARQ-19 | `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:19` — `assertThat(tree.propertyNames()).containsExactlyInAnyOrder("gerenciamento","etapas","demandas","demandasArquivadas");` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19 | `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:21` — `assertThat(tree.get(lista).get(0).propertyNames()).containsExactlyInAnyOrder("id","numeroProcesso","pessoa","responsavel","status","prioridade","dataEmissao","prazoEtapa","prazoGeral","dataConclusao","dataCancelamento","motivoCancelamento","observacoes","etapaId");` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19 | `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:22` — `assertThat(json.treeToValue(tree.get(lista).get(0),DemandaResponse.class)).isEqualTo(lista.equals("demandas")?ativa:arquivo);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19 borda compatibilidade | `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:27` — `assertThat(r.demandasArquivadas()).isEmpty();` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19 borda compatibilidade | `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:28` — `assertThat(r.demandas()).containsExactly(item(1L));` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19 borda compatibilidade | `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:29` — `assertThat(json.readTree(json.writeValueAsString(r)).get("demandasArquivadas").isArray()).isTrue();` | valor literal/estado exigido pelo critério, expresso no assert | Sim |

Check C, necessidade:

| Evidência/assert | Critério/borda | Manter |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:19` — `assertThat(tree.propertyNames()).containsExactlyInAnyOrder("gerenciamento","etapas","demandas","demandasArquivadas");` | ARQ-19 | Sim |
| `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:21` — `assertThat(tree.get(lista).get(0).propertyNames()).containsExactlyInAnyOrder("id","numeroProcesso","pessoa","responsavel","status","prioridade","dataEmissao","prazoEtapa","prazoGeral","dataConclusao","dataCancelamento","motivoCancelamento","observacoes","etapaId");` | ARQ-19 | Sim |
| `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:22` — `assertThat(json.treeToValue(tree.get(lista).get(0),DemandaResponse.class)).isEqualTo(lista.equals("demandas")?ativa:arquivo);` | ARQ-19 | Sim |
| `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:27` — `assertThat(r.demandasArquivadas()).isEmpty();` | ARQ-19 borda compatibilidade | Sim |
| `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:28` — `assertThat(r.demandas()).containsExactly(item(1L));` | ARQ-19 borda compatibilidade | Sim |
| `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:29` — `assertThat(json.readTree(json.writeValueAsString(r)).get("demandasArquivadas").isArray()).isTrue();` | ARQ-19 borda compatibilidade | Sim |

Adequação A/B/C/D PASS: outcomes/campos por valor, nenhuma tautologia; casos ligados a critérios/bordas. Padrão ApiIntegrationSupport, docs/OPERACAO.md/pom.xml. Sem SPEC_DEVIATION. Status Implemented; revisão independente pendente.

## T3: separar arquivo nas leituras

Premissas: contrato/bordas spec.md, dependências concluídas e nenhuma mudança de política. Arquivos: sistema/src/main/java/com/patp/sistema/service/EtapaService.java, sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java. Documentos: tasks/spec/evidence/DIAGNOSTICO e somente Handoff do STATE. Sucesso: outcomes exatos, estado persistido/DTO e gate.

Gate PASS: 5 testes, 0 falhas/erros/skips. Log `C:/Users/Marco/AppData/Local/Temp/arquivo-T3.log`. Maven test selecionado; T8 verify completo, JDK25/H2/create-drop/port0/Mockito5.23.0. Sem skip/remoção; adequação antiga autorizada somente em T4.

Check A, suficiência:

| Critério/borda | Evidência/assert exato | Outcome | Coberto |
| --- | --- | --- | --- |
| ARQ-18/19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:27` — `assertThat(r.demandas()).containsExactly(DemandaResponse.de(nulo),DemandaResponse.de(ativa));` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18/19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:28` — `assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(arquivo));` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18/19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:29` — `assertThat(r.etapas()).extracting("quantidadeDemandas").containsExactly(2L,0L);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18/19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:30` — `assertThat(r.gerenciamento().arquivado()).isEqualTo(quadroArquivado);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18/19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:31` — `assertThat(conteudoPersistido()).isEqualTo(antes);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-18/19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:32` — `assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(versao);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:41` — `assertThat(r.demandas()).isEmpty();assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(p));` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:42` — `assertThat(r.etapas()).extracting("quantidadeDemandas").containsOnly(0L);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19/20 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:44` — `assertThat(removido.gerenciamento().versao()).isEqualTo(3L);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-21 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:48` — `assertThatThrownBy(()->service.excluir(token(dono).substring(7),q.getId(),e.getId(),0L)).isInstanceOfSatisfying(ApiException.class,x->{assertThat(x.getStatus().value()).isEqualTo(409);assertThat(x.getMessage()).isEqualTo("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.");});` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-21 | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:49` — `assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19 borda independência | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:55` — `assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(p));assertThat(r.demandas()).isEmpty();` | valor literal/estado exigido pelo critério, expresso no assert | Sim |
| ARQ-19 borda independência | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:56` — `assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(r.gerenciamento().versao()).isEqualTo(2L);` | valor literal/estado exigido pelo critério, expresso no assert | Sim |

Check C, necessidade:

| Evidência/assert | Critério/borda | Manter |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:27` — `assertThat(r.demandas()).containsExactly(DemandaResponse.de(nulo),DemandaResponse.de(ativa));` | ARQ-18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:28` — `assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(arquivo));` | ARQ-18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:29` — `assertThat(r.etapas()).extracting("quantidadeDemandas").containsExactly(2L,0L);` | ARQ-18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:30` — `assertThat(r.gerenciamento().arquivado()).isEqualTo(quadroArquivado);` | ARQ-18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:31` — `assertThat(conteudoPersistido()).isEqualTo(antes);` | ARQ-18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:32` — `assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(versao);` | ARQ-18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:41` — `assertThat(r.demandas()).isEmpty();assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(p));` | ARQ-19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:42` — `assertThat(r.etapas()).extracting("quantidadeDemandas").containsOnly(0L);` | ARQ-19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:44` — `assertThat(removido.gerenciamento().versao()).isEqualTo(3L);` | ARQ-19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:48` — `assertThatThrownBy(()->service.excluir(token(dono).substring(7),q.getId(),e.getId(),0L)).isInstanceOfSatisfying(ApiException.class,x->{assertThat(x.getStatus().value()).isEqualTo(409);assertThat(x.getMessage()).isEqualTo("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.");});` | ARQ-21 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:49` — `assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();` | ARQ-21 | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:55` — `assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(p));assertThat(r.demandas()).isEmpty();` | ARQ-19 borda independência | Sim |
| `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:56` — `assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(r.gerenciamento().versao()).isEqualTo(2L);` | ARQ-19 borda independência | Sim |

Adequação A/B/C/D PASS: outcomes/campos por valor, nenhuma tautologia; casos ligados a critérios/bordas. Padrão ApiIntegrationSupport, docs/OPERACAO.md/pom.xml. Sem SPEC_DEVIATION. Status Implemented; revisão independente pendente.
