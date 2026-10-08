# Edição de demandas Design

**Spec:** spec.md. **Status:** autorizado para implementação local; padrões do projeto e AD-023–026.

## Architecture Overview

Reutilizar o fluxo do quadro: formulário → API dedicada com versão → serviço transacional com lock do gerenciamento → demanda/histórico/versão → snapshot confirmado. A rota antiga delega ao mesmo núcleo de edição, mantendo seu retorno de entidade. Não adicionar tabelas, bibliotecas ou migração.

## Code Reuse Analysis

| Componente | Uso |
| --- | --- |
| GerenciamentoGuard | Lock, quadro ativo e criador/admin. |
| ProcessoService | Normalização, limites, sessão, incremento e resposta pelas etapas. |
| VersaoDeserializer/DataDemandaDeserializer | Tipos JSON estritos já usados no cadastro. |
| HistoricoService | EDICAO com autor persistido da sessão. |
| api.js | Requisição, erros e snapshotValido; novo verificador de campos e ciclo. |
| EditorDemanda/Quadro | Modo edição e rascunho, ocupação global, atualização manual, cancelamento e foco. |

## Components

- EditarDemandaRequest: versao e sete campos informativos; desconhecidos recusados.
- ProcessoService.editarDados: autenticar, resolver demanda no quadro, lock, ativo/admin, versão, estado reconhecido, validar campos, salvar, registrar, flush e incrementar. Retorna snapshot.
- ProcessoService.editar: adaptador legado; mesmo núcleo, sem versão enviada; número idêntico/omitido aceito, diferente recusado. Ciclo divergente recusado antes da gravação.
- DemandaController PUT /{gerenciamentoId}/demandas/{demandaId}: DTO dedicado e resposta 200.
- api.editarDemanda: comparar resposta com dados normalizados e campos preservados, versão +1, quadro ativo/admin.
- EditorDemanda: recebe demanda opcional; edição preenche campos, número somente leitura; cadastro mantém contrato existente.
- Quadro: estado separado para demanda em edição, limitado ao quadro atual, ações Editar para status reconhecido ativo/admin.

## Data Models

Payload completo: `{versao,pessoa,responsavel,prioridade,dataEmissao,prazoEtapa,prazoGeral,observacoes}`. Número, ID, status, etapa e encerramentos não pertencem ao payload. Snapshot e entidade permanecem os atuais. Não alterar schema.

## Error Handling Strategy

Erros seguem ApiException/handler atuais. Falha de resposta/rede preserva rascunho e exige GET manual. Não trocar os valores digitados por GET posterior; usar a versão atualizada para novo envio manual. Nenhum retry automático. Ausência/externa 404; sessão 401; terceiro 403; arquivo/versão/status antigo 409; entrada inválida 400; falha de escrita 500 com rollback.

## Risks & Concerns

| Concern | Location | Mitigation |
| --- | --- | --- |
| Rota antiga permite terceiro e renumeração | ProcessoService.java:208 | Mesmo núcleo e bloqueio explícito; ajustar testes antigos somente após autorização específica. |
| PUT antigo não incrementa versão | ProcessoService.java:208 | Incrementar sob lock; testar conflito com ações do ciclo e arquivo. |
| Retorno 200 pode não confirmar dados | api.js:97 | Exigir demanda, campos, ciclo e versão; testes de respostas falsas. |
| Estado do editor pode sobreviver à troca de quadro | Quadro.jsx:35 | Vincular ao ID atual, descartar na troca e ignorar resposta tardia. |
| Testes antigos enumeram ações de ciclo | QuadroTransicoes.test.jsx:44 | Incluir Editar nos três status, preservando todas as ações anteriores; sem alteração do caso desconhecido. |

## Tech Decisions

Reutilização do lock do quadro evita mecanismo concorrente novo. Versão é obrigatória na API nova, preservando compatibilidade de entrada da rota antiga. Correção informativa de encerradas conserva valores antigos, inclusive null/motivo longo. Estados desconhecidos permanecem somente consulta neste corte.
