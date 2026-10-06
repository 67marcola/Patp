# Cadastro e leitura de demandas Specification

**Base:** 28b8c88. **Data:** 2026-10-06. **Status:** implementação e verificação independente PASS,41/41 critérios. Fonte final926c862; teste de uso humano separado e ainda pendente.

## Problem Statement

Criar processo não tem ação e o quadro mostra contagens sem carregar demandas. A criação antiga recebe entidade completa, permitindo escolher etapas/estado e não valida os campos escolhidos pelo usuário. Esta entrega conecta cadastro e consulta com dados persistidos.

## Goals

- [x] Cadastrar uma demanda por formulário e API, inicialmente na primeira etapa de trabalho.
- [x] Exibir cartões reais e preservar dados após recarga/configuração/arquivo.
- [x] Garantir obrigatórios, unicidade global, transação, autorização e tratamento de erros.

## Out of Scope

| Item | Motivo |
| --- | --- |
| Editar/excluir demanda | Permissões e preservação ainda precisam de escolhas próprias |
| Mover/pular/concluir/cancelar/reabrir | Próxima entrega conforme AD-011–015; não declarar coerência permanente nas APIs antigas |
| Comentários, logs de gerenciamento e gráficos | Entregas próprias; preservar histórico de criação existente |
| MySQL configurado, contas reais, migração, push/deploy | Somente implementação local/testes fictícios autorizados |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Criar/etapa inicial | Todos autenticados, quadro ativo, primeira TRABALHO ordem/ID | AD-017/018 | Sim |
| Número e obrigatórios | Manual único global; número e pessoa obrigatórios | AD-019/020, resposta1A/2A | Sim |
| Estado/datas finais | Em andamento; encerramento/motivo null | Cadastro não executa ações finais | Default técnico declarado |
| Texto/bounds | Trim, 1..255 obrigatórios, opcionais255/obs10000, vazios opcionaisnull | Schema existente e formulário consistente | Default técnico declarado |
| Prioridade/datas | Texto livre; datas ISO opcionais sem default/ordem relativa exigida | Não inventar taxonomia ou prazos | Default técnico declarado |
| Duplicidade | Ignorar caixa, preservar grafia; lock global→alvo, precheckIgnoreCase e índice existente | Serializar cadastros entre quadros garante caixa mesmo no H2 VARCHAR; sem alterar legados/schema/collation | Default técnico declarado |
| Snapshot | Ampliar estrutura existente com demandas flat, counts derivados da mesma lista sob lock | Evitar consultas/estados misturados | Default técnico declarado |
| Compatibilidade POST antigo | Manter200/entidade/sem versão, exigir primeira etapa persistida e estado inicial válido | Fechar cadastro alternativo sem quebrar contratos das ações fora do corte | Default técnico declarado |
| Versão | Novo endpoint exige/incrementa1; legado e demais ações antigas mantêm contrato atual | Versionar novo fluxo sem inventar revisão geral de demandas | Default técnico declarado |
| Testes antigos | Acrescentar demandas nos fixtures, preservar resumos; tornar Destino primeira só nos casos de criação concorrente | Contrato novo escolhido substitui cadastro em segunda etapa, mantendo cenário/assertions | Autorizado pelo escopo confirmado |

**Open questions:** none neste corte; edição/exclusão/transições não são inferidas.

## User Stories

### P1: Cadastro persistido

**User Story:** como usuário autenticado, quero cadastrar uma demanda no quadro.
**Acceptance Criteria:**
1. WHEN POST /api/gerenciamentos/{id}/demandas recebe cadastro válido e versão atual THEN o sistema SHALL responder201 com snapshot completo incluindo o novo registro persistido. (CAD-01)
2. WHEN número e cliente/solicitante são enviados THEN o sistema SHALL salvar ambos com trim, entre1 e255 unidades UTF-16. (CAD-02)
3. WHEN campos opcionais são enviados ou omitidos THEN o sistema SHALL preservar valores válidos com trim e transformar texto opcional vazio em null, respeitando255 para responsável/prioridade e10000 para observações. (CAD-03)
4. WHEN datas opcionais são enviadas ou omitidas THEN o sistema SHALL preservar datas ISO válidas ou null sem gerar data de hoje nem impor ordem entre prazos. (CAD-04)
5. WHEN uma demanda é criada THEN o sistema SHALL salvar status exatamente Em andamento e dataConclusao/dataCancelamento/motivoCancelamento null. (CAD-05)
6. WHEN há trabalhos com lacunas/empates/homônimos/categoriaSQLnull THEN o sistema SHALL escolher o primeiro TRABALHO por ordem/ID, sem escolher uma final. (CAD-06)
7. IF o quadro não possui trabalho THEN o sistema SHALL responder409 com Cadastre uma etapa de trabalho antes de criar demandas. sem gravar. (CAD-07)
8. WHEN criador/admin/outro autenticado cria em quadro ativo inclusive sem criador THEN o sistema SHALL permitir cadastro sem conferir podeAdministrar. (CAD-08)
9. IF sessão falta/é inválida, quadro inexiste ou está arquivado THEN o sistema SHALL responder respectivamente401/404/409 conforme guard atual, sem gravar. (CAD-09)
10. IF número/cliente falta/é branco/excede255 ou opcionais excedem seus limites THEN o sistema SHALL responder400 com mensagem do campo e preservar registros/histórico/versão. (CAD-10)
11. IF JSON/data/versão tem formato inválido THEN o sistema SHALL responder400 com Dados da requisição inválidos. sem gravar. (CAD-11)
12. IF o cadastro novo contém campo desconhecido, ID, etapa, status ou encerramento THEN o sistema SHALL responder400 com Dados da requisição inválidos. sem gravar. (CAD-12)
13. IF o número já existe em qualquer quadro, inclusive com caixa diferente THEN o sistema SHALL responder409 com Já existe uma demanda com esse número. sem registro/histórico parcial. (CAD-13)
14. IF versão é ausente ou obsoleta THEN o sistema SHALL responder400 Informe uma versão válida do gerenciamento. ou409 Gerenciamento alterado por outro usuário. Atualize e tente novamente. sem gravar. (CAD-14)
15. WHEN novo cadastro confirma THEN o sistema SHALL incrementar versão exatamente uma vez e salvar exatamente um histórico CRIACAO/Processo criado. com autor da sessão na mesma transação. (CAD-15)
16. IF persistência da demanda/histórico falha THEN o sistema SHALL reverter demanda/histórico/versão integralmente e responder500 Não foi possível concluir a operação. (CAD-16)
17. WHEN cadastros disputam com arquivo/remoção/reordenação ou número igual entre quadros THEN o sistema SHALL serializar pelo quadro/restrição única, sem órfãos/histórico parcial e com no máximo um cadastro confirmado por número. (CAD-17)
18. WHEN POST /api/processos legado recebe primeira etapa persistida e status ausente ou Em andamento THEN o sistema SHALL manter200/entidade/sem versão exigida/incremento, aplicando os mesmos campos/defaults/duplicidade e transação. (CAD-18)
19. IF cadastro legado contém ID, estado final ou dados de encerramento THEN o sistema SHALL responder400 sem gravar, preservando a recusa de ID atual e sem aceitar etapa/gerenciamento falsificado. (CAD-19)
20. IF cadastro legado indica etapa posterior/final THEN o sistema SHALL responder400 Novas demandas devem começar na primeira etapa de trabalho. sem gravar; etapa persistida inexistente continua404. (CAD-20)
**Independent Test:** HTTP/persistência H2 e MySQL fictício, incluindo todos campos, histórico, versão e rollback.

### P1: Consulta coerente

**User Story:** como usuário autenticado, quero consultar os cartões reais do quadro.
**Acceptance Criteria:**
1. WHEN estrutura-etapas é consultada THEN o sistema SHALL devolver gerenciamento, etapas e demandas do mesmo quadro, ordenadas por ID, com os14 campos flat do design, incluindo etapaId, sem cadeia de entidades. (CAD-21)
2. WHEN snapshot é montado THEN o sistema SHALL calcular quantidadeDemandas a partir da mesma lista de demandas e preservar ordenação/categorias oficiais anteriores. (CAD-22)
3. WHEN quadro ativo ou arquivado é consultado THEN o sistema SHALL preservar todos os dados/versão, inclusive status/valores antigos ausentes ou contraditórios, sem preparação ou escrita. (CAD-23)
4. WHEN CRUD de etapas ou novo cadastro retorna snapshot THEN o sistema SHALL incluir os cartões atuais junto de metadados/etapas, sem perdê-los após configuração. (CAD-24)
5. WHEN consulta disputa com transação do quadro THEN o sistema SHALL aguardar seu commit e retornar uma única combinação coerente de versão/etapas/cartões/contagens. (CAD-25)
**Independent Test:** snapshot HTTP completo, duas demandas em quadros diferentes, registro antigo e concorrência sob lock.

### P1: Formulário e cartões

**User Story:** como usuário, quero usar Criar processo e ver a demanda salva.
**Acceptance Criteria:**
1. WHEN snapshot ativo com trabalho está carregado THEN a interface SHALL permitir Criar processo a qualquer usuário autenticado, independentemente de podeAdministrar. (CAD-26)
2. WHEN faltam trabalhos THEN a interface SHALL desabilitar cadastro e orientar criador/admin a usar Nova etapa, ou outros a solicitar configuração ao criador/admin. (CAD-27)
3. WHEN formulário abre THEN a interface SHALL oferecer Número da demanda e Cliente/solicitante obrigatórios, os seis opcionais do design e a informação de etapa inicial automática, sem escolher status/etapa/encerramento. (CAD-28)
4. IF textos violam os limites definidos THEN a interface SHALL mostrar a mesma mensagem do campo, preservar dados e não enviar POST. (CAD-29)
5. WHEN formulário é enviado THEN a interface SHALL enviar somente os nove campos de cadastro do design, com textos normalizados/opcionaisnull e versão atual. (CAD-30)
6. WHILE cadastro está pendente THEN a interface SHALL executar somente um POST e bloquear campos, demais mutações, Cancelar, Voltar e Sair. (CAD-31)
7. WHEN cadastro confirma snapshot válido THEN a interface SHALL fechar formulário, atualizar versão/cache/contagens/cartões e devolver foco ao botão Criar processo sem cadastro sintético nem novo POST. (CAD-32)
8. IF cadastro falha ou resposta não confirma201, quadro ativo, versão enviada+1 e a demanda com os oito valores normalizados/estado inicial/finaisnull/primeira TRABALHO definidos neste contrato THEN a interface SHALL conservar todos campos/mensagem, exigir Atualizar quadro antes de reenviar e nunca repetir POST automaticamente; GET/configuração continuam aceitando snapshots vazios e dados antigos. (CAD-33)
9. WHEN quadro é atualizado com rascunho aberto THEN a interface SHALL preservar rascunho e aplicar versão/permissões/trabalhos atuais; arquivo/zero trabalho impede reenvio. (CAD-34)
10. WHEN navegação/consulta muda de quadro THEN a interface SHALL cancelar/ignorar resposta anterior sem mostrar cartões do quadro anterior. (CAD-35)
11. IF snapshot está incompleto ou inconsistente THEN API frontend SHALL rejeitar sucesso, incluindo204 ou ausência de demandas, ID/vínculo inválido, contagens divergentes e duplicação de IDs. (CAD-36)
12. WHEN demanda é renderizada THEN a interface SHALL mostrar seu número/cliente, status/responsável/prioridade, datas e observações conforme design na etapaId correta, sem controles de edição/exclusão/transição. (CAD-37)
13. WHEN dado opcional antigo é ausente THEN a interface SHALL indicar Não informado, preservar status literal desconhecido e renderizar texto como texto, incluindo tags HTML. (CAD-38)
14. WHEN quadro arquivado é aberto THEN a interface SHALL mostrar os mesmos cartões somente para consulta, sem Criar processo. (CAD-39)
15. WHEN usuário usa Tab/Enter ou cancela formulário THEN a interface SHALL manter foco inicial/retorno e rascunhos/confirmacões de etapas existentes sem mutação ao cancelar. (CAD-40)
16. WHEN fluxo real cria mínimo/completo, tenta número duplicado em outro quadro e recarrega THEN o sistema SHALL manter dados/IDs/etapa/counts e mensagem de duplicidade no navegador, preservando os três E2E anteriores. (CAD-41)
**Independent Test:** Vitest por campos/payload/estado, quatro Edge/H2 e QA desktop/mobile.

## Edge Cases

Quadro só com finais, legado sem criador, trabalho homônimo de final/categoriaSQLnull, posições empatadas/lacunas, textos nos limites/Unicode, número repetido entre quadros, POST com campos de encerramento, datas opcionais nulas/pasadas, resposta204/incompleta, cache antigo após arquivo, troca de quadro e falha parcial. Rotas de edição/exclusão/transição fora do corte preservam seus testes.

## Implicit Requirements Sweep

| Dimensão | Resolução |
| --- | --- |
| Input/bounds | CAD-02–04/10–14/19–20/29 |
| Falha parcial | CAD-16/33 |
| Dedup/retry | CAD-13/17/31/33; número único e sem retry automático |
| Auth | CAD-08–09/26/39; nenhuma auth/rate limit nova |
| Concorrência/ordem | CAD-06/14–17/25/35 |
| Ciclo de dados | CAD-23/39; nenhuma exclusão/migração |
| Observabilidade | CAD-15 histórico existente; logs de gerenciamento fora do corte |
| Dependência externa | N/A: sem serviço externo novo, falha de banco CAD-16 |
| Transições | Somente entrada inicial CAD-05/06; ações futuras fora do corte |
| Expiração | N/A: não cria sessão/TTL ou dado descartável novo |

## Requirement Traceability

| Requirement | Task | Status |
| --- | --- | --- |
| CAD-01 | T2, T5, T6, T7 | Complete T2, T5, T6, T7; Verified |
| CAD-02 | T2, T4 | Complete T2, T4; Verified |
| CAD-03 | T2, T4 | Complete T2, T4; Verified |
| CAD-04 | T2, T4 | Complete T2, T4; Verified |
| CAD-05 | T2, T6, T7 | Complete T2, T6, T7; Verified |
| CAD-06 | T2, T7 | Complete T2, T7; Verified |
| CAD-07 | T2, T5 | Complete T2, T5; Verified |
| CAD-08 | T2, T5 | Complete T2, T5; Verified |
| CAD-09 | T2 | Complete T2; Verified |
| CAD-10 | T2, T4 | Complete T2, T4; Verified |
| CAD-11 | T2 | Complete T2; Verified |
| CAD-12 | T2 | Complete T2; Verified |
| CAD-13 | T2, T6 | Complete T2, T6; Verified |
| CAD-14 | T2 | Complete T2; Verified |
| CAD-15 | T2, T7 | Complete T2, T7; Verified |
| CAD-16 | T2 | Complete T2; Verified |
| CAD-17 | T2 | Complete T2; Verified |
| CAD-18 | T2 | Complete T2; Verified |
| CAD-19 | T2 | Complete T2; Verified |
| CAD-20 | T2 | Complete T2; Verified |
| CAD-21 | T1 | Complete T1; Verified |
| CAD-22 | T1 | Complete T1; Verified |
| CAD-23 | T1 | Complete T1; Verified |
| CAD-24 | T1, T5 | Complete T1, T5; Verified |
| CAD-25 | T1 | Complete T1; Verified |
| CAD-26 | T5 | Complete T5; Verified |
| CAD-27 | T5 | Complete T5; Verified |
| CAD-28 | T4 | Complete T4; Verified |
| CAD-29 | T4 | Complete T4; Verified |
| CAD-30 | T3, T4, T5, T7 | Complete T3, T4, T5, T7; Verified |
| CAD-31 | T4, T5 | Complete T4, T5; Verified |
| CAD-32 | T5, T7 | Complete T5, T7; Verified |
| CAD-33 | T3, T4, T5, T7 | Complete T3, T4, T5, T7; Verified |
| CAD-34 | T5 | Complete T5; Verified |
| CAD-35 | T5 | Complete T5; Verified |
| CAD-36 | T3, T7 | Complete T3, T7; Verified |
| CAD-37 | T5 | Complete T5; Verified |
| CAD-38 | T5 | Complete T5; Verified |
| CAD-39 | T5 | Complete T5; Verified |
| CAD-40 | T4, T5 | Complete T4, T5; Verified |
| CAD-41 | T6 | Complete T6; Verified |

## Success Criteria

Preservar baseline278 Java/235Vitest/3E2E; gates por tarefa, quarto E2E de demandas, build TEMP/lint e MySQL fictício. Verificador novo após último commit, todos41ACs com file:line/assertion, sensor de integridade>=5 preferencialmente em RAM, comparação integral após descarte, validate_spec/tasks/state. UAT humano separado, sem inferir aprovação de autorização para continuar.
