# Edição de demandas Specification

**Status:** Draft de 07/10/2026. Permissão e tecnologias confirmadas; número e edição de encerradas aguardam resposta. Nenhuma implementação da nova edição. Este draft é revisável, mas ainda não autoriza fechar a especificação.

## Problem Statement

O quadro permite cadastrar e acompanhar demandas, mas não oferece edição dos dados informativos. A rota legada de edição aceita usuários fora de AD-023, não aplica a validação do cadastro e não atualiza a versão do quadro. O criador/admin precisa corrigir dados com histórico e proteção contra conflitos, preservando o ciclo da demanda.

## Goals

- [ ] Editar dados informativos com autorização, validação e persistência transacional.
- [ ] Oferecer formulário preenchido e confirmar o resultado antes de fechar.
- [ ] Registrar conclusão e evidências no diagnóstico após testes e revisão independente.

## Out of Scope

| Item | Reason |
| --- | --- |
| Retirada/exclusão/arquivamento de demandas | Política separada ainda não decidida. |
| Alterar status, etapa ou encerramento pelo formulário | Ciclo já tem ações próprias e contrato verificado. |
| Corrigir automaticamente estado legado | AD-022 exige correção explícita; não inventar estado ou dados. |
| Comentários laterais, gráficos e atividade por quadro | Entregas próprias. |
| Migração real, atualização de versões, push/deploy | Autorização atual cobre implementação e commits locais. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Permissão | Criador/admin em quadro ativo; sem criador, apenas admin | AD-023 e AD-005/006 | Sim |
| Tecnologias | Conjunto atual, H2 somente testes | AD-024 | Sim |
| Número | Proposta: fixo após cadastro | Preservar identificação e referências | Pergunta pendente; não implementar ainda |
| Estado encerrado | Proposta: permitir correção informativa sem alterar ciclo | Corrigir informação não deve reabrir trabalho | Pergunta pendente; não implementar ainda |
| Campos e limites | Cliente obrigatório 1–255; responsável/prioridade opcionais até 255; observações opcionais até 10000; três datas opcionais | Reutilizar cadastro existente, sem nova regra de cronologia | Default proposto |
| Normalização | Mesma normalização do cadastro; opcional em branco vira null; limites em unidades UTF-16 | Contrato consistente entre criação e edição | Default proposto |
| Rascunho e conflito | Preservar campos e exigir atualização manual, sem repetir mutação | Padrão já validado no quadro | Default proposto |
| Envio sem diferenças | Salvar explicitamente ainda registra EDICAO e incrementa versão uma vez | Não introduzir comportamento silencioso especial; consistente com edição de quadros/etapas | Default proposto |
| Registro legado com status desconhecido | Proposta conservadora: permanece consulta até regra explícita de correção | Manter orientação existente e evitar ampliar correção de estado neste corte | Default proposto |

**Open questions:** número e edição de encerradas têm perguntas enviadas. A política de retirada pertence a outra feature. Após respostas, fechar o contrato e validar novamente antes de Execute.

## User Stories

### P1: Corrigir dados com responsabilidade

**User Story:** Como criador do quadro ou administrador, quero corrigir os dados de uma demanda para manter as informações de trabalho atualizadas.

**Independent Test:** Editar cliente e observações, recarregar o quadro e conferir os novos valores, autor da edição e ciclo preservado.

**Acceptance Criteria:**

- **EDI-01:** IF a sessão estiver ausente, inválida ou encerrada THEN o sistema SHALL devolver HTTP 401 sem alterar demanda, comentários, histórico ou versão.
- **EDI-02:** IF um funcionário que não criou o quadro solicitar edição THEN o sistema SHALL devolver HTTP 403 sem efeitos persistidos.
- **EDI-03:** IF o quadro estiver arquivado THEN o sistema SHALL devolver HTTP 409 com `Gerenciamento arquivado. Restaure-o antes de alterar.` sem efeitos persistidos.
- **EDI-04:** WHILE o quadro não tiver criador identificado o sistema SHALL permitir edição somente a administrador autenticado.
- **EDI-05:** WHEN uma edição autorizada válida for aceita THEN o sistema SHALL persistir exatamente cliente, responsável, prioridade, data de emissão, prazo da etapa, prazo geral e observações normalizados do formulário completo.
- **EDI-06:** IF cliente estiver ausente, vazio após normalização ou tiver mais de 255 unidades UTF-16 THEN o sistema SHALL devolver HTTP 400 com `Informe um cliente/solicitante entre 1 e 255 caracteres.` sem efeitos persistidos.
- **EDI-07:** IF responsável ou prioridade exceder 255 unidades UTF-16, ou observações exceder 10000, após normalização THEN o sistema SHALL devolver HTTP 400 sem efeitos persistidos, com a mensagem correspondente já usada no cadastro.
- **EDI-08:** WHEN um campo opcional for enviado vazio ou null THEN o sistema SHALL salvar null nesse campo, permitindo retirar a informação opcional.
- **EDI-09:** IF uma data enviada não for uma string ISO válida ou null THEN o sistema SHALL devolver HTTP 400 sem efeitos persistidos.
- **EDI-10:** The sistema SHALL preservar literalmente ID, status, etapa, data de conclusão, data de cancelamento e motivo de cancelamento durante edição informativa.
- **EDI-11:** IF a demanda não existir no gerenciamento da rota THEN o sistema SHALL devolver HTTP 404 sem editar demanda de outro quadro.
- **EDI-12:** IF o payload tentar alterar o ciclo ou incluir campos desconhecidos na rota dedicada THEN o sistema SHALL devolver HTTP 400 sem efeitos persistidos.

### P1: Preservar consistência e autoria

**User Story:** Como responsável pelo quadro, quero saber que a edição foi salva integralmente e que não substituiu uma alteração concorrente.

**Independent Test:** Enviar duas edições com a mesma versão; somente uma é aceita, com um evento, autor correto e uma nova versão.

**Acceptance Criteria:**

- **EDI-13:** IF a versão do quadro estiver ausente, negativa ou não for um inteiro válido THEN o sistema SHALL devolver HTTP 400 sem efeitos persistidos.
- **EDI-14:** IF a versão enviada diferir da versão persistida THEN o sistema SHALL devolver HTTP 409 com `Gerenciamento alterado por outro usuário. Atualize e tente novamente.` sem efeitos persistidos.
- **EDI-15:** WHEN uma edição for aceita THEN o sistema SHALL incrementar a versão do quadro exatamente uma vez e devolver HTTP 200 com o snapshot completo atualizado.
- **EDI-16:** WHEN uma edição for aceita THEN o sistema SHALL registrar exatamente um evento EDICAO da demanda, com autor da sessão, data/hora e descrição `Processo alterado.`.
- **EDI-17:** IF uma escrita de demanda, histórico ou versão falhar THEN o sistema SHALL reverter todas as escritas da edição.
- **EDI-18:** WHEN edição disputar com arquivamento ou transição da mesma demanda THEN o sistema SHALL respeitar a ordem dos commits pelo lock do quadro, recusando arquivo/versão incompatíveis sem resultado parcial.
- **EDI-19:** The sistema SHALL aplicar a autorização e a preservação do ciclo também na rota legada PUT `/api/processos/{id}`, sem permitir contornar o contrato pela rota antiga.

### P1: Editar no quadro sem perder o rascunho

**User Story:** Como criador/admin, quero um formulário preenchido e confirmação do resultado para corrigir dados sem perder minhas alterações em caso de erro.

**Independent Test:** Abrir Editar demanda por teclado, corrigir campos, salvar, verificar cartão/versão e recarregar; simular erro e conferir rascunho preservado.

**Acceptance Criteria:**

- **EDI-20:** WHEN um usuário autorizado abrir Editar demanda THEN a interface SHALL preencher os campos com os valores do snapshot atual, exibindo os opcionais null como vazios.
- **EDI-21:** WHEN o usuário cancelar o formulário THEN a interface SHALL fechar sem enviar mutação e restaurar foco a um controle existente do quadro.
- **EDI-22:** WHILE um salvamento estiver pendente a interface SHALL impedir envio duplicado, fechamento do formulário e demais mutações ou saída do quadro.
- **EDI-23:** IF houver erro HTTP, rede ou resposta de sucesso não confirmada THEN a interface SHALL preservar os campos, mostrar erro e exigir atualização manual do quadro sem repetir a edição automaticamente.
- **EDI-24:** WHEN HTTP 200 confirmar versão anterior +1, demanda correta, campos normalizados e ciclo preservado THEN a interface SHALL atualizar cartão e cache do quadro, fechar o formulário e restaurar foco existente.
- **EDI-25:** WHEN o gerenciamento exibido mudar THEN a interface SHALL descartar o formulário anterior e ignorar respostas tardias daquele gerenciamento.
- **EDI-26:** WHILE o usuário não tiver permissão ou o quadro estiver arquivado a interface SHALL exibir os dados para consulta sem botão Editar demanda.

## Edge Cases

- Valores exatamente 255/10000 são aceitos; acima disso são recusados, incluindo caracteres que ocupam duas unidades UTF-16.
- Datas bissextas válidas são aceitas e datas impossíveis recusadas; não acrescentar exigência de prazo posterior à emissão.
- Opcional retirado deve virar null no banco e Não informado no cartão.
- Encerramentos antigos com null, datas ou motivos longos não são normalizados por edição informativa.
- Criador nulo não é atribuído automaticamente.
- Uma edição não deve mover demanda nem mudar contagens das etapas.
- Número e estados permitidos serão incorporados como critérios específicos após as duas respostas; este draft não presume essas escolhas.

## Implicit Requirement Dimensions

| Dimension | Resolution |
| --- | --- |
| Input validation & bounds | EDI-05–09/12 e limites do cadastro. |
| Failure / partial failure | EDI-17/23. |
| Idempotency / retry / duplicate | EDI-14/22/23: versão, um envio e atualização manual. |
| Auth boundaries & rate limits | EDI-01–04/19/26; rate limit novo N/A, não há endpoint público nem política nova nesta entrega. |
| Concurrency / ordering | EDI-13–15/18. |
| Data lifecycle / expiry | EDI-03/10; retirada/expiração N/A, fora do escopo. |
| Observability | EDI-16; consulta completa de atividade é outra entrega. |
| External-dependency failure | EDI-17/23; nenhuma integração externa nova. |
| State-transition integrity | EDI-10/12/19; edição informativa não executa transições. |

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| EDI-01 | P1: Autorização | Specify | Pending |
| EDI-02 | P1: Autorização | Specify | Pending |
| EDI-03 | P1: Autorização | Specify | Pending |
| EDI-04 | P1: Autorização | Specify | Pending |
| EDI-05 | P1: Dados | Specify | Pending |
| EDI-06 | P1: Dados | Specify | Pending |
| EDI-07 | P1: Dados | Specify | Pending |
| EDI-08 | P1: Dados | Specify | Pending |
| EDI-09 | P1: Dados | Specify | Pending |
| EDI-10 | P1: Ciclo preservado | Specify | Pending |
| EDI-11 | P1: Dados | Specify | Pending |
| EDI-12 | P1: Dados | Specify | Pending |
| EDI-13 | P1: Consistência | Specify | Pending |
| EDI-14 | P1: Consistência | Specify | Pending |
| EDI-15 | P1: Consistência | Specify | Pending |
| EDI-16 | P1: Histórico | Specify | Pending |
| EDI-17 | P1: Consistência | Specify | Pending |
| EDI-18 | P1: Consistência | Specify | Pending |
| EDI-19 | P1: Rota legada | Specify | Pending |
| EDI-20 | P1: Interface | Specify | Pending |
| EDI-21 | P1: Interface | Specify | Pending |
| EDI-22 | P1: Interface | Specify | Pending |
| EDI-23 | P1: Interface | Specify | Pending |
| EDI-24 | P1: Interface | Specify | Pending |
| EDI-25 | P1: Interface | Specify | Pending |
| EDI-26 | P1: Interface | Specify | Pending |

**Coverage:** 26 critérios em draft; nenhum implementado nesta entrega, nenhum declarado Verified. Tarefas serão produzidas após as decisões de produto.

## Success Criteria

- [ ] Decisões de número e estados fechadas e incorporadas aos critérios.
- [ ] Todas as provas de autorização, validação, persistência, conflito e UI aprovadas.
- [ ] Suítes anteriores preservadas; mudanças em testes antigos apresentadas de forma concreta antes de alterá-los.
- [ ] Verifier independente, sensor de falhas e validate_state PASS.
- [ ] DIAGNOSTICO.md atualizado com a conclusão técnica e a aceitação humana identificada separadamente.
