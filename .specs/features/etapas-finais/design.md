# Etapas finais Design

**Spec:** spec.md. **Status:** escopo autorizado; escolhas técnicas locais declaradas. Profundidade média, oito tarefas incluindo T8 corretiva de schema, sem novo framework de migração.

## Architecture Overview

Etapa recebe enum persistido como string em coluna nullable, permitindo ler schema/valores legados sem inventar uma categoria pelo nome. Getter converte categoria nula em TRABALHO. EtapasFinaisService cria/verifica o par oficial e ordena por categoria, ordem e ID. GerenciamentoService inclui o par na transação inicial. EtapaService modifica somente a sequência de trabalhos, mantém o par e devolve categoria/contagens no snapshot atual.

PreparacaoEtapasFinaisService oferece plano e aplicação global transacional sob locks de quadros em ordem de ID. Um main próprio, PrepararEtapasFinais, exige primeiro argumento plano/aplicar antes de abrir contexto Spring sem servidor web. O startup normal não chama a ferramenta. Aplicação utiliza status exatos atuais e altera somente etapa da demanda; reporta as contagens e versões dos quadros alterados, sem chamar Histórico/ProcessoService.

Quadro distingue trabalhos e finais pelo DTO. Editor recebe quantidade de trabalhos. A tela de criação informa que finais são automáticas e não as envia. Nenhuma final é sintetizada no cliente.

## Code Reuse Analysis

| Componente | Reuso |
| --- | --- |
| GerenciamentoGuard/Repository | Lock de quadro e permissões normais; manutenção usa lock sem liberar ações normais arquivadas |
| EtapaRepository | Etapas persistidas por quadro/ordem/ID |
| ProcessoRepository | Contagens e leitura para inventário; acrescentar consulta por quadro se necessária |
| EntityManager | Incremento de versão existente, exatamente uma vez por quadro alterado |
| Quadro/EditorEtapa | Snapshot/versão, estado pendente, foco e erro existentes |
| ApiIntegrationSupport e helper | H2 e Edge isolados já configurados |

## Data Models

CategoriaEtapa: TRABALHO, CONCLUIDA, CANCELADA. Etapa.categoria string nullable; default Java TRABALHO, legado SQL null lido como TRABALHO. DTO acrescenta categoria. Não usar unicidade (quadro,categoria), pois há vários trabalhos; garantia de uma final por tipo combina lock e verificação do par. Finais com nome oficial, Setor null e ordem após trabalhos; ordenação por categoria prevalece sobre ordens antigas preservadas.

## Error Handling Strategy

CRUD mantém 400/401/403/404/409 atuais. Tentativa de editar/remover final: 409, `Etapas finais obrigatórias não podem ser alteradas.`. Cliente não escolhe categoria final nem ID inicial. Preparação recusa duplicidade explícita e reverte toda a execução; relatório não inventa timestamps/eventos históricos. Main inválido não abre banco. Nenhum retry de aplicação por API/browser.

## Risks & Concerns

| Concern | Location | Impact | Mitigation |
| --- | --- | --- | --- |
| Status/etapa dissociados | ProcessoService.java:256/286 | Novos encerramentos continuam legado até entrega dos fluxos | Out of Scope explícito, não prometer invariância permanente; transições serão próxima entrega |
| Categoria inicial via entidade | GerenciamentoService.java:45 | Cliente poderia criar final ou reutilizar ID | Rejeitar categoria final/ID antes de salvar qualquer quadro |
| Homônimo e ordens antigas | Etapa.java:20/26 | Inferência por nome/renumeração perde significado | Categoria explícita e preservação das etapas antigas na manutenção |
| Quadros arquivados | GerenciamentoGuard.java:37 | Migração por serviço de ação normal bloqueia ou fabrica eventos | Componente próprio de manutenção, sem alterar guard normal |
| Testes com listas antigas | GerenciamentoApiTests/EtapaApiTests/etapas.spec.js | Novo par altera cardinalidade | Asserts exatos adicionais para finais; manter cenários e valores dos trabalhos |
| ddl-auto da aplicação normal | docs/OPERACAO.md | Startup normal pode acessar DB real | Não iniciar nesta sessão; documentar adição de coluna/revisão e comando explícito do operador |

## Tech Decisions

| Decision | Choice | Rationale |
| --- | --- | --- |
| Preparação | Serviço JPA + main explícito, sem runner/GET | Reusa transações/locks/testes e deixa operação controlável |
| Schema | Coluna VARCHAR nullable e enum STRING | Compatibilidade de legados; MySQL TEMP deve verificar atualização |
| Estados antigos | Só igualdade exata Concluido/Cancelado | Evita interpretação de variantes desconhecidas; inventário mostra os demais |
| Versão | Uma vez para cada quadro realmente alterado | Evita snapshot cliente obsoleto sem incrementar em reexecução |
