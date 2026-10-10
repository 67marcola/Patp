# Atividade por gerenciamento — decisões e descoberta

**Data:**10/10/2026. **Estado:** definição em andamento, sem aplicação implementada e sem requisito concluído. Continuação após comentarios-lateral:28/28 Verified,840 Java/H2,1000 Vitest,336 MySQL fictício,15 Edge/H2,lint/build e sensor7/7 PASS. Base atual7825bfd, branch testes.

## Escolhas confirmadas

- AD-030: registrar automaticamente alterações de quadro, etapas e demandas, incluindo movimentos, encerramentos e arquivo/restauração. Mostrar o que aconteceu, alvo, autor da conta e horário.
- AD-031: qualquer usuário autenticado consulta, inclusive arquivados. Registros preservados, sem edição/exclusão pela interface. Dados antigos mostrados como estão, sem inventar autores ou acontecimentos.
- Tecnologias AD-024 mantidas. Implementação local e testes fictícios; sem operação no banco real, migração aplicada, push, deploy ou upgrade.
- Registrar no diagnóstico cada requisito com estado verdadeiro; só concluir após gates e Verifier TLC PASS.

## Perguntas enviadas, ainda sem resposta

1. Edições: recomendado mostrar campo, valor anterior e novo, por exemplo Responsável: Ana → Bruno. Alternativa: apenas ação/autor/horário. Essa escolha determina a precisão do conteúdo a persistir e exibir; os históricos antigos não terão valores inventados.
2. Comentários: recomendado incluir na atividade apenas Comentário adicionado com demanda/autor/horário. Texto completo permanece na conversa. Alternativa: não incluir envios na atividade. Não decidir pelo silêncio.

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

Receber as duas escolhas restantes, precisar eventos/valores, repetição sem mudança, origem de registros antigos e consulta/filtros. Produzir spec testável e plano atômico TLC com evidência por camada. Esta descoberta é preparação, não conclusão da atividade.
