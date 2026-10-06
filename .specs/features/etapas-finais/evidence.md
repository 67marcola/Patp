# Evidência de execução backend

## T1: categoria persistida e DTO

Pré-plano: categoria nullable STRING; getter null retorna TRABALHO, sem inferir nome nem escrever. Arquivos: CategoriaEtapa.java, Etapa.java, EtapaResponse.java, EtapaService.java, EtapaCategoriaTests.java e spec/tasks/evidence. Sucesso: persistência das três categorias e snapshot HTTP com campos/contagem reais; SQL null permanece null após GET. Sem mudança de fixtures globais.

Gate: `mvn.cmd -B verify`, JDK 25, classpath H2: **249 testes, 0 falhas/erros/skips**. Log: `C:/Users/Marco/AppData/Local/Temp/etapas-finais-backend-20261006/T1-verify.log`.

| AC/done when | Evidência + assertion | Resultado | Cobertura |
| --- | --- | --- | --- |
| Enum STRING persistido/devolvido | `sistema/src/test/java/com/patp/sistema/EtapaCategoriaTests.java:28` `.isEqualTo(categoria.name())`; `:30` `.getCategoria()).isEqualTo(categoria)` | TRABALHO/CONCLUIDA/CANCELADA | Sim |
| FIN-06 legado null/homônimo sem escrita | mesmo arquivo `:48` `jsonPath("$.etapas[0].categoria").value("TRABALHO")`; `:50` `assertThat(conteudoPersistido()).isEqualTo(antes)`; `:51` `.isNull()` | trabalho, SQL null intacto | Sim |
| FIN-07 identidade/campos/contagem | mesmo arquivo `:44–49` `value(etapa.getId())`, `value("Concluídos")`, `value("Engenharia")`, `value(7)`, `value("TRABALHO")`, `value(1)` | campos reais | Sim |

| Assertion (arquivo/linha acima) | Requisito | Manter |
| --- | --- | --- |
| :28/:30 persistência enum | T1 done when | Sim |
| :43–49 snapshot inteiro | FIN-06/07 | Sim |
| :50/:51 snapshot igual/SQL null | FIN-06 | Sim |

Adequação PASS: expectativas derivadas da spec, sem assertions rasas, campos individuais verificados, dois testes novos necessários. Padrões SpringBootTest/MockMvc do repositório seguidos. Nenhum teste alterado/excluído/skip, nenhuma SPEC_DEVIATION.

## T2: par oficial e ordenação

Pré-plano: componente sob transação/lock do chamador, categoria identifica finais; verificar duplicidades antes de gravações, criar somente ausentes. Ordenação em memória TRABALHO ordem/ID, CONCLUIDA, CANCELADA, preservando dados existentes. Arquivos: EtapasFinaisService.java, EtapasFinaisServiceTests.java e spec/tasks/evidence. Sucesso: campos exatos, par por quadro, repetição sem alteração, homônimos/lacunas/empates preservados e duplicidade explícita sem efeitos; Full H2.

Gate Full H2 PASS: **253 testes, 0 falhas/erros/skips**, log TEMP/etapas-finais-backend-20261006/T2-verify.log.

| AC/done when | Evidência + assertion em `sistema/src/test/java/com/patp/sistema/EtapasFinaisServiceTests.java` | Resultado | Cobertura |
| --- | --- | --- | --- |
| FIN-01/03 par exato por quadro | :40 `hasSize(2)`; :41 `containsExactly(CONCLUIDA, CANCELADA)`; :42 `containsExactly("Concluídos", "Cancelados")`; :43 `containsExactly(null, null)`; :44 `containsExactly(1, 2)`; :45 `isNotNull().isNotEqualTo(...)`; :47/48 `containsExactly(quadroId, quadroId)`; :49 `doesNotContainAnyElementsOf(...)` | campos oficiais e IDs próprios | Sim |
| FIN-08/14 homônimo/ordem preservados | :69 `containsExactly(b.getId(), c.getId(), a.getId())`; :70 categorias exatas; :72 `isEqualTo(trabalhosAntes)`; :74 `hasSize(5)` | ordem/ID entre trabalhos, finais no fim | Sim |
| FIN-11/26 preservar/repetir/completar ausente | :53 `isEqualTo(antes)`; :54 `isZero()`; :86 `hasSize(2)`; :87 `isEqualTo(existente.getId())`; :88 `isEqualTo(antes)`; :89 `isEqualTo(CANCELADA)` | somente final ausente, repetição zero efeito | Sim |
| FIN-27 duplicidade | :101/102 `isInstanceOf(IllegalStateException.class).hasMessage("Etapas finais duplicadas para a categoria CONCLUIDA.")`; :103 `isEqualTo(antes)` | erro explícito sem criação parcial | Sim |

| Assertion (arquivo/linha acima) | Requisito | Manter |
| --- | --- | --- |
| :40–49 | FIN-01/03 | Sim |
| :53/54 | FIN-26 | Sim |
| :69/70/72/74 | FIN-08/14 | Sim |
| :86–89 | FIN-11/26 | Sim |
| :101–103 | FIN-27 | Sim |

Adequação PASS: quatro testes de integração, campos reais e rejeição sem efeitos; padrões existentes, sem alterações em testes anteriores, sem SPEC_DEVIATION.

## T8: tipo físico VARCHAR explícito

Pré-plano corretivo: a inspeção bytecode local `MySQLDialect.getEnumTypeDeclaration` mostra `enum (` como tipo nativo. Fixar mapeamento VARCHAR(20) nullable para cumprir design, mantendo enum STRING. Arquivos: Etapa.java, EtapaCategoriaTests.java e evidence.md. Sucesso: assertion física INFORMATION_SCHEMA VARCHAR nullable/comprimento20 e Full H2. Verificação física MySQL TEMP será feita pelo root depois de T5.

Gate Full H2 PASS, 253 testes sem falhas/erros/skips; log T1a-verify.log no mesmo TEMP.

| Done when | Assertion | Resultado | Cobertura |
| --- | --- | --- | --- |
| VARCHAR20 nullable físico | `sistema/src/test/java/com/patp/sistema/EtapaCategoriaTests.java:23` `.containsEntry("DATA_TYPE", "CHARACTER VARYING").containsEntry("IS_NULLABLE", "YES").containsEntry("CHARACTER_MAXIMUM_LENGTH", 20L)` | VARCHAR20 nullable H2 | Sim |

| Assertion | Requisito | Manter |
| --- | --- | --- |
| EtapaCategoriaTests.java:23 INFORMATION_SCHEMA | T1 done when/design modelo VARCHAR nullable | Sim |

Adequação PASS, assertion adicional de schema sem enfraquecer as existentes. Não há SPEC_DEVIATION.

## T3: criação atômica

Pré-plano: garantir par na transação inicial depois dos trabalhos; validar IDs/categorias antes do primeiro save. Arquivos: GerenciamentoService.java, GerenciamentoController.java (DTO atualmente descarta ID/categoria, precisa repassá-los), GerenciamentoApiTests.java (somente contagens/listas autorizadas, assertions finais extras), GerenciamentoFinaisTests.java, spec/tasks/evidence. Sucesso: HTTP cria par próprio em dois quadros, preserva N trabalhos, rejeita entradas finais/ID com400 sem efeitos; constraint H2 na segunda final demonstra rollback depois de quadro/trabalho/primeira final persistidos. Full H2.

Gate Full H2 PASS: **258 testes, zero falhas/erros/skips**, log T3-verify.log no TEMP anterior; validate_spec/tasks PASS, zero erros/warnings.

| AC/done when | Evidência + assertion em `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java` | Resultado | Cobertura |
| --- | --- | --- | --- |
| FIN-01/02 sem/com trabalhos | :26 `isCreated()`/versão0; :32 `hasSize(2)`; :33 `hasSize(4)`; :34–37 `containsExactly("Concluídos", "Cancelados")`, setores Campo/Projeto, ordens3/7, categorias TRABALHO | trabalhos preservados + par | Sim |
| FIN-01/03 finais e vínculos/IDs próprios | :39 nomes exatos; :40 categorias exatas; :41 `containsExactly(null, null)`; :42 IDs distintos; :43 vínculos respectivos; :45 `doesNotContainAnyElementsOf(...)`; :48/49 categorias HTTP | par persistido próprio | Sim |
| FIN-04 rejeição ID/categoria | :62 `isBadRequest()`; :63 `isEqualTo(antes)`; :64 `isEqualTo(quadrosAntes)` |400 sem efeitos | Sim |
| FIN-05 falha segunda final | :73 `isInternalServerError()`/erro exato; :74/75 `isZero()` |rollback quadro/trabalho/primeira final | Sim |

| Assertion (arquivo/linha acima) | Requisito | Manter |
| --- | --- | --- |
| :26/:29/:32–45/:47–49 | FIN-01/02/03 | Sim |
| :62–64 | FIN-04 | Sim |
| :73–75 | FIN-05 | Sim |
| GerenciamentoApiTests listas atualizadas + todos os campos de trabalho anteriores | FIN-01/02 e regressão autorizada contexto | Sim |

Adequação PASS: cinco casos novos (1 criação,3 rejeição,1 falha parcial); testes antigos conservados. Somente cardinalidades/listas afetadas atualizadas, assertions finais exatas acrescentadas. Padrões de teste existentes seguidos; sem SPEC_DEVIATION.

## T4: proteção e sequência no CRUD

Pré-plano: separar trabalhos da lista completa; proteção por categoria depois de auth/arquivo/versão; garantir par somente na confirmação de mutation válida, na mesma transação/lock e incremento único. GET ordena/consulta sem preparação. Arquivos: EtapaService.java, EtapaFinaisApiTests.java, EtapaApiTests.java/EtapaArchiveTests.java/EtapaDemandConcurrencyTests.java para expectativas afetadas e assertions finais extras, spec/tasks/evidence. Sucesso: finais vazias protegidas409 erro exato sem mudanças; posições N/N+1 só trabalho, homônimos CRUD, snapshot com IDs/campos/contagens/finais ordenadas; auth/arquivo/conflito/rollback anteriores conservados. Full H2.

Gate Full H2 PASS: **266 testes, zero falhas/erros/skips**, log `C:/Users/Marco/AppData/Local/Temp/etapas-finais-backend-20261006/T4-verify.log`. A retomada reconciliou o diff com esse gate; nenhuma alteração de código/teste posterior ao gate.

| AC/done when | Evidência + assertion | Resultado da spec | Cobertura |
| --- | --- | --- | --- |
| FIN-09/10 finais vazias, criador/admin, PUT/DELETE | `sistema/src/test/java/com/patp/sistema/EtapaFinaisApiTests.java:44` e `:47` `isConflict()` / `jsonPath("$.erro").value("Etapas finais obrigatórias não podem ser alteradas.")`; `:48` `assertThat(estado()).isEqualTo(antes)` | 409 exato, registros/versão intactos | Sim |
| FIN-11 CRUD inteiro preserva par/campos/IDs e versão única | `EtapaFinaisApiTests.java:81` `value(3)`; `:83` `value(2)`; `:84` `assertThat(jdbc.queryForList("select * from etapas where categoria <> 'TRABALHO' order by id")).isEqualTo(finaisAntes)`; `:85` `isEqualTo(5L)`; `:86` `containsExactly(CONCLUIDA, CANCELADA)` | trabalho criado/editado/removido, finais integralmente iguais, uma versão por cada uma das cinco mutations | Sim |
| FIN-07/08/14 categoria por identidade, homônimo configurável, finais após trabalhos/contagens reais | `EtapaFinaisApiTests.java:59` `value("TRABALHO")`; `:60–63` nomes/categorias finais exatos; `:72–75` `value(finais.get(0).getId())`, `value(2)`, `value(finais.get(1).getId())`, `value(1)`; `:79` `value(trabalho.getId())` | homônimo trabalho, finais ordenadas após N trabalhos, IDs e contagens reais | Sim |
| FIN-07 demais campos reais do snapshot | `sistema/src/test/java/com/patp/sistema/EtapaApiTests.java:114–125` `value("Concluídos")`, `value("CONCLUIDA")`, `isEmpty()`, `value(0)`, `value("Cancelados")`, `value("CANCELADA")`, `value(primeira.getId())`, `value(1)`, `value("Mesmo")`, `value("Operação")`, `value(2)`, `value(ultima.getId())`, `value("Legada")`, `value(3)`; `sistema/src/test/java/com/patp/sistema/EtapaCategoriaTests.java:44–49` campos reais de etapa legado | valores reais de nomes, categorias, IDs, Setor, ordem e contagens | Sim |
| FIN-12 intervalos 1..N+1/1..N, inclusive somente finais | `EtapaFinaisApiTests.java:95` `isCreated()` posição1 com só finais; `:70` `isCreated()` posição2 com um trabalho; `:78–79` `isOk()` / ID trabalho posição2 com dois trabalhos; `EtapaApiTests.java:360–363` posição/criação/ordens exatas | somente N trabalhos delimita posições | Sim |
| FIN-13 posição final/Setor branco sem escrita | `EtapaFinaisApiTests.java:101` `isBadRequest()` / `value(setor ? "Informe um setor entre 1 e 255 caracteres." : "Escolha uma posição válida para a etapa.")`; `:102` `assertThat(estado()).isEqualTo(antes)` | 400 exato sem registros/versão alterados nos quatro casos | Sim |
| FIN-19 permission/arquivo/versão preservados | `EtapaApiTests.java:228–229` `isForbidden()` / `value(PERMISSAO)` / `isEqualTo(antes)`; `:246–247` `isConflict()` / `value(ARQUIVO)` / `isEqualTo(antes)`; `:387–388` `isConflict()` / `value(VERSAO)` / `isEqualTo(antes)` | 403/409/409 e zero efeito | Sim |
| FIN-29 GET legado readonly; rollback/concorrência anteriores conservados | `EtapaFinaisApiTests.java:111` `value(1)` / `value("TRABALHO")`; `:113–114` `value(1)` / `assertThat(estado()).isEqualTo(antes)`; `EtapaApiTests.java:421–423` `isInternalServerError()` / `value("Não foi possível concluir a operação.")` / `assertThat(estado()).isEqualTo(antes)`; `EtapaDemandConcurrencyTests.java:88` `isEqualTo(removerPrimeiro ? 1L : 0L)` e `:143` `isEqualTo(1L)` | GET não prepara; falhas revertidas; lock mantém versão | Sim |

As referências abreviadas nas tabelas são relativas a `sistema/src/test/java/com/patp/sistema/`.

| Assertion (arquivo/linha acima) | Requisito | Manter |
| --- | --- | --- |
| EtapaFinaisApiTests.java:44/47/48 (dois atores, duas finais, duas rotas) | FIN-09/10 | Sim |
| EtapaFinaisApiTests.java:59–86 (ciclo de homônimos completo) | FIN-07/08/11/12/14 | Sim |
| EtapaFinaisApiTests.java:95/101/102 (quatro entradas inválidas) | FIN-12/13 | Sim |
| EtapaFinaisApiTests.java:111/113/114 (consulta legado) | FIN-06/29 | Sim |
| EtapaApiTests.java:114–125/153–160/213–217 novas assertions finais; EtapaArchiveTests.java:83–88/152–153; EtapaDemandConcurrencyTests.java:85–86/139–140/151–154 | FIN-07/08/11/19 e adaptação autorizada das cardinalidades anteriores | Sim |
| Assertions anteriores de trabalho/auth/arquivo/versão/rollback, conservadas | FIN-12/13/19 e regressão exigida no done when | Sim |

Adequação PASS: oito casos novos, todos ancorados; campos e snapshots persistidos comprovam resultado, nenhum teste somente por chamada de mock. Padrão SpringBootTest/MockMvc/H2 do projeto seguido; sem skip/exclusão, sem SPEC_DEVIATION. Testes antigos alterados somente nas listas/contagens autorizadas, com finais exatas adicionais. A ausência de execução em startup receberá também evidência específica de main/contexto em T5.

## T5: preparação explícita

Pré-plano: serviço dedicado sem endpoint/runner; plano readonly inventaria todos os IDs ordenados e status exatos, inclusive null; aplicar bloqueia os quadros em ordem de ID e mantém toda execução na mesma transação. Apenas referências de Concluido/Cancelado mudam; garantia de par preserva trabalhos/finais existentes; uma versão por quadro alterado. Arquivos: PreparacaoEtapasFinaisService.java, PrepararEtapasFinais.java, ProcessoRepository.java, GerenciamentoRepository.java, pom.xml (main normal explicitado), PreparacaoEtapasFinaisTests.java, docs/OPERACAO.md, spec/tasks/evidence. Sucesso: fixtures completas com filhos/datas contraditórias/ausentes, arquivo/null criador/homônimos, plano/main readonly, execução/reexecução, versão/rollback global/duplicidade e recusa inválida antes do contexto; Full H2 e adequação antes do commit. Testes MySQL TEMP e Verificador serão feitos pelo root depois deste lote.

Gate Full H2 PASS: **278 testes, zero falhas/erros/skips**, sendo **12 casos novos** em PreparacaoEtapasFinaisTests. Log `C:/Users/Marco/AppData/Local/Temp/etapas-finais-backend-20261006/T5-verify.log`. Focado anterior: 11/11 PASS; depois foram acrescentados o caso de quadro vazio e assertions do relatório por quadro, seguidos do Full. Repackage manteve SistemaApplication como entrada normal; main de operador é selecionado explicitamente com PropertiesLauncher.

Todas as referências da tabela seguinte são de `sistema/src/test/java/com/patp/sistema/PreparacaoEtapasFinaisTests.java`. A fixture principal possui seis quadros/28 demandas, trabalhos homônimos com empate/lacuna/categoria SQL null, arquivo/criador null, final parcial, quadro completo que muda só referência e quadro já preparado. Todas as demandas têm comentário/histórico original. `semColuna` usa `select *` e remove exclusivamente o campo que a spec autoriza alterar; não omite qualquer outro campo da comparação.

| AC/done when | Evidência + assertion | Resultado da spec | Cobertura |
| --- | --- | --- | --- |
| FIN-20 inventário/plano integral readonly | :119 `extracting(q -> q.id()).containsExactlyElementsOf(quadrosCenario.stream().map(Gerenciamento::getId).toList())`; :120 `assertThat(plano.finaisCriadas()).isZero()`; :121 `assertThat(plano.referenciasAlteradas()).isZero()`; :124 `assertThat(inventario.nome()).isEqualTo("Legado " + i)`; :125 `isEqualTo(i == 1)`; :126 `isEqualTo(i == 2 ? null : quadrosCenario.get(0).getCriador().getId())`; :127 `containsExactly(CONCLUIDA, CANCELADA)`; :128 `containsExactly(new StatusInventario("Concluido", 1), new StatusInventario("Cancelado", 1), new StatusInventario(null, 1), new StatusInventario("Em andamento", 1), new StatusInventario("concluido", 1), new StatusInventario("Concluído", 1), new StatusInventario("Cancelado ", 1), new StatusInventario("cancelado", 1))`; :131 `isEqualTo(2)`; :137 `assertThat(estado()).isEqualTo(antes)` | todos os quadros/valores/status/ausências, zero gravações | Sim |
| FIN-21 finais ausentes em ativo/arquivo/null criador/par parcial/quadro vazio | :152 `assertThat(aplicado.finaisCriadas()).isEqualTo(7)`; :154 `extracting(q -> q.finaisCriadas()).containsExactly(2L, 2L, 2L, 1L, 0L, 0L)`; :181 `extracting(Etapa::getCategoria).containsExactly(CONCLUIDA, CANCELADA)`; :182 `containsExactly("Concluídos", "Cancelados")`; :183 `containsExactly(null, null)`; :184 `isNotEqualTo(finaisQuadro.get(1).getId())`; :192 `isEqualTo(2)`; :195 `extracting(Etapa::getOrdem).containsExactly(1, 2)` | criar somente ausentes, par oficial próprio | Sim |
| FIN-22 status exatos, só etapa_id e mesmo quadro | :153 `assertThat(aplicado.referenciasAlteradas()).isEqualTo(8)`; :155 `containsExactly(2L, 2L, 2L, 1L, 1L, 0L)`; :169 `assertThat(atual.getEtapa().getCategoria()).isEqualTo("Concluido".equals(anterior.getStatus()) ? CONCLUIDA : CANCELADA)`; :170 `assertThat(atual.getEtapa().getGerenciamento().getId()).isEqualTo(anterior.getEtapa().getGerenciamento().getId())`; :156 `assertThat(semColuna("processos", "etapa_id")).isEqualTo(dadosDemandas)` | referência correta, demais campos intactos | Sim |
| FIN-23 todos os campos/datas/motivos/IDs/status e comentários/históricos | :156 `assertThat(semColuna("processos", "etapa_id")).isEqualTo(dadosDemandas)`; :162 `assertThat(jdbc.queryForList("select * from comentarios order by id")).isEqualTo(comentariosAntes)`; :163 `assertThat(jdbc.queryForList("select * from historicos order by id")).isEqualTo(historicosAntes)` | todos os campos SQL intactos, inclusive datas/motivos ausentes/contraditórios da fixture :49–73; nenhum filho novo | Sim |
| FIN-24 trabalhos e campos do quadro intactos | :160 `assertThat(jdbc.queryForMap("select * from etapas where id=?", id)).isEqualTo(linha)` para toda etapa anterior; :157 `assertThat(semColuna("gerenciamentos", "versao")).isEqualTo(dadosQuadros)`; :164 `assertThat(jdbc.queryForList("select * from usuarios order by id")).isEqualTo(usuariosAntes)` | preserva nome/descrição/arquivo/criador e IDs/campos/ordens/Setor/categoria das etapas antigas | Sim |
| FIN-25 null/desconhecidos inventariados sem interpretação nem motivo/data | :128 status e quantidades exatos (null, andamento, caixa, acento e espaço); :172 `assertThat(atual.getEtapa().getId()).isEqualTo(anterior.getEtapa().getId())` para cada valor desconhecido; :156 snapshot integral exceto referência | preservação completa de null/variantes, nenhuma fabricação | Sim |
| FIN-26 reexecução zero efeitos/IDs/dados/versões | :205 `assertThat(repeticao.finaisCriadas()).isZero()`; :206 `assertThat(repeticao.referenciasAlteradas()).isZero()`; :208 `assertThat(q.finaisAusentes()).isEmpty()`; :209 `assertThat(q.referenciasAjustar()).isZero()`; :210/211 `finaisCriadas/referenciasAlteradas.isZero()`; :213 `assertThat(estado()).isEqualTo(antes)` | relatório zero e snapshot SQL integral igual | Sim |
| FIN-27 duplicidade de ambas categorias no último quadro | :223 `isInstanceOf(IllegalStateException.class)`; :224 `hasMessage("Etapas finais duplicadas para a categoria " + categoria + ".")`; :225 `assertThat(estado()).isEqualTo(antes)`; :226/227 mesma recusa no plano; :228 snapshot igual | erro explícito e rollback global após quadro anterior processado | Sim |
| FIN-27 falha de banco na última final | :238 `isInstanceOf(RuntimeException.class)`; :239 `assertThat(falha.getMessage().toLowerCase(Locale.ROOT)).contains("falha_preparacao")`; :240 `assertThat(estado()).isEqualTo(antes)` | constraint real falha, rollback integral de estruturas/referências/versões/filhos anteriores | Sim |
| FIN-28 versão por quadro só estrutura/só referência/ambos/nenhum | :177 `assertThat(quadro.getVersao()).isEqualTo(versoesAntes.get(i) + (i == 5 ? 0 : 1))`; :178 `assertThat(aplicado.quadros().get(i).versao()).isEqualTo(quadro.getVersao())`; :196 `getVersao()).isEqualTo(1)` com só par em quadro vazio; :213 snapshot reexecução igual | exatamente uma vez por quadro alterado, nenhum incremento sem efeito | Sim |
| FIN-29 startup normal e plano CLI sem preparação/schema writes | :261 `assertThat(estado()).isEqualTo(antes)` após contexto normal; :268 `assertThat(estado()).isEqualTo(antes)` após main plano com `create-drop` enviado; :270 `assertThat(etapas.count()).isEqualTo(3)`; :271 `getCategoria()).isEqualTo(CONCLUIDA)`; :272 `getVersao()).isEqualTo(1)` somente após main aplicar | startup/GET nunca preparam; operador força validate e aplica apenas com ação explícita | Sim |
| FIN-30 inválido antes de contexto/conexão | :279 `assertThatThrownBy(() -> PrepararEtapasFinais.main(args)).isInstanceOf(IllegalArgumentException.class)`; :280 `hasMessage("Informe a ação plano ou aplicar como primeiro argumento.")` para ausente/inválido/caixa/URL em lugar de ação | recusa exata antes de datasource inválido sequer abrir contexto | Sim |

| Assertion concreta (`PreparacaoEtapasFinaisTests.java`) | Maps to | Manter |
| --- | --- | --- |
| :119 `containsExactlyElementsOf(...)`, :120/121 `isZero()`, :124–128 `isEqualTo(...)`/`containsExactly(...)`, :131/133/134/135/136 `isEqualTo(2)`/`containsExactly(CANCELADA)`/`isEmpty()`/`isEqualTo(1)`/`isZero()`, :137 `assertThat(estado()).isEqualTo(antes)` | FIN-20/25, plano sem escrita | Sim |
| :152 `isEqualTo(7)`, :153 `isEqualTo(8)`, :154/155 `containsExactly(...)`, :156 `assertThat(semColuna("processos", "etapa_id")).isEqualTo(dadosDemandas)`, :157 `assertThat(semColuna("gerenciamentos", "versao")).isEqualTo(dadosQuadros)`, :160 `assertThat(jdbc.queryForMap("select * from etapas where id=?", id)).isEqualTo(linha)`, :162/163/164 snapshots SQL iguais, :169/170/172 destino/categoria/quadro ou referência igual, :177/178 versão exata, :181–184 categorias/nomes/Setor/IDs exatos | FIN-21/22/23/24/25/28, aplicação com campos reais | Sim |
| :192 `isEqualTo(2)`, :193 `isZero()`, :194 `containsExactly(CONCLUIDA, CANCELADA)`, :195 `containsExactly(1, 2)`, :196 `getVersao()).isEqualTo(1)`, :197 snapshot quadro igual, :198 `assertThat(processos.count()).isZero()` | FIN-21/28, quadro vazio e somente estrutura | Sim |
| :205/206/209/210/211 `isZero()`, :208 `isEmpty()`, :213 `assertThat(estado()).isEqualTo(antes)` | FIN-26, reexecução sem efeitos | Sim |
| :223/224 e :226/227 `isInstanceOf(IllegalStateException.class).hasMessage("Etapas finais duplicadas para a categoria " + categoria + ".")`, :225/228 `assertThat(estado()).isEqualTo(antes)` | FIN-27, duas categorias/rollback | Sim |
| :238/239 erro runtime contendo falha_preparacao, :240 `assertThat(estado()).isEqualTo(antes)` | FIN-27, falha parcial global | Sim |
| :260 `contexto.isActive()).isTrue()`, :261/268 `assertThat(estado()).isEqualTo(antes)`, :270 `isEqualTo(3)`, :271 `isEqualTo(CONCLUIDA)`, :272 `isEqualTo(1)` | FIN-29 e done when main explícito, sem startup/schema write | Sim |
| :279/280 `isInstanceOf(IllegalArgumentException.class).hasMessage("Informe a ação plano ou aplicar como primeiro argumento.")` | FIN-30, quatro entradas recusadas | Sim |

Adequação PASS: todas as ACs e edges deste corte têm resultado persistido/exato; comparações SQL incluem cada campo/filho, não apenas chamadas. Doze casos necessários, incluindo falhas após primeiro quadro já ter sofrido flush. Padrões SpringBootTest/MockMvc/H2 e docs/OPERACAO.md seguidos; nenhum teste antigo alterado, excluído ou skipped. Sem SPEC_DEVIATION. MySQL TEMP/schema antigo e Verificador independente permanecem gates do encerramento da feature a cargo do root.
