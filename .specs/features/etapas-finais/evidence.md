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

## T6: apresentação e editor somente de trabalhos

Pré-plano: somente CONCLUIDA/CANCELADA identificam finais; null/ausente permanece trabalho legado, sem inferir pelo nome. Renderizar a lista/IDs/contagens recebidos, sem sintetizar colunas. Editor recebe quantidade e ordinal visual somente dos trabalhos. Guardar abrirEditor/abrirRemocao/salvar/remover pela categoria tanto do alvo em cache quanto do snapshot atual. Criação explica o par automático e continua enviando somente trabalhos. Arquivos: Quadro.jsx, CriarGerenciamento.jsx, QuadroEtapas.test.jsx, CriarGerenciamento.test.jsx e spec/tasks/evidence. Sucesso: preservar regressão de teclado/foco/pendência/erro/arquivo/versão e provar FIN-15–19, homônimos, legado sem par, finais vazias e intervalos de trabalhos; npm test completo e adequação antes do commit. Nenhum CSS necessário; nenhuma ação de demandas alterada.

Gate Quick `npm.cmd test` PASS: **235 testes, zero falhas/skips, 11 arquivos**, com **222 anteriores + 13 casos novos**. Baseline anterior 222/222 PASS. O primeiro gate detectou erro de coleta causado pela posição de um bloco novo de testes; o bloco foi movido para o escopo superior sem mudar assertions/casos. Gate seguinte 234/234 PASS; acrescentado o edge de finais vazias, seguido do gate completo 235/235 PASS (34,77s, 2026-10-06). `validate_tasks` e `git diff --check` PASS. Build/lint/Edge e Verificador pertencem ao fechamento T7.

Todas as referências Quadro abaixo são de `frontend/src/pages/QuadroEtapas.test.jsx`; as referências Criação são de `frontend/src/pages/CriarGerenciamento.test.jsx`.

| AC/done when/edge | Evidência + assertion | Resultado da spec | Cobertura |
| --- | --- | --- | --- |
| FIN-15 identificação oficial, sem Setor/ações/posição | Quadro:48 `expect(within(coluna).getByText("Etapa final obrigatória").textContent).toBe("Etapa final obrigatória")`; :49 `expect(within(coluna).queryByText(/Setor responsável:/)).toBeNull()`; :50 `expect(within(coluna).queryByText(/^Posição /)).toBeNull()`; :51 `expect(within(coluna).queryByRole("button")).toBeNull()` | tag exata, campos/controles ausentes em ambas oficiais | Sim |
| Finais/IDs/contagens recebidos, sem síntese | Quadro:47 ``expect(within(coluna).getByRole("heading").id).toBe(`etapa-titulo-${etapa.id}`)``; :52 ``expect(within(coluna).getByText(`${etapa.quantidadeDemandas} ${etapa.quantidadeDemandas === 1 ? "demanda" : "demandas"}`).textContent).toBe(`${etapa.quantidadeDemandas} ${etapa.quantidadeDemandas === 1 ? "demanda" : "demandas"}`)``; :520 `expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").id)).toEqual(["etapa-titulo-4", "etapa-titulo-5"])`; :521 `expect(screen.queryByText("Etapa final obrigatória")).toBeNull()` | IDs20/21 e contagens3/1 do servidor; legado contém somente IDs4/5 e nenhuma final inventada | Sim |
| Edge finais vazias protegidas | Quadro:470 `conferirFinais(vazias)` executa :47–53 com IDs20/21, contagens0/0 e mesmos controles ausentes; :471 `expect(screen.getAllByRole("region")).toHaveLength(2)` | par vazio continua obrigatório/protegido | Sim |
| FIN-16 intervalos só trabalhos e ordinais visuais | Quadro:478 `expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1", "2", "3"])`; :482 mesma expressão `.toEqual(["1", "2"])`; :484/:485 `getByText("Posição 1").textContent).toBe("Posição 1")` / `getByText("Posição 2").textContent).toBe("Posição 2")`; :307 mesma seleção com ordens legadas7/7 | criar1..N+1, editar1..N, posição visual entre trabalhos | Sim |
| FIN-16 Nome/Setor obrigatórios com zero trabalhos | Quadro:455 seleção `.toEqual(["1"])`; :458 `expect(screen.getByRole("alert").textContent).toBe("Informe um nome de etapa entre 1 e 255 caracteres.")`; :461 mesmo alerta `.toBe("Informe um setor entre 1 e 255 caracteres.")`; :462 métodos `.toEqual(["GET"])` | único lugar1, vazio recusado sem mutação | Sim |
| FIN-17 quadro sem trabalho conserva par e informa vazio | Quadro:441 `expect((await screen.findByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada" })).textContent).toBe("Nenhuma etapa de trabalho cadastrada")`; :443 lista headings `.toEqual(["Concluídos", "Cancelados"])`; :322 mesma lista após remoção do último trabalho, :324 métodos `.toEqual(["GET", "DELETE"])` | texto exato e finais persistentes antes/depois da última remoção | Sim |
| Homônimos explícitos/null/ausentes continuam trabalho | Quadro:498 `expect(within(coluna).queryByText("Etapa final obrigatória")).toBeNull()`; :499 Setor ``expect(within(coluna).getByText(`Setor responsável: ${etapa.setor}`).textContent).toBe(`Setor responsável: ${etapa.setor}`)``; :500 posição ``expect(within(coluna).getByText(`Posição ${index + 1}`).textContent).toBe(`Posição ${index + 1}`)``; :501 botão Remover `.not.toBeNull()`; :503/:504 nome/setor editor `.toBe(etapa.nome)` / `.toBe(etapa.setor)`; :505 seleção `.toEqual(["1", "2"])`; :522 `queryByRole("button", { name: "Remover etapa 2: Cancelados" })).not.toBeNull()` | TRABALHO e legado homônimo editáveis; oficiais separadas pela categoria/tag | Sim |
| FIN-18 explicar criação automática, sem colunas sintéticas no formulário | Criação:54 `expect(screen.getByRole("heading", { name: "Etapas de trabalho iniciais" }).textContent).toBe("Etapas de trabalho iniciais")`; :57 `expect(screen.getByText("As etapas finais Concluídos e Cancelados são criadas automaticamente.").textContent).toBe("As etapas finais Concluídos e Cancelados são criadas automaticamente.")`; :59/:66 `queryByLabelText("Nome da etapa 1"/"Nome da etapa 3")).toBeNull()` | descrição exata, somente linhas de trabalho adicionadas pelo usuário | Sim |
| FIN-18 payload vazio/com homônimos só trabalho | Criação:48 `expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome: "Instalações", descricao: "", etapas: [] })`; :69 `expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome: "Quadro", descricao: "", etapas: [{ nome: "Concluídos", setor: "Técnico", ordem: 1 }, { nome: "Cancelados", setor: "Técnico", ordem: 2 }] })` | campos normalizados exatos; sem ID/categoria/finais sintéticas | Sim |
| FIN-15/19 alvo em cache final após refresh | Quadro:543 `expect(screen.getByRole("button", { name: acao, exact: true }).disabled).toBe(true)`; :545 `expect(screen.getByLabelText("Posição").disabled).toBe(true)`; :548 `expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", metodo, "GET"])`; :550–552 tag exata/Setor/posição/ações ausentes | PUT e DELETE, ambas categorias oficiais: refresh não habilita nova mutação; posição final não configurável | Sim |
| FIN-19 arquivo/permissão/conflito, pendência e foco preservados | Quadro:107/:108/:109 existência de Nova/Editar/Remover `.toBe(permitido)`; :293–299 ausência de controles e rascunho bloqueado após refresh de flags; :209 payloads exatos com versões0/7; :154/:155/:169/:170 botões pendentes `.toBe(true)`; :143 `expect(document.activeElement).toBe(origem)`; :554 `expect(document.activeElement).toBe(screen.getByRole("button", { name: "+ Nova etapa" }))` | permissões do snapshot, retry manual/versão atual e teclado/foco existentes | Sim |

| Assertion concreta | Maps to | Manter |
| --- | --- | --- |
| Quadro:47–53 IDs/tag/contagens/ausência Setor/posição/botões, chamados :72/:81/:86/:278/:323/:352/:391/:429/:445/:463/:470/:486 | FIN-15 e preservação do par, edge final vazia; adaptações autorizadas de listas anteriores | Sim |
| Quadro:441/443/446/447/449 texto/pair/ações/flags/métodos; :322/324 pair após última remoção | FIN-15/17/19, três papéis e zero trabalho | Sim |
| Quadro:455/456 seleção1/default1, :458/:461 erros exatos, :462 somenteGET | FIN-16/17, campos obrigatórios com finais somente | Sim |
| Quadro:478/479 criação1..3/default3; :482/483 edição1..2/default2; :484/485 posições1/2; :487 somenteGET | FIN-16, posições somente trabalhos | Sim |
| Quadro:495 nomes homônimos duplicados; :498–505 setor/posição/controles/editor exatos; :510/511 tag/botões oficiais; :513 somenteGET | FIN-15/16 e edge homônimos | Sim |
| Quadro:520 IDs4/5, :521 tag ausente, :522 botão legado, :523 Setor Campo, :524 somenteGET | FIN-15/29 e done when sem síntese/categoria por nome | Sim |
| Quadro:543/545 bloqueio; :548 métodos GET/mutação/GET; :550–552 identificação/ausências; :554 foco Nova | FIN-15/19, quatro casos de cache/refresh atual e foco | Sim |
| Criação:43/48 aviso exato/payload vazio; :54/55/57 textos, :59/66 sem linhas sintéticas, :69 payload inteiro | FIN-18, sem/com trabalho e nomes homônimos | Sim |
| Assertions anteriores de teclado, pendência, erro, arquivo/permissão/versão, setores e campos de trabalho | FIN-19 e regressão exigida pelo done when, conservadas com o novo par | Sim |

Adequação PASS: treze casos novos necessários, nenhum shallow-only; payloads/IDs/textos/contagens/controles têm valores exatos. Guardas adicionais inspecionadas em Quadro.jsx:68/75/89/109, incluindo categoria do snapshot atual (:30); bloqueio do editor/diálogo em :154/164. Os testes comprovam o resultado sem mutação após refresh, junto da proteção visual. Padrão Vitest/Testing Library/teclado do repositório e matriz React de tasks.md seguidos. Todos os 222 casos anteriores preservados; somente expectativas de listas/vazio afetadas pelo contrato novo foram adaptadas, com assertions finais extras. Sem exclusões/skips/SPEC_DEVIATION. T7/MySQL TEMP/Verificador/UAT humano continuam separados.

## T7: integração de navegador e fechamento operacional

Pré-plano: adaptar os três E2E existentes sem remover cenários/assertions de trabalho/teclado/auth/arquivo/reload. Campos inclui categoria; expectativa exata TRABALHO para trabalhos e par CONCLUIDA/Concluídos, CANCELADA/Cancelados, Setor null, IDs distintos próprios, contagem0. Preservar os IDs e todos os campos originais das finais em cada snapshot de CRUD/reload/arquivo/restauração. Testar aviso/payload só trabalhos, posições sem finais, zero trabalho e remoção do último trabalho. Arquivos: três E2E, docs/OPERACAO.md, design.md (correção administrativa sete→oito tarefas com T8), spec/tasks/evidence. Sucesso: 235 Vitest, build em TEMP novo, lint, três Edge/H2 zero retries/skips, evidência MySQL TEMP e adequação antes do commit. Nenhum backend/ação de demanda/STATE alterado.

Gate Build PASS em 2026-10-06:

- `npm.cmd test`: **235/235, 11 arquivos, zero falhas/skips**, 34,82s. Sem casos React novos nesta tarefa.
- `npm.cmd run build -- --outDir C:/Users/Marco/AppData/Local/Temp/creral-etapas-finais-T7-6c1e1b3a8baa40e1bfbff29b77eea334/build`: PASS, 23 módulos; diretório novo fora do projeto, frontend/dist preservado.
- `npm.cmd run lint`: PASS antes do primeiro E2E e novamente depois da assertion do último trabalho.
- `npm.cmd run test:e2e`: **3/3 Edge/H2 PASS**, cada arquivo em banco novo, zero retries/skips; JDK `C:/Program Files/Java/jdk-25.0.2` e Maven `C:/Users/Marco/apache-maven-3.9.16/bin` no PATH explícito. Primeiro gate 3/3 PASS; acrescentado o edge GER de exclusão do último trabalho/restauração, seguido do gate final completo: AUT2,8s, ETA6,1s, GER4,7s. Portas próprias4173/18082 sem listener após encerramento do helper.

Logs de todos os gates em `C:/Users/Marco/AppData/Local/Temp/creral-etapas-finais-T7-6c1e1b3a8baa40e1bfbff29b77eea334`: vitest.log, build.log, lint-final.log e e2e-final.log. Capturas do gate final: `C:/Users/Marco/AppData/Local/Temp/creral-etapas-e2e-7VuYvB` (finais/editor/confirmacao/ativo/arquivado desktop/mobile, dez PNG) e `C:/Users/Marco/AppData/Local/Temp/creral-gerenciamentos-e2e-pFvFxE` (ativos/arquivado desktop/mobile, quatro PNG).

QA visual executado pelo root com view_image: **12 capturas desktop/mobile PASS**, sem ajuste visual necessário. Seis capturas finais/editor/arquivado de `creral-etapas-e2e-bTe8PI` (primeiro gate, mesmo código ETA do gate final); quatro confirmacao/ativo de `creral-etapas-e2e-7VuYvB`; duas arquivado de `creral-gerenciamentos-e2e-pFvFxE`. Texto legível, tag/contagens oficiais sem Setor/posição/controles, editor/confirmacao/foco e consulta arquivada preservados, sem overflow horizontal. Nenhuma aprovação de UAT humano inferida dessas imagens.

### Adequação dos três E2E preservados

Nas tabelas seguintes, ETA = `frontend/e2e/etapas.spec.js`, GER = `frontend/e2e/gerenciamentos.spec.js`, AUT = `frontend/e2e/autocadastro.spec.js`. Os objetos de expectativa das finais são escritos com todos os valores literais, somente IDs vindo da primeira resposta e comprovados inteiros/positivos/distintos. O `campos` inclui ID, nome, Setor, ordem, categoria e contagem. Em ETA as ordens originais1/2 das finais permanecem mesmo quando os trabalhos mudam, com ordenação por categoria no snapshot.

| AC/done when/edge | Evidência + assertion | Resultado da spec | Cobertura |
| --- | --- | --- | --- |
| FIN-01 sem trabalhos persiste par exato | AUT:82 `expect(etapasIniciais).toHaveLength(2)`; :87 `expect(etapasIniciais[0].id).not.toBe(etapasIniciais[1].id)`; :92 `expect(etapasIniciais).toEqual(finaisEsperadas)`, objeto :88–91 fixa nomes/categorias/Setor null/ordens1,2/contagens0; ETA:100 `expect(semTrabalhos.etapas.map(campos)).toEqual(finaisEsperadas)` com objeto :96–99 igual | exatamente duas oficiais persistidas, IDs próprios distintos | Sim |
| FIN-02 trabalho preservado + duas finais | GER:64 `expect(etapasIniciais).toHaveLength(3)`; :70 `expect(etapasIniciais.map(campos)).toEqual([{ id: etapasIniciais[0].id, nome: "Planejamento", setor: "Técnico", ordem: 1, categoria: "TRABALHO", quantidadeDemandas: 0 }, { id: etapasIniciais[1].id, nome: "Concluídos", setor: null, ordem: 2, categoria: "CONCLUIDA", quantidadeDemandas: 0 }, { id: etapasIniciais[2].id, nome: "Cancelados", setor: null, ordem: 3, categoria: "CANCELADA", quantidadeDemandas: 0 }])` | trabalho enviado e par oficial próprios | Sim |
| FIN-03 finais vinculadas ao quadro consultado, identidade independente | GER:65 `expect(new Set(etapasIniciais.map(etapa => etapa.id)).size).toBe(3)`; AUT:109/:139 mesma igualdade `expect((await estrutura(...)).etapas).toEqual(finaisEsperadas)` pelo ID do quadro em :77; prova HTTP entre dois quadros já em `sistema/src/test/java/com/patp/sistema/GerenciamentoFinaisTests.java:43` `assertThat(par).extracting(e -> e.getGerenciamento().getId()).containsExactly(...)`, :45 `assertThat(sem).extracting(Etapa::getId).doesNotContainAnyElementsOf(com.stream().map(Etapa::getId).toList())`, também executada na seleção MySQL | identidades distintas, mesmos IDs nos reloads do quadro correto; separação entre quadros comprovada por HTTP/persistência | Sim |
| FIN-07 campos/categorias/contagens reais no snapshot | ETA:100 par exato; :160 `expect(tripla.etapas.map(campos)).toEqual([...finaisEsperadas])` com três trabalhos completos :161–163; :104 ``expect(coluna.getByRole("heading")).toHaveAttribute("id", `etapa-titulo-${final.id}`)``; :106 `expect(coluna.getByText("0 demandas", { exact: true })).toBeVisible()` | DTO persistido e IDs/contagem da interface coincidem | Sim |
| FIN-08 trabalhos primeiro, finais no fim | ETA:168 `expect(page.getByRole("region").getByRole("heading")).toHaveText(["Planejamento", "Análise", "Execução", "Concluídos", "Cancelados"])`; :181 mesma lista com Entrega primeiro; :193 mesma lista com Entrega por último; :221 lista exata de dois trabalhos + par | categoria define posição final, trabalhos seguem suas posições | Sim |
| FIN-11 CRUD preserva IDs e todos os campos das finais | ETA:138/:147/:160/:184/:196/:226 `expect(snapshot.etapas.map(campos)).toEqual([...finaisEsperadas])` com campos de cada trabalho esperados explicitamente; :170 `expect((await consultar()).etapas.map(campos)).toEqual(tripla.etapas.map(campos))`; GER:164 `expect(semTrabalho.etapas.map(campos)).toEqual(etapasIniciais.slice(1).map(campos))` | criar/reordenar/remover não muda par; último trabalho removido conserva exatamente as duas finais originais | Sim |
| FIN-15 oficiais sem Setor/posição/ações | ETA:105 `expect(coluna.getByText("Etapa final obrigatória", { exact: true })).toBeVisible()`; :107 `expect(coluna.getByText(/Setor responsável:/)).toHaveCount(0)`; :108 `expect(coluna.getByText(/^Posição /)).toHaveCount(0)`; :109 `expect(coluna.getByRole("button")).toHaveCount(0)`; helper chamado em ativo/reload/arquivo | tag exata e controles ausentes em ambas oficiais | Sim |
| FIN-16 somente posições de trabalhos | ETA:78 `expect(page.getByLabel("Posição", { exact: true }).locator("option")).toHaveText(Array.from({ length: quantidadeTrabalhos + 1 }, (_, index) => String(index + 1)))`, chamadas :135/:144/:154 com N0/1/2; :128 seleção `.toHaveText(["1"])`; :176 seleção `.toHaveText(["1", "2", "3"])` na edição de três trabalhos | criação1..N+1, edição1..N, ignorando as finais | Sim |
| FIN-17 zero trabalho inicial/depois da última remoção/reload | ETA:121 `expect(page.getByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada", exact: true })).toBeVisible()`; :122 headings `.toHaveText(["Concluídos", "Cancelados"])`; GER:157/:167 mesmo aviso depois de remover Planejamento/recarregar, :158 headings exatamente as finais, :164 campos preservados | aviso exato e par permanece | Sim |
| FIN-18 aviso automático e payload só trabalho | AUT:63 `expect(page.getByText("As etapas finais Concluídos e Cancelados são criadas automaticamente.", { exact: true })).toBeVisible()`; :70 `expect(criacao.request().postDataJSON()).toEqual({ nome: "Quadro após cadastro", descricao: "Dados fictícios do autocadastro.", etapas: [] })`; GER:45 `expect((await envioCriacao).postDataJSON()).toEqual({ nome: "Instalação de postes", descricao: "Quadro fictício para teste do CRUD.", etapas: [{ nome: "Planejamento", setor: "Técnico", ordem: 1 }] })` | vazio e trabalho real; nenhuma final/ID/categoria enviada | Sim |
| FIN-19 arquivo/cache/versão/permissões existentes | ETA:237 `expect((await arquivoRequest).postDataJSON()).toEqual({ versao: 6 })`; :249 metadata `.toMatchObject({..., arquivado: true, versao: 7 })`; :242–245 Nova/Editar/Remover/Criar processo `.toHaveCount(0)`; :250 `expect(arquivada.etapas.map(campos)).toEqual(removida.etapas.map(campos))`; GER:135/:145 nomes/criador/arquivo/versões2,3 preservados; permissões/falhas permanecem nos casos React/HTTP de T4/T6 | arquivo readonly, versão atual sem recarregar lista, restauração preserva par/trabalhos | Sim |
| FIN-29 consultas/reload não fazem manutenção ou versão nova | GER:171 `expect(await semTrabalhoReload.json()).toEqual(semTrabalho)` com versão4/campos exatos em :163; legado sem preparação em `sistema/src/test/java/com/patp/sistema/EtapaFinaisApiTests.java:114` `assertThat(estado()).isEqualTo(antes)`, executado também na seleção MySQL; script físico :39/:40/:41/:49 abaixo | snapshot igual após reload; GET/startup legado preserva dados | Sim |
| Done when três cenários/teclado/persistência preservados | ETA:131/:207/:215 `expect(controle).toBeFocused()`; :216/:225 métodos exatos sem gravação ao cancelar; AUT:52/:55/:119/:125/:135 identidade/cache/saída/login exatos; GER:107 foco ao cancelar arquivo, :149 `expect(etapasRestauradas.map(campos)).toEqual(etapasIniciais.map(campos))` | todos os cenários anteriores continuam, sem skip/retry/exclusão | Sim |

| Assertions concretas | Maps to | Manter |
| --- | --- | --- |
| ETA:88–100 tamanho/IDs/objeto finais exatos; :104–109 IDs/tag/contagem/Setor/posição/controles | FIN-01/03/07/15 | Sim |
| ETA:78/128/176 opções; :138/147/160/184/196/226 snapshots completos com par original; :168/181/193/221/254 headings na ordem exata | FIN-02/07/08/11/16 | Sim |
| ETA:121/122 aviso/pair; :131/133/:207/:215/:216/:225 foco/métodos; :237/242–250 arquivo/versão/snapshot; :258 igualdade após reload | FIN-17/19/29 e regressão de teclado/CRUD | Sim |
| GER:31 aviso, :45 payload exato, :64–73 cardinalidade/IDs/trabalho/par, :78–81 identificação/proteção | FIN-02/03/07/15/18 | Sim |
| GER:122/:149 snapshots iguais em arquivo/restauração, :157–164 último trabalho removido/aviso/par/version4, :167/:171 reload igual | FIN-11/17/19/29 | Sim |
| AUT:63/:70 aviso/payload vazio, :82–102 par persistido e render exato, :109/:139 reload/login com mesmos IDs/campos | FIN-01/03/07/15/17/18/29 | Sim |
| Assertions anteriores de cadastro/login/cache, setores/posições/versões, arquivo/restauração, cancelamento/teclado e largura desktop/mobile | FIN-19 e preservação dos três cenários exigida pelo done when | Sim |

Adequação PASS: nenhum caso antigo excluído/enfraquecido/skipped; apenas listas/vazio autorizados foram adaptados, com campos/categorias/finais adicionais exatos. As assertions inspecionam respostas persistidas, payloads completos e estado renderizado, não somente chamadas. Cada campo de trabalho/final e cada campo dos payloads foi conferido separadamente no objeto literal das expectativas. Três cenários necessários, com edge adicional de último trabalho/removal/reload. Padrões Playwright/Edge/teclado e matriz de tasks.md seguidos; retries0/configuração existente preservados. Sem SPEC_DEVIATION. OPERACAO contém roteiro Comprar poste + par após recarga e limites somente trabalho; ações de demandas continuam fora deste corte.

### Gate MySQL TEMP registrado pelo root

Relatório completo lido em `C:/Users/Marco/AppData/Local/Temp/creral-etapas-finais-3b48b05a57314180a8f952ea6fa43d07/mysql-evidence.md`, backend efcdbdf, MySQL Community8.0.43/Java25. Root executou **30 testes selecionados PASS, zero falhas/erros/skips** (par4, criação5, HTTP8, preparação12, GET legado1). O H2 completo de T5 continua **278/278**; os 30 selecionados não substituem nem são somados como nova suíte completa. Log mysql-selected-verify.log na mesma pasta.

Schema antigo criado pelo jar anterior, sem categoria: **17 registros em seis tabelas**. Startup normal no schema fictício acrescentou somente `VARCHAR(20) NULL`, preservou os quatro trabalhos com categoriaSQLnull e todos os campos antigos, sem preparação. CLI plano conservou snapshot integral e reportou zero gravações. Aplicar criou seis finais e mudou apenas duas referências de status exatos Concluido/Cancelado, preservando dados/filhos/trabalhos/arquivo/criador; versões4/8/2 foram a5/9/3 uma vez. Repetição retornou zero criações/movimentos, snapshot integral/IDs/versões iguais; enviar create-drop à CLI comprovou validate forçado sem apagar schema. MySQL TEMP próprio foi encerrado com shutdown0; nenhum banco configurado foi operado.

Evidência-or-zero física: script `C:/Users/Marco/AppData/Local/Temp/creral-etapas-finais-3b48b05a57314180a8f952ea6fa43d07/legacy-check.py`, executado pelo root com exit0 em cada modo:

| Gate físico/AC | Linha + assertion | Resultado | Cobertura |
| --- | --- | --- | --- |
| Schema VARCHAR20 nullable, FIN-06/07/29 | :43 `assert column == [{'DATA_TYPE': 'varchar', 'CHARACTER_MAXIMUM_LENGTH': '20', 'IS_NULLABLE': 'YES'}], column`; :39 `assert {key: new[key] for key in old} == old, (table, old['id'])`; :40/41 `assert len(current['etapas']) == 4` / `assert all(row['categoria'] == 'NULL' for row in current['etapas'])` | coluna correta, campos antigos intactos, sem preparação | Sim |
| Plano/reexecução, FIN-20/26 | :49 `assert current == before`; :80 `assert current == first` | todas as seis tabelas e versões iguais | Sim |
| Par/preservação/versão, FIN-21/23/24/28 | :55 `assert len(current['etapas']) == 10`; :57 `assert current[table] == before[table], table`; :59 quadro exceto versão igual; :60 `assert int(new['versao']) == int(old['versao']) + 1, old['id']`; :63 todos os campos de trabalhos iguais; :66 `assert sorted((row['categoria'], row['nome'], row['setor']) for row in finals) == [('CANCELADA', 'Cancelados', 'NULL'), ('CONCLUIDA', 'Concluídos', 'NULL')], finals` | seis finais próprias, 17 registros/filhos anteriores preservados, versão+1 por quadro | Sim |
| Referência exata/desconhecidos, FIN-22/23/25 | :68 demanda exceto etapa_id igual; :71 `assert stages[new['etapa_id']]['categoria'] == expected`; :72 `assert stages[new['etapa_id']]['gerenciamento_id'] == stages[old['etapa_id']]['gerenciamento_id']`; :74 `assert new['etapa_id'] == old['etapa_id'], old['id']` | só Concluido/Cancelado mudam para final do mesmo quadro; acento/null preservados | Sim |

Todas essas assertions físicas mapeiam aos gates/ACs indicados e são necessárias ao T7; nenhuma nova operação MySQL foi executada pelo worker frontend. Dados/scripts/logs TEMP preservados. Verificador independente novo será despachado pelo root após o commit T7, com relatório próprio; o worker não escreveu validation.md. UAT humano permanece pendente.
