# Atividade por gerenciamento — decisões e descoberta

**Data:**10/10/2026. **Estado:** proposta aprovada, execução autorizada por T1; nenhum requisito concluído. Continuação após comentarios-lateral:28/28 Verified,840 Java/H2,1000 Vitest,336 MySQL fictício,15 Edge/H2,lint/build e sensor7/7 PASS. Planejamento f1bbb08, branch testes.

## Escolhas confirmadas

- AD-030: registrar automaticamente alterações de quadro, etapas e demandas, incluindo movimentos, encerramentos e arquivo/restauração. Mostrar o que aconteceu, alvo, autor da conta e horário.
- AD-031: qualquer usuário autenticado consulta, inclusive arquivados. Registros preservados, sem edição/exclusão pela interface. Dados antigos mostrados como estão, sem inventar autores ou acontecimentos.
- AD-032: edições mostram os campos alterados com valor anterior e novo, além de autor e horário. Históricos antigos conservam somente o detalhe existente.
- AD-033: envio de comentário entra como Comentário adicionado, com demanda, autor e horário; a mensagem completa permanece na conversa.
- Tecnologias AD-024 mantidas. Implementação local e testes fictícios; sem operação no banco real, migração aplicada, push, deploy ou upgrade.
- Registrar no diagnóstico cada requisito com estado verdadeiro; só concluir após gates e Verifier TLC PASS.

## Respostas recebidas e próxima decisão

Registro histórico da preparação em10/10/2026: a proposta abaixo foi confirmada posteriormente. Não há decisão pendente sobre compatibilidade ou forma de execução.

As duas perguntas acima foram respondidas explicitamente com as recomendações, registradas em AD-032/033. Não estão mais pendentes.

Proposta para a compatibilidade: impedir novas escritas manuais no histórico antigo, mantendo sua consulta e todos os registros existentes. Há três cenários antigos que aceitam essa escrita: HistoricoArchiveTests (um cenário), DemandaArquivoHistoricoApiTests (um) e DemandaArquivoHistoricoTests (um cenário parametrizado em dois estados). A alteração exige decisão do usuário e autorização exata para os testes; ainda não aplicada. A proposta completa está em spec.md/design.md/tasks.md, todos Draft até confirmação.

## Descobertas no código

| Ponto | Evidência | Efeito na próxima proposta |
| --- | --- | --- |
| Quadro não grava atividade própria | GerenciamentoService.java:40 criação, :102 edição e :125 mudança de arquivo | Instrumentar as três transações sem alterar permissões, respostas ou regras de versão |
| Etapas não gravam atividade própria | EtapaService.java:56 criação, :79 edição e :103 remoção | Preservar evento e identificação mesmo após remover uma etapa vazia; reordenação automática precisa de regra precisa |
| Demandas já têm histórico automático | ProcessoService.java:139 criação, :253 edição, :371 transição e :408 arquivo | Preservar esses registros e evitar duplicação na lista nova; usar os mesmos caminhos centrais para APIs antigas/novas |
| Histórico antigo aceita informação manual | HistoricoController.java POST, HistoricoService.registrarExterno e HistoricoArchiveTests.java:56–65 | Distinguir histórico anterior de registros novos automáticos, sem presumir confiabilidade/autoria antiga. Definir compatibilidade dessa escrita na spec; teste antigo espera MANUAL/Informado, qualquer alteração de outcome exige confirmação TLC concreta |
| Consulta atual é por demanda | HistoricoRepository.findByProcessoIdOrderByDataHoraAsc | Criar consulta isolada por quadro, ordenação determinística com desempate, vazio/arquivo e DTO sem senha/entidades |
| Comentários conservam histórico/versão | ComentarioService.gravar e COM-12 da entrega concluída | Caso envio entre na atividade, usar o novo registro sem alterar o histórico de demanda ou incrementar versão |
| Fixtures removem históricos antes dos pais | ApiIntegrationSupport.java:30 e :76 | Persistência adicional deve ser incluída na limpeza e nas provas de rollback/preservação, sem apagar/ignorar cenários |
| Quadro tem coordenação de formulários/envio | Quadro.jsx:74/:188/:307 | Consulta de atividade deve preservar bloqueios, foco, rascunhos e respostas tardias dos fluxos existentes |

Caminhos acima estão em sistema/src/main/java/com/patp/sistema/ (service/controller/repository), sistema/src/test/java/com/patp/sistema/ e frontend/src/pages/. Nenhum banco configurado foi consultado nesta descoberta. Não há lições confirmadas no projeto; L-006/L-007 candidatas permanecem evidências do sensor anterior, sem promoção automática.

## Próximo passo

Checkpoint final do trabalhador3 em13/10/2026: T1–T18 Implemented;1087 Java/H2,1176 Vitest/28 arquivos,18 Edge/H2/9 arquivos,847 MySQL fictício,lint/buildTEMP PASS. MySQL owned19572 encerrado após identidade atual e shutdown,porta33823 ausente. O orquestrador executará dois ajustes pequenos já identificados (ceiling da primeira resposta e booleano visual Sim/Não),depois Verifier fresco/sensor/validate_state. Trabalhador3 não declara nenhum ACT Verified. As notas datadas abaixo conservam os checkpoints anteriores.

Checkpoint atual13/10/2026: T1–T17 Implemented;T18 é a próxima tarefa. Usuário autorizou especificamente corrigir a expectativa nova de arquivo do quadro ematividade.spec.jsT17:200→204,compatível com@ResponseStatus(NO_CONTENT) existente,após falha concreta204. Nenhum outro outcome novo/antigo foi alterado. Helperkeyboard ajustado mecanicamente para contar controles focáveis em vez de limite30,conservando assertion de foco e Enter. Verifier fresco será disparado pelo orquestrador após T18 e eventuais correções pequenas documentadas,sem conclusão antecipada.

Checkpoint12/10/2026: T1–T14 Implemented; lote2 adicionou166 testes Java,com1087 Java/H2,1000 Vitest,847 MySQL fictício,lint/buildTEMP PASS,zero falhas/erros/skips. O orquestrador revisa o lote antes de iniciar trabalhador3 T15–T18. Atividade permanece em andamento,sem critérios Verified. Os três cenários exatos T13 autorizados foram adequados; escrita manual bloqueada sem alterar consulta antiga. Evidências em evidence.md e estados em tasks.md.

Respostas explícitas recebidas: proposta/defaults aprovados; bloquear escrita manual e adequar exatamente os três cenários T13; três agentes sequenciais7/7/4 aprovados. Não perguntar novamente. Verifier fresco automático após T18, independente da escolha dos trabalhadores.

## Gates da proposta em10/10/2026

validate_spec.py --strict: exit0,0 erros/avisos;39 critérios com IDs e defaults explícitos. validate_tasks.py: exit0,0 erros, um aviso esperado de Tests:none na entidade T1, conferido contra a matriz (compile).18 tarefas com dependências e testes co-localizados; tabelas de consistência preenchidas. git diff --check restrito aos documentos alterados: exit0. Esses resultados validam os documentos; nenhum teste do sistema foi reexecutado nesta preparação e nenhum requisito está concluído.
