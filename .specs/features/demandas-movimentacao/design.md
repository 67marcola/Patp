# Movimentação de demandas Design

**Spec:** spec.md. **Status:** definido conforme autorização persistente e AD-022.

## Architecture Overview

Reutilizar as transações e o lock do gerenciamento. Uma rotina de transição em ProcessoService valida sessão, quadro persistido, permissão, versão, estado e destino antes de gravar demanda, evento e incremento de versão. As APIs tipadas e antigas chamam essa rotina. A edição genérica não altera o ciclo de vida.

Escolha técnica proposta: serviço compartilhado, sem duplicar regras por controller. Regras duplicadas facilitariam divergências entre APIs; criar um motor de fluxo separado acrescentaria estrutura desnecessária para quatro ações. Esta proposta não amplia o escopo e segue a autorização local já recebida.

## Code Reuse Analysis

| Component | Location | How to Use |
| --- | --- | --- |
| Sessão e permissão | SessaoService/GerenciamentoGuard | Resolver usuário persistido, exigir quadro ativo e criador/admin. |
| Lock e versão | GerenciamentoGuard/EtapaService | Adquirir lock de quadro, comparar versão e compor snapshot coerente. |
| Categoria persistida | CategoriaEtapa/EtapaRepository | Identificar trabalho/finais por categoria, nunca por nome. |
| Eventos | HistoricoService/Historico | Registrar autoria da sessão e dataHora do novo evento na mesma transação. |
| Validação estrita | VersaoDeserializer/CriarDemandaRequest | DTOs de transição rejeitam propriedades extras e versões inválidas. |
| Formulários e cache | EditorDemanda/Quadro/api.js | Reutilizar busy, rascunho, foco, atualização manual e isolamento entre quadros. |

## Components

- ProcessoService: rotina de ação e wrappers legados; preserva o lock global→quadro da criação. Transições precisam somente do lock de quadro e não adquirem o global depois dele.
- DemandaController: quatro PUTs /api/gerenciamentos/{quadroId}/demandas/{demandaId}/{mover|concluir|cancelar|reabrir}; resposta200 snapshot.
- DTOs: mover/reabrir recebem {versao,etapaId}; concluir {versao}; cancelar {versao,motivo}. IDs positivos e versões estritas; nenhuma entidade, autor ou data recebidos.
- api.js: confirmar HTTP200, snapshot completo, ID da demanda, categoria/destino correto, versão+1 e status/campos atuais esperados. Não alterar cadastroConfirmado.
- AcaoDemanda: diálogo acessível, destino de trabalho ou motivo, confirmação de conclusão, pending, validação e rascunho.
- Quadro: botões por permissão/estado; compartilha bloqueio e cache; snapshot antigo não altera quadro novo.

## Data Models

Sem novos registros de domínio. Historico.descricao passa de VARCHAR255 para LONGTEXT para preservar motivos antigos e nomes de etapas completos. Documentar alteração de schema e testá-la somente em H2/MySQL fictícios. Não iniciar aplicação normal para aplicar ddl-auto.

A descrição de REABERTURA inclui status anterior, etapa anterior, etapa destino, dataConclusao, dataCancelamento e motivoCancelamento anteriores, com campos identificados e valores null explícitos. Eventos anteriores permanecem intactos. O timestamp dessa descrição é o da reabertura; as datas antigas são valores preservados, não eventos fabricados.

## Error Handling Strategy

| Error Scenario | Handling | User Impact |
| --- | --- | --- |
| Sem sessão / sem permissão | 401 / 403; nenhuma escrita | Erro explicável e formulário preservado. |
| Quadro arquivado, versão antiga, repetição ou final ausente | 409; nenhuma escrita | Atualizar/restaurar/preparar conforme mensagem. |
| Destino inválido/final e motivo inválido | 404 / 400; nenhuma escrita | Corrigir a seleção ou o motivo. |
| Falha na gravação | Rollback integral e erro500 existente | Nenhum sucesso parcial ou retry automático. |
| Resposta2xx incorreta | ApiError; manter diálogo e exigir GET manual | Não ocultar falha com fechamento prematuro. |
| Status desconhecido |409 sem efeitos | Manter consulta, informar correção explícita. |

## Risks & Concerns

| Concern | Location | Impact | Mitigation |
| --- | --- | --- | --- |
| Ações atuais não verificam admin nem destinos finais | ProcessoService.java:294 | Movimento indevido e incoerência status/coluna | Rotina compartilhada e testes de permissão/estado/rollback. |
| PUT genérico altera status/etapa | ProcessoService.java:204 | Contorna cancelamento, motivo e reabertura | Recusar alteração de ciclo; preservar campos omitidos. |
| Histórico limitado a255 | Historico.java:25 | Falha ou perda de motivo/nome longo | LONGTEXT, teste integral e operação documentada. |
| Testes antigos aceitam movimento de terceiro | ProcessoArchiveTests.java:87, HistoricoArchiveTests.java:52, EtapaDemandConcurrencyTests.java:45 | Contradizem AD-012 | Adaptar cenários às escolhas explícitas, manter asserts de integridade e acrescentar403 sem efeitos; sem skip/delete. |
| API retorna2xx sem confirmar ação | api.js:16 | Fecha diálogo apesar de resultado incorreto | Confirmação correlacionada a payload e assertions de valores. |
| Legado com status desconhecido | PreparacaoEtapasFinaisService.java | Classificação sem escolha do usuário | AD-022: bloquear transições, leitura preservada, nenhuma classificação automática. |

## Tech Decisions

| Decision | Choice | Rationale |
| --- | --- | --- |
| Atomicidade | Demanda+evento+versão numa transação READ_COMMITTED | Padrão persistido e proteção contra gravação parcial. |
| Repetição |409 sem efeitos | Cada evento corresponde a uma alteração real. |
| Compatibilidade | API antiga200 entidade, versão também incrementa | Snapshot antigo deve ficar desatualizado após qualquer transição. |
| Limites | Novo motivo10000 UTF-16; históricos antigos sem truncamento | Limite coerente com observações e preservação retroativa. |

