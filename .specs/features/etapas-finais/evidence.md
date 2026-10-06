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

