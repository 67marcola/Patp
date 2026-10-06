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
