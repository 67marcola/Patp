# Entrada automática após cadastro Specification

**Status:** especificação pronta para implementação local autorizada. Teste humano pendente.
**Data:** 2026-10-05. **Contexto:** `context.md`, AD-004 em `.specs/STATE.md`.

## Problem Statement

Cadastrar uma conta exige hoje voltar ao login. O servidor já oferece sessões para login, mas o cadastro não cria uma sessão e a interface ignora sua resposta. A entrega permite usar imediatamente a conta recém-criada.

## Goals

- [ ] Entrar na lista de gerenciamentos imediatamente após cadastro confirmado.
- [ ] Preservar segurança do cadastro, login manual e cache de sessão existente.
- [ ] Explicar falhas sem repetir cadastro nem perder os campos preenchidos.
- [ ] Verificar o fluxo real com H2 e validação independente.

## Out of Scope

PM/visual geral, novas regras de senha/email, mudança de papéis, nova política de cadastro, cookies/expiração, recuperação de senha e cache inválido anterior ao boot. Nenhum CRUD ou fluxo de movimentação adicional. Sem MySQL configurado, contas reais, push ou deploy.

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Cadastro deve entrar diretamente | Usar sessão criada após salvar a conta | Pedido explícito do usuário | Sim |
| Papel inicial | FUNCIONARIO inclusive primeiro cadastro e papel recebido | AD-004 e serviço atual | Sim |
| Resposta | HTTP 200 com DTO plano igual ao login | Compatibilidade existente | Escolha técnica declarada |
| Persistência no navegador | localStorage com token e usuario de quatro campos | Fluxo atual, compartilhado entre as duas telas | Escolha técnica |
| Setor legado | string ou null na resposta | Modelo permite null; não inventar dado | Escolha técnica |
| Sessão | Em memória no backend; reinício invalida tokens | Mecanismo existente | Limite preservado |
| Falhas atuais do serviço | Duplicidade recebe 500 genérico; ID recebido recebe 400 | Não redesenhar erros fora do requisito | Limite explícito |

**Open questions:** none para esta entrega. Perguntas sobre movimentação e UAT anteriores continuam pendentes em STATE.

## User Stories

### P1: Criar conta e sessão juntas na resposta

**User Story:** como novo funcionário, quero usar minha conta assim que o cadastro termina.
**Why P1:** elimina o segundo login solicitado pelo usuário.
**Acceptance Criteria**:
1. WHEN um cadastro válido é persistido THEN o servidor SHALL responder HTTP 200 com `token,id,nome,setor,email,papel`, token não vazio e ID do usuário salvo, permitindo usar esse token em GET /usuarios/me e GET /gerenciamentos sem POST /login. (AUT-01)
2. WHEN uma conta é cadastrada THEN o servidor SHALL persistir FUNCIONARIO, inclusive no primeiro cadastro e quando o corpo solicita ADMINISTRADOR; a senha SHALL ser BCrypt e senha/hash SHALL estar ausentes da resposta. (AUT-02)
3. IF o corpo do cadastro traz um ID THEN o servidor SHALL responder 400 com `O ID do usuário deve ser definido pelo sistema.`, sem alterar usuários existentes nem devolver token. (AUT-03)
4. IF o cadastro recebe JSON malformado, email duplicado ou falha de persistência THEN o servidor SHALL responder respectivamente 400 com `Dados da requisição inválidos.`, ou 500 com `Não foi possível concluir a operação.`, sem devolver token nem criar outra conta ou alterar a conta anterior. (AUT-04)
5. WHEN um login manual válido é solicitado THEN o servidor SHALL preservar HTTP 200 e o mesmo DTO seguro, com token utilizável e papel persistido atual. (AUT-05)
**Independent Test:** HTTP real de cadastro/login, dados persistidos, ausência de senha, uso do token e falhas sem novo registro.

### P1: Confirmar a resposta antes de entrar

**User Story:** como funcionário, quero saber se o navegador conseguiu confirmar minha entrada.
**Why P1:** resposta incompleta não autoriza inventar uma sessão.
**Acceptance Criteria**:
1. WHEN login ou cadastro responde HTTP 200 THEN o cliente SHALL aceitar somente objeto com token string não vazio após trim, id inteiro positivo, nome/email strings e setor string ou null; resposta inválida ou JSON ilegível SHALL lançar ApiError com status 200 e `Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.`, sem repetir a requisição. (AUT-06)
2. WHEN a entrada confirmada é recebida THEN App SHALL gravar token original em `token` e somente `id,nome,setor,email` em `usuario`, abrir Gerenciamentos e permitir recarregar a página com a mesma sessão; senha, hash e papel SHALL estar ausentes desse cache. (AUT-07)
3. IF o navegador rejeita gravação da sessão THEN App SHALL manter a tela de autenticação, tentar restaurar o cache anterior e mostrar `Não foi possível salvar sua sessão neste navegador. Permita salvar dados do site e entre pelo login.`, sem enviar outra requisição de autenticação. (AUT-08)
4. WHEN o usuário entra pelo login manual ou sai após cadastro THEN o sistema SHALL preservar entrada por credenciais válidas e remoção de token/usuario ao sair, retornando à tela de login. (AUT-09)
**Independent Test:** cliente HTTP e App com respostas/caches reais em jsdom; navegador confirma reload e logout/login.

### P1: Usar o formulário sem perder dados

**User Story:** como novo funcionário, quero enviar uma única inscrição e entender um erro.
**Why P1:** evita duplicações e abandono durante a operação.
**Acceptance Criteria**:
1. WHEN cadastro retorna uma sessão confirmada THEN Cadastro SHALL chamar a entrada compartilhada e mostrar Gerenciamentos diretamente, sem POST /login adicional nem exigir voltar ao login. Nome/setor/email enviados SHALL ter trim e senha SHALL manter o conteúdo original. (AUT-10)
2. WHILE o cadastro está pendente THEN o formulário SHALL impedir envios repetidos, desabilitar os cinco campos, submit e Voltar para login, e liberar todos os controles quando houver erro. (AUT-11)
3. IF senhas diferem ou possuem menos de seis caracteres THEN Cadastro SHALL mostrar respectivamente `As senhas não são iguais.` ou `A senha deve possuir pelo menos 6 caracteres.`, preservar os campos e não fazer requisição. Campos obrigatórios e email HTML SHALL manter validação nativa existente. (AUT-12)
4. IF cadastro recebe erro HTTP THEN Cadastro SHALL mostrar a mensagem de erro da API e preservar todos os campos sem entrar; se rede ou resposta 2xx não confirma a operação SHALL mostrar `Não foi possível confirmar o cadastro. Se a conta já foi criada, entre pelo login.`, sem retry automático ou login automático. (AUT-13)
5. WHEN o usuário navega pelo formulário THEN os cinco campos SHALL ter rótulos associados, ações SHALL funcionar por teclado e erros SHALL ter role alert; no fluxo real criar uma conta e um quadro, recarregar, sair e entrar manualmente SHALL preservar o ID da conta e o quadro criado. (AUT-14)
**Independent Test:** RTL cobre validação/pending/falhas, Playwright Edge/H2 percorre cadastro por teclado até dados persistidos.

## Edge Cases

- ID de outra conta ou ID escolhido não existente: AUT-03.
- Primeiro cadastro, cadastro seguinte e tentativa de papel ADMINISTRADOR: AUT-02.
- Email repetido, JSON inválido e save falhando: AUT-04. Não alegar rollback de uma falha de rede após gravação.
- Token ausente/vazio, ID inválido, campos públicos inválidos, setor null legítimo, JSON truncado/ilegível: AUT-06.
- Falha ao gravar a segunda chave no navegador: AUT-08; restauração é tentativa, não garantia caso o navegador recuse todas as operações.
- Senha de cinco/seis caracteres, espaços significativos na senha, mismatch: AUT-10/12.
- Duplo submit imediato antes do rerender, campos/voltar pendentes e erro liberando controles: AUT-11/13.
- Login manual e reload com cache seguro: AUT-07/09/14.

## Success Criteria

AUT-01–14 com assertions físicas e gates sem falhas/skips. Preservar todos os 238 Java/H2, 149 Vitest e 2 E2E atuais, adicionando casos pertinentes. Build/lint e novo E2E devem passar. Verificador independente exige evidência por critério e sensor de falhas em scratch; `validate_state.py autocadastro` deve sair 0 antes de declarar entrega técnica pronta. UAT humano é registrado separadamente.

## Requirement Traceability

| Requirements | Task | Status | Evidence |
| --- | --- | --- | --- |
| AUT-01 | T1 | complete | evidence.md, T1 |
| AUT-02 | T1 | complete | evidence.md, T1 |
| AUT-03 | T1 | complete | evidence.md, T1 |
| AUT-04 | T1 | complete | evidence.md, T1 |
| AUT-05 | T1 | complete | evidence.md, T1 |
| AUT-06 | T2 | pending | gate e assertions serão registrados em evidence.md |
| AUT-07 | T3, T5 | pending | evidence.md |
| AUT-08 | T3, T4 | pending | evidence.md |
| AUT-09 | T3, T5 | pending | evidence.md |
| AUT-10 | T4, T5 | pending | evidence.md |
| AUT-11 | T4 | pending | evidence.md |
| AUT-12 | T4 | pending | evidence.md |
| AUT-13 | T4 | pending | evidence.md |
| AUT-14 | T4, T5 | pending | evidence.md |
