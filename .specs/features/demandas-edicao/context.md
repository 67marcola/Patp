# Edição de demandas — contexto

**Gathered:** 2026-10-07. **Status:** Em definição; duas decisões de produto aguardam resposta. Nenhum código da edição foi implementado.

## Feature Boundary

Acrescentar edição de dados informativos aos cartões de demandas. Criador do quadro ou administrador pode editar em quadro ativo, conforme AD-023. Movimentação, conclusão, cancelamento e reabertura permanecem nas ações próprias. Retirada de demandas, comentários laterais, atividade por quadro e gráficos são entregas separadas.

## Implementation Decisions

- AD-023: edição restrita ao criador/admin; arquivados somente consulta.
- AD-024: manter Java/Spring Boot/JPA/Hibernate/MySQL/Workbench, React/JavaScript/HTML/CSS/Vite, Maven e Node.js/npm; JUnit/H2, Vitest/Testing Library e Playwright nos testes.
- Usuário pediu que cada requisito concluído e verificado seja registrado no DIAGNOSTICO.md. Progresso parcial será identificado como parcial até a revisão independente da entrega.
- Usuário pediu explicação das funcionalidades e da opção recomendada em cada pergunta.

### Decisões perguntadas, ainda sem resposta

1. Número: foi recomendado manter fixo após cadastro, preservando a referência da demanda. Alternativa: permitir correção com unicidade global.
2. Encerradas: foi recomendado permitir correção informativa em quadro ativo, sem alterar status, etapa, datas de encerramento ou motivo. Alternativa: somente demandas em andamento.

As duas propostas aparecem como propostas na spec, sem aprovação implícita. Não iniciar implementação dependente enquanto aguardam resposta.

### Defaults de implementação propostos

- Reutilizar limites, normalização e validação de datas do cadastro, sem acrescentar novas regras de ordem entre datas.
- Abrir formulário preenchido a partir do snapshot do quadro; cancelar sem gravação.
- Enviar versão do quadro, confirmar campos da resposta e manter o rascunho após erro, com atualização manual.
- Registrar um evento EDICAO com autor real da sessão; salvar demanda, histórico e versão numa transação.
- Histórico de edição informativo não substitui futura entrega da consulta de atividade por quadro.

## Referências e riscos encontrados

| Referência | Fato encontrado | Tratamento necessário |
| --- | --- | --- |
| ProcessoService.java, método editar | Edição legada aceita terceiro, não valida os dados como cadastro e não incrementa versão. | Aplicar AD-023 também na rota antiga; validar campos e preservar o ciclo, histórico e versão. |
| ProcessoArchiveTests.java, ativoConservaCriacaoEdicaoMovimentoFinalizacaoEHistoricosAutomaticos | Cenário permite que Outro edite e altere o número; criação por Outro deve continuar válida. | Após as decisões, apresentar ajuste concreto do teste antigo antes de modificá-lo, preservando criação, ciclo e histórico. |
| DemandaTransicaoServiceTests.java, edicaoDeOutrosCamposNaoLimpaStatusOuEtapaOmitidos | Cenário permite edição por Colaborador e alteração de número. | Manter a prova de ciclo preservado e adaptar a autorização/número à política aprovada. |
| QuadroTransicoes.test.jsx, MOV-28/MOV-35 | Algumas assertions enumeram todos os botões do cartão, embora o contrato original trate das transições. | Incluir Editar somente onde permitido; manter as provas das ações de ciclo e do bloqueio de terceiros/arquivados. |
| EditorDemanda.jsx | Formulário existente serve apenas ao cadastro e começa vazio. | Acrescentar modo de edição sem mudar o cadastro validado. |

## Deferred Ideas

Retirada de demandas tem pergunta anterior pendente (arquivar/restaurar recomendado). Não depende da edição para ser decidida, nem bloqueia esta entrega. Correção explícita de estados legados, comentários, atividade e gráficos permanecem fora deste corte.
