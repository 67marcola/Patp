# CRUD de etapas de trabalho: design

**Spec:** `spec.md`. **Status:** decisão técnica definida dentro da continuação local autorizada.

## Architecture Overview

EtapaService administra o snapshot de configuração sob o lock já existente do gerenciamento. O servidor verifica identidade atual, quadro, permissão, arquivamento, etapa, versão e campos antes de gravar. POST/PUT/DELETE retornam snapshot JSON completo, inclusive DELETE200, evitando uma confirmação vazia seguida de parser JSON ou consulta adicional.

Alternativas consideradas: (1) versão do quadro existente, escolhida por não exigir schema novo; (2) revisão estrutural separada, exigiria modelo/coluna e protocolo adicional; (3) somente lock, insuficiente para detectar tela antiga. O usuário não precisa escolher implementação de transações. A consequência da opção1 está explícita: configurar etapas também atualiza a versão usada para editar/arquivar o quadro.

## Code Reuse Analysis

| Componente | Uso |
| --- | --- |
| SessaoService | Resolver usuário persistido por token em cada operação. |
| GerenciamentoGuard | Lock/ativo existentes; acrescentar política administrativa reutilizada também por GerenciamentoService, sem mudar exigirAtivo das demandas. |
| GerenciamentoResponse | Metadados da configuração, atualizando lista/Quadro com o mesmo DTO. |
| VersaoDeserializer / OrdemDeserializer | Números inteiros estritos; não aceitar string/decimal/bool/overflow. |
| ApiExceptionHandler | Erros JSON previsíveis e 500 genérico. |
| api.js, keyboard helper, preview isolado | Clientes sem retries de mutação, tests RTL/Edge com Tab/Enter, H2 explícito. |

## Components

- `EtapaController`: novo GET estrutura-etapas; GET array anterior preservado; mutações com Authorization e versão; POST201/PUT200/DELETE200 todos com configuração JSON.
- `EtapaService`: consulta snapshot, CRUD, validação, reordenação e ocupação atômicos. Guardar ordem determinística antes de inserir/mover/remover; renumerar 1..N. Flush e incremento explícito único da versão ao confirmar.
- `ConfiguracaoEtapasResponse` / `EtapaResponse`: snapshot `{gerenciamento,etapas}` e resumo `{id,nome,setor,ordem,quantidadeDemandas}`.
- Repositórios: ordenar por ordem/ID e contar demandas por etapa após bloquear quadro. Preferir consulta agrupada para contagens do snapshot; `existsByEtapaId` para remoção.
- `EditorEtapa.jsx`: formulário nome/setor/posição com rascunho, validação, pending/ref, erro acessível e callbacks de salvar/cancelar.
- `Quadro.jsx`: snapshot atual, controles permissionados, confirmação, atualização/retryGET e retorno de foco; contagens reais sem falso texto de vazio.
- `Gerenciamentos.jsx`: receber metadados atualizados do Quadro para versão/cache corretos ao voltar e arquivar.

Na edição de legado, Quadro passa ao formulário a posição visual da etapa (`index + 1` da sequência consultada). Assim o select 1..N também funciona quando a ordem persistida é 7/7 ou contém lacunas. O snapshot conserva os números antigos e abrir/cancelar o editor não grava nada; a próxima mutação confirmada aplica a normalização prevista.

## Data Models

Nenhuma mudança de tabela/coluna. Reusar Gerenciamento.@Version. Snapshot protegido pelo mesmo lock dos escritores evita combinar versão velha com etapas novas. Demandas e históricos conservam IDs/valores; nomenclatura não classifica categoria final.

## Error Handling Strategy

400 validação/formato; 401 sessão; 403 terceiro; 404 quadro/etapa não vinculada; 409 arquivo/versão/ocupação; 500 persistência com rollback. UI conserva rascunho e erro, permite GET de atualização em conflito/comunicação, nunca repete mutação automaticamente. DELETE responde200 com JSON, não200 vazio.

## Risks & Concerns

| Concern | Local | Mitigação |
| --- | --- | --- |
| Permissão administrativa aplicada a todos os filhos | GerenciamentoGuard.exigirAtivo | Não mudar política das demandas/comentários; usar guarda administrativa separada somente para configuração. |
| Ordens empatas/gaps e setor nulo antigos | Etapa/model/repository | GET preserva; escrita reorganiza somente ordens; não inventar setor. |
| Tela antiga sobrescreve sequência nova | EtapaService | Lock + comparação de versão; incrementar quadro e atualizar cache UI. |
| Etapa removida enquanto recebe demanda | ProcessoService/EtapaService | Ambos bloqueiam quadro; conferir ocupação dentro do lock; destino que foi removido responde404 sem vínculo órfão. Tradução404 é acompanhante de consistência, sem novas regras de mover/finalizar. |
| N+1 de contagens | Consulta snapshot | Consulta de contagens agrupadas ou alternativa mínima justificada pelo trabalhador, sem carregar entidades de todas as demandas. |
| Fixture antiga afirma terceiro pode editar/ordem fora do limite | EtapaArchiveTests | Adaptar contratos legitimamente substituídos por AD-007/009. Preservar cenário/count/as assertions de integridade; acrescentar negações novas. Não apagar/ignorar testes. |
| Arquivar com versão0 depois de configurar etapas | EtapaArchiveTests/Gerenciamentos cache | Novo contrato força conflito na solicitação antiga; adicionar arquivo com versão atual e prova de serialização. |
| Contagens fixas e cache antigo | Quadro/Gerenciamentos | Snapshot real e callback de metadados; não renderizar falso estado vazio ocupado. |

## Tech Decisions

Testes antigos de contratos deliberadamente substituídos serão atualizados apenas conforme decisões já autorizadas, sem reduzir precisão: sucesso usa criador/admin; posições válidas são preparadas; versão atual torna o arquivo possível e versão antiga é recusada. Assertions antigas de rollback/readonly/status das demandas continuam aplicáveis. Nenhuma revisão de teste é usada para acomodar bug de implementação.

Não instalar nova biblioteca; todos os mecanismos estão no código existente. MySQL real permanece fora do teste. Verificador novo após T5, ≥5 mutações por autorização/integridade, somente scratch.
