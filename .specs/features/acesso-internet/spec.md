# Acesso pela internet Specification

**Data:** 14/10/2026. **Status:** Approved para preparação local após escolhas explícitas e pedido de continuar para acesso pelo celular com dados móveis. Porte grande: hospedagem, persistência e autorização de cadastros. Publicação externa ainda não realizada; contas Render/Aiven ainda serão criadas pelo usuário.

## Problem Statement

O piloto funciona em outro computador do mesmo Wi-Fi, conforme relato do usuário. Agora estudantes precisam acessar por computador ou celular de outras redes. Colocar somente a interface na Vercel não disponibiliza a API e o banco locais; além disso, o cadastro atual permite a qualquer inscrito consultar os quadros.

## Goals

- Disponibilizar o piloto acadêmico por HTTPS, usando React na Vercel, Java/Spring Boot no Render e MySQL no Aiven.
- Começar com uma base nova de testes e liberar acesso aos dados somente após aprovação administrativa da conta.
- Preparar arquivos, instruções e provas focadas, preservando o piloto local e distinguindo preparação técnica de publicação/acesso externo observados.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Importação dos dados locais | Usuário escolheu base nova de testes. |
| Ambiente definitivo dos empregados e garantia de disponibilidade | Esta etapa usa planos gratuitos para o piloto acadêmico. |
| Novo papel Gerente, edição de papéis e bloqueio de contas aprovadas | Pedido atual é autorizar novos cadastros; admins iniciais escolhidos explicitamente. |
| Convites por e-mail, recuperação de senha, MFA e regras próprias de antifraude | Não necessários para a aprovação manual do piloto; proteção de infraestrutura permanece a dos provedores. |
| Rejeição/exclusão de cadastros e aprovação em lote | Aprovação individual atende a primeira versão; contas não aprovadas continuam pendentes. |
| Mudança automática no MySQL local, firewall ou roteador | Publicação usa serviços separados em nuvem. |
| Contratação, push ou publicação sem etapa explícita | Spec autoriza preparação local; ações externas serão feitas no ambiente selecionado, com autorização correspondente. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Provedores | Vercel + Render + Aiven, faixas gratuitas | Escolha explícita após explicar limites | Sim |
| Dados iniciais | Base nova de testes, sem copiar dados/contas locais | Escolha explícita do usuário | Sim |
| Cadastro externo | Exigir aprovação do administrador antes de entrar/consultar | Escolha explícita do usuário | Sim |
| Administradores da base nova | Novas contas controladas dos e-mails marcoimmarquin@gmail.com e admin@gmail.com, promovidas por procedimento explícito do operador | São os admins escolhidos; base nova não contém essas contas, sem promoção automática por e-mail | Proposto |
| Aprovação inicial | Lista de pendentes e ação Aprovar individual, sem rejeitar/apagar | Menor fluxo que implementa a decisão recebida | Proposto |
| Isolamento local | Aprovação exigida somente no modo internet; não alterar schema/dados do MySQL local | Preservar piloto e cadastro direto já entregues | Proposto |
| Sessão online | Oito horas a partir da criação e revogação ao sair; novo login após reinício do Render | Limite explícito para o acesso público e compatibilidade com uma instância gratuita | Proposto |
| Endereços/contas dos provedores | Definidos na etapa de publicação assistida, após preparo local | São recursos externos ainda não configurados | Pendente operacional, não impede especificação |
| Infraestrutura gratuita | Sujeita a limites, inatividade e regras dos provedores; sem promessa de uso empresarial grátis | Condições oficiais documentadas em context.md | Informado |

**Open questions:** none — decisões de dados/cadastro resolvidas e demais defaults explicitados acima para confirmação do conjunto. URLs e recursos externos serão definidos durante a publicação assistida.

## User Stories

### P1: Acessar de outras redes

**User Story:** Como estudante autorizado, quero acessar pelo navegador de um computador ou celular de outra rede, usando os mesmos dados online.

**Acceptance Criteria:**

1. **NET-01:** WHEN o piloto é publicado nos provedores escolhidos THEN o sistema SHALL disponibilizar interface e API por HTTPS, com React/Vercel, Java/Spring Boot/Render e MySQL/Aiven, sem depender deste computador ligado.
2. **NET-02:** The build público SHALL usar endereço HTTPS da API online e conter somente configuração pública, sem localhost, credenciais locais, senha de banco ou arquivo de certificado privado nos assets/imagem final.
3. **NET-03:** IF configuração obrigatória de banco, TLS ou origem pública estiver ausente/inválida THEN o modo internet SHALL recusar a inicialização com mensagem identificável e sem tentar o banco local; credenciais SHALL permanecer fora das mensagens. O artefato Render SHALL ativar obrigatoriamente o modo internet, sem cair no cadastro direto local por ausência de configuração.
4. **NET-04:** WHILE o modo internet opera o sistema SHALL validar a estrutura sem DDL/SQL de inicialização automático e conectar ao MySQL com verificação da identidade do servidor TLS.
5. **NET-05:** WHEN a base nova é preparada explicitamente THEN o procedimento SHALL criar somente a estrutura no banco online indicado, sem importar contas/dados locais ou executar DROP; existência de qualquer tabela de usuário no schema alvo SHALL ser recusada antes da primeira criação, inclusive estrutura parcial de tentativa anterior.
6. **NET-06:** WHEN o serviço online reinicia THEN contas aprovadas, demandas e histórico salvos SHALL permanecer no MySQL; sessões em memória poderão exigir novo login, conforme aviso do guia.
7. **NET-07:** WHILE o sistema usa os modos local/piloto existentes THEN cadastro direto, regras e estrutura de banco atuais SHALL permanecer compatíveis, sem exigir novas tabelas/colunas no validate desses modos ou executar migração; aprovação online SHALL ser acessada somente pela política explícita do modo internet.

**Independent Test:** Conferir build sem segredos, inicialização sem configuração, TLS/estrutura, base fictícia vazia/não vazia e reinício. Após publicação, abrir URL HTTPS por uma rede externa e conferir interface/API/dados.

### P1: Autorizar novas contas online

**User Story:** Como administrador, quero aprovar os novos cadastros antes que consultem os dados da equipe.

**Acceptance Criteria:**

8. **NET-08:** WHEN uma pessoa se cadastra no modo internet THEN o sistema SHALL criar funcionário pendente e responder HTTP202 com estado PENDENTE e mensagem de espera, sem token, ignorando papel/aprovação enviados pelo cliente.
9. **NET-09:** WHILE uma conta online está pendente o login com senha correta SHALL responder HTTP403 e mensagem Cadastro aguardando aprovação do administrador, sem criar sessão; chamadas protegidas sem sessão válida SHALL continuar HTTP401.
10. **NET-10:** WHEN um administrador aprovado consulta os pendentes THEN a API SHALL responder HTTP200 com lista por ID crescente, contendo somente ID, nome, setor e e-mail; sem sessão SHALL responder401 e funcionário aprovado SHALL receber403.
11. **NET-11:** WHEN um administrador aprovado aprova uma conta existente THEN a API SHALL responder HTTP200 com ID e estado APROVADO; repetir a ação SHALL preservar autoria/horário da primeira aprovação; ID inexistente SHALL responder404 e funcionário/sem sessão SHALL receber403/401 sem alteração.
12. **NET-12:** WHEN cadastro ou aprovação comum falha na persistência THEN o sistema SHALL preservar a integridade da transação, sem conta parcialmente cadastrada ou aprovação parcial; aprovação comum SHALL conservar senha, dados e papel da conta. A concessão explícita de ADMINISTRADOR pertence somente ao procedimento separado de NET-15.
13. **NET-13:** WHEN o cadastro online recebe HTTP202 THEN a interface SHALL mostrar que aguarda autorização, sem entrar ou gravar sessão; envio duplicado SHALL permanecer bloqueado e falha SHALL conservar os campos, sem retry automático.
14. **NET-14:** WHILE um administrador aprovado usa o ambiente online a interface SHALL oferecer consulta de pendentes e ação Aprovar individual, com carregamento, vazio e erro explícitos; resposta incerta SHALL conservar a lista e orientar atualização, sem declarar aprovação confirmada.
15. **NET-15:** WHEN o operador prepara os primeiros administradores da base nova THEN o procedimento SHALL exigir escolha explícita da conta recém-cadastrada e conferida por ID/e-mail, concedendo acesso aprovado e papel administrador somente às contas escolhidas; nenhum cadastro público SHALL promover por coincidência de e-mail.
16. **NET-16:** WHEN a sessão online completa oito horas desde sua criação THEN a API SHALL recusar seu token com HTTP401, independentemente de uso no intervalo.
17. **NET-17:** WHEN o usuário escolhe Sair no modo internet THEN a interface SHALL solicitar revogação do token e remover a sessão local; falha de comunicação SHALL informar que a revogação não foi confirmada, sem repetir automaticamente a operação.

**Independent Test:** Em MySQL fictício separado, cadastrar pendente, provar ausência de sessão/acesso, preparar admin de fixture, listar/aprovar como admin e recusar funcionário/anon; entrar depois, conferir persistência/idempotência, expiração e saída. Interface com estados reais em computador/celular e dois contextos de navegador.

### P1: Publicar com estado e limites compreensíveis

**User Story:** Como responsável pelo piloto, quero arquivos e instruções que permitam publicar nos planos escolhidos e saber o que foi comprovado.

**Acceptance Criteria:**

18. **NET-18:** The guia SHALL explicar configuração e publicação dos três serviços, HTTPS/origem, preparo da base vazia, primeiros admins, aprovação de funcionários, limites gratuitos/inatividade, reinício e diagnóstico, sem pedir senhas pessoais no chat ou contratar plano pago.
19. **NET-19:** WHEN a publicação ocorre THEN a verificação SHALL registrar URL HTTPS, consulta de API protegida, login autorizado e acesso de computador/celular fora da LAN como observado ou pendente; testes locais SHALL não ser apresentados como prova desse acesso externo.
20. **NET-20:** WHEN um requisito é implementado e passa seus gates THEN o diagnóstico SHALL registrar seu estado e evidência; conclusão técnica SHALL depender do Verifier independente, mantendo publicação e aceite reais pendentes até observados.

**Independent Test:** Inspeção do guia e rastreabilidade; execução assistida na nuvem após preparo. Relato externo do usuário registrado separadamente de automação local.

## Edge Cases

- NET-03/04: configuração ausente, certificado não confiável e estrutura incompatível recusam inicialização sem fallback para dados locais.
- NET-05: preparação recusa banco não vazio; sem importar os administradores locais por suposição. Falha após DDL parcial informa preparação não concluída e tabelas existentes, sem tentar rollback destrutivo ou liberar o serviço; repetição recusa a estrutura parcial.
- NET-08/09/12: corpo tenta escolher papel/aprovação, falha de gravação e conta pendente não conseguem sessão.
- NET-10/11/14: listas vazias, alvos inexistentes, aprovação repetida/concorrente e resultado incerto preservam autorização e dados.
- NET-16/17: expiração, reinício e falha ao sair têm estados explícitos; nenhuma sessão é prometida após reinício do Render.
- Dimensões: entradas/limites, falhas, idempotência, autenticação, concorrência de aprovação, ciclo da sessão, logs sem segredos e indisponibilidade externa cobertos acima. Alterações em transições de demandas N/A porque regras de negócio existentes são preservadas; novas regras de frequência de negócio N/A porque não foram pedidas e esta entrega usa os limites de infraestrutura dos provedores.

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| NET-01 | P1: Outras redes | Specify | Pending |
| NET-02 | P1: Outras redes | Specify | Pending |
| NET-03 | P1: Outras redes | Specify | Pending |
| NET-04 | P1: Outras redes | Specify | Pending |
| NET-05 | P1: Outras redes | Specify | Pending |
| NET-06 | P1: Outras redes | Specify | Pending |
| NET-07 | P1: Outras redes | Execute | Implementing |
| NET-08 | P1: Autorizar contas | Execute | Implementing |
| NET-09 | P1: Autorizar contas | Specify | Pending |
| NET-10 | P1: Autorizar contas | Execute | Implementing |
| NET-11 | P1: Autorizar contas | Execute | Implementing |
| NET-12 | P1: Autorizar contas | Execute | Implementing |
| NET-13 | P1: Autorizar contas | Specify | Pending |
| NET-14 | P1: Autorizar contas | Specify | Pending |
| NET-15 | P1: Autorizar contas | Specify | Pending |
| NET-16 | P1: Autorizar contas | Specify | Pending |
| NET-17 | P1: Autorizar contas | Specify | Pending |
| NET-18 | P1: Publicação | Specify | Pending |
| NET-19 | P1: Publicação | Specify | Pending |
| NET-20 | P1: Publicação | Specify | Pending |

## Success Criteria

- [ ] Preparo técnico permite iniciar API e interface com MySQL online separado, sem segredos no build e sem alterar o piloto local.
- [ ] Pendente não acessa; administrador aprova; funcionário aprovado entra e usa dados persistentes.
- [ ] Gates focados e Verifier independente aprovados; sem repetir suítes históricas, conforme AD-035.
- [ ] Guia e diagnóstico corretos; publicação/acesso externo registrados somente quando observados.
