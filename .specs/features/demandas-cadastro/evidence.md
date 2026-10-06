# Cadastro e leitura de demandas: evidências

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
