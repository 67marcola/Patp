# Arquivamento de demandas Design

**Spec:** spec.md. **Status:** Draft técnico para revisão; decisões de produto fixadas.

## Architecture Overview

Recomenda-se marcador Boolean arquivado na própria demanda, separado de status/etapa. Alternativas para o mesmo alcance: tabela de arquivos separada (mais joins/sincronização) ou status Arquivado (exige guardar ciclo original em outro lugar). Marcador preserva vínculos e permite restauração literal, sem mover registros. Tecnologias atuais mantidas.

## Code Reuse Analysis

| Component | Location | Reuse |
| --- | --- | --- |
| Lock/permissão | GerenciamentoGuard | exigirAtivo/exigirAdministracao/bloquear. |
| Versão/JSON estrito | TransicaoVersaoRequest/VersaoDeserializer | Payload apenas versão; mesmas mensagens. |
| Snapshot | EtapaService/ConfiguracaoEtapasResponse/DemandaResponse | Mesmo DTO nas duas listas; contagem só ativas. |
| História | HistoricoService | Evento interno com autor/clock; método externo protegido separado. |
| UI | Quadro/AcaoDemanda/api.js | Pending global, focos, confirmação e GET manual. |
| Testes | ApiIntegrationSupport/DemandaEdicao*Tests/QuadroEdicao.test.jsx/e2e/edicao.spec.js | Comparação física, rollback CHECK, concorrência sincronizada, teclado/recarga. |

## Components

1. Processo: Boolean nullable arquivado default false; isArquivado considera null=false; JSON somente leitura. Cadastro sempre ativo.
2. ConfiguracaoEtapasResponse: demandasArquivadas adicional; construtor anterior delega arquivo vazio para compilação gradual. DemandaResponse mantém 14 campos exatos.
3. EtapaService.resposta: obter todas uma vez, dividir pelo marcador e contar só ativas. existsByEtapaId continua incluindo arquivo na proteção da remoção.
4. ProcessoService: núcleo comum arquivar/restaurar sob lock ativo/admin/versão, alvo correto, status reconhecido e repetição. Flag+evento+versão na mesma transação. Edição/transições verificam flag. DELETE legado autentica/admin/ativo e recusa409 sem apagar.
5. ComentarioService: após lock do quadro, recusar nova escrita no arquivo; leitura intacta.
6. HistoricoService: registrarExterno verifica marcador sob lock; registrar interno continua permitindo eventos administrativos durante arquivo. HistoricoController POST usa externo, inclusive quando nome de ação enviado for ARQUIVAMENTO.
7. DemandaController: PUT /{quadro}/demandas/{id}/arquivar e /restaurar, TransicaoVersaoRequest,200snapshot.
8. API frontend: cliente e confirmação por valor; alvo idêntico nos 14 campos e transferido entre listas, versão+1, permissões/contagens válidas. Impedir IDs duplicados entre listas.
9. AcaoArquivamentoDemanda: confirmação/foco/cancelar/pending/erro, sem alterar ciclo. Quadro integra botões e arquivo com dados completos; cards ativos permanecem com dados e ações anteriores.

## Data Models

processos.arquivado: Boolean nullable; null legado equivale a ativo sem gravação automática. Snapshot: {gerenciamento,etapas,demandas,demandasArquivadas}. Etapa_id e unicidade global permanecem. APIs recebem {versao}. GETs legados podem consultar ambas, com marcador explícito na entidade.

## Error Handling Strategy

Erros exatos da spec, nenhum retry. Recusas comparadas com snapshot físico inteiro e versão. Confirmar resposta antes de fechar diálogo. Cancelar possível após erro; GET manual conserva confirmação, mas desabilita alvo que deixou de ser elegível.

## Risks & Concerns

| Concern | Location | Impact | Mitigation |
| --- | --- | --- | --- |
| DELETE grava evento vinculado antes de apagar | ProcessoService.java:378 | Contornar política ou falhar500 | Recusa409 deliberada ativo/admin; teste zero efeitos preservado. |
| Snapshot conta/mistura todas | EtapaService.java:154 | Arquivo aparece nas colunas | Separar antes de contar; provar em consultas/mutações. |
| Escritas antigas desconhecem arquivo | ProcessoService.java:216/296; ComentarioService.java:35 | Alterar registro fora da UI | Guard do marcador sob lock e provas das rotas antigas. |
| Histórico manual usa registrar interno | HistoricoController.java:28 | Modificar arquivo e forjar evento | Método externo protegido sem bypass por nome de ação. |
| Coluna sem ativas ainda tem arquivo | EtapaService.java:105 | Perder referência de restauração | Manter exists sobre qualquer demanda e explicar vínculo no arquivo. |
| Gerados rastreados dirty | git status | Commit/descarte involuntário | Add explícito; sensor em scratch TEMP com hashes/porcelain antes/depois. |

## Tech Decisions

| Decision | Choice | Rationale |
| --- | --- | --- |
| Marcador | Boolean nullable separado | Preservar ciclo; null legado sem migração real. |
| Snapshot | Duas listas na mesma versão | Evitar consulta do arquivo com versão diferente. |
| História interna/externa | Guard externo separado | Eventos internos continuam sem abrir bypass do POST. |
| Gates | H2/MySQL fictícios; Vitest/Edge | Stack confirmada, persistência nos dois bancos, nenhum real. |
