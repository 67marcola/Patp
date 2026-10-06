# Retirar PM da autenticação Specification

**Status:** T1 implementada e verificada independentemente, 4/4 critérios PASS; UAT humano pendente. **Base:** 39ea59c. **Data:** 2026-10-05.

## Problem Statement

Login e cadastro mostram PM em um bloco decorativo. O usuário pediu sua retirada. A entrega remove esse bloco por inteiro nas duas telas e os estilos exclusivos que ficam sem uso.

## Goals

- [x] Remover PM e seu espaço decorativo nas duas telas.
- [x] Conferir leitura e controles em desktop e celular, preservando os fluxos existentes.

## Out of Scope

Não criar logo ou identidade visual nova, redesenhar o sistema ou alterar regras de autenticação. As etapas finais e as perguntas de movimentação continuam em outra entrega. Sem alterações Java/banco, MySQL configurado, contas reais, push ou deploy. Artefatos anteriores, incluindo frontend/dist, permanecem preservados.

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Retirada de PM | Excluir o bloco inteiro nas duas telas | Pedido explícito do usuário | Sim |
| Substituição visual | Nenhum novo símbolo; título existente inicia o cartão | Não foi fornecido outro logotipo | Default técnico declarado |
| CSS | Remover somente duas regras exclusivas login-logo | rg não encontrou outros usos | Evidência do código |
| Escopo de teste | Reusar suítes existentes e inspeção visual, sem novos testes unitários para remoção estática | Alteração pequena e reversível; nenhuma regra mudou | Escolha técnica |

**Open questions:** none nesta remoção. Nenhuma resposta de negócio ou UAT é inferida de continuar.

## User Stories

### P1: Acessar formulários sem PM

**User Story:** como usuário da Creral, quero login e cadastro sem a marca PM solicitada para retirada.
**Why P1:** pedido explícito de interface.
**Acceptance Criteria**:
1. WHEN a tela de login é aberta THEN ela SHALL não mostrar PM nem um bloco decorativo vazio, e o primeiro conteúdo do cartão SHALL ser o título Bem-vindo. (MAR-01)
2. WHEN a tela de cadastro é aberta THEN ela SHALL não mostrar PM nem um bloco decorativo vazio, e o primeiro conteúdo do cartão SHALL ser o título Criar cadastro. (MAR-02)
3. WHEN cada tela é aberta em 1280x800 ou 375x812 THEN ela SHALL não ter rolagem horizontal, manter campos e botões dentro da largura visível e permitir ler o formulário por rolagem vertical quando necessária. (MAR-03)
4. WHEN o usuário cadastra, recarrega, sai e entra manualmente THEN o sistema SHALL preservar entrada direta após cadastro, sessão/quadro da mesma conta, controles de teclado e mensagens de erro/pending já verificados. (MAR-04)
**Independent Test:** inspeção visual/DOM das quatro combinações de tela e viewport; 222 testes frontend e 3 E2E existentes, sem alterar suas assertions.

## Edge Cases

- Remover apenas o texto deixando símbolo vazio não atende MAR-01/02.
- Celular deve conservar os campos/ações, sem alargar a página (MAR-03).
- Login manual e entrada direta após cadastro devem funcionar por teclado, com erros/pending preservados (MAR-04).

## Success Criteria

Uma alteração atômica nos três arquivos de apresentação. Gate frontend: npm test, build em TEMP novo, lint e os três E2E Edge/H2. Inspeção com capturas reais e assertions de resultados. Não acrescentar testes de implementação para a remoção visual. Verificador independente com sensor proporcional de 1–3 falhas em cópias isoladas e relatório físico; validate_state marca-login deve passar. UAT humano separado, pendente.

## Requirement Traceability

| Requirement | Task | Status | Evidence |
| --- | --- | --- | --- |
| MAR-01 | T1 | verified | validation.md; evidence.md |
| MAR-02 | T1 | verified | validation.md; evidence.md |
| MAR-03 | T1 | verified | validation.md; evidence.md |
| MAR-04 | T1 | verified | validation.md; evidence.md |
