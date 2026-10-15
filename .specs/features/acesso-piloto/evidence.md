# Evidências operacionais do piloto

## T1 iniciador implementado, ainda sem fechamento da feature

14/10/2026. Proposta do modo piloto aprovada explicitamente após spec validada. Passos: T1 iniciador/configuração/checks focados; T2 ciclo real com MySQL fictício e duas sessões; T3 guia/evidências. Verifier fresco ao final. Sem tasks/design separados pelo porte médio.

Arquivos T1: scripts/piloto-config.mjs, piloto-lan.mjs, piloto-config.test.mjs, package.json e WebConfig.java. Premissa: mesmo endereço para interface/API, build em cópia TEMP de src/main/POM para não tocar target real, MySQL original resolvido pelo Spring e override explícito quando fornecido. Interceptor limitado a /api/** para permitir HTML/assets; autenticação da API preservada e prova HTTP fica em T2. A documentação oficial Spring confirma index.html em static-locations como welcome page: https://docs.spring.io/spring-boot/reference/web/servlet.html.

Gate focado `npm run test:piloto`:6/6 PASS,0falhas/skips,92ms;lint PASS. Duas invocações até aqui, repetição após reforços de cleanup/credenciais apontados por revisão de código independente,sem falhas de teste. Runtime e prova Java/duas sessões aguardam T2,sem declarar piloto concluído.

| Critério / resultado | Evidência frontend/scripts/piloto-config.test.mjs | Coberto em T1 |
| --- | --- | --- |
| PIL-01/04 endereço local e porta | :11 deepEqual host/8082; :12 assert.throws para ausente,externo,wildcard,porta0/texto,argumento desconhecido/repetido | Sim, acesso real em T2 |
| PIL-02 MySQL local e configuração | :16 objeto de configuração exato; :17 override fictício; :18 throws para H2/remoto/banco vazio | Sim, persistência em T2 |
| PIL-03 validate/sem SQLinit | :22/23 args includes validate e never | Sim, banco incompatível em T2 |
| PIL-06 credenciais só servidor | :27 args.every semusername/password/url; :29/30 JSON contém sóURL e env mantém literal da senha fictícia | Sim, assets/auth em T2 |
| PIL-04/05 serviço ocupado preservado | teste Porta ocupada exige rejeição e servidor.listening true; nova reserva após fechar | Sim |
| PIL-05 limpeza confinada | teste Limpeza rejeita cwd e TEMPraiz,remove somente creral-piloto-* próprio | Sim |
| PIL-04 corrida/prontidão | teste Página própria aceita nonce correto,rejeita outra página e nonce diferente | Sim |

Mapeamento reverso: os seis testes correspondem às sete linhas acima; nenhuma asserção especulativa. Resultados esperados derivam dos8PIL. Revisão independente de código resolveu: preservação de credenciais escapadas pelo Spring, cancelamento durante cópia, identidade da própria página e preservação de TEMP se filho não confirma saída. Nenhum teste antigo alterado ou removido. T2 comprovará efeitos observáveis sem suite completa Java/MySQL/Vitest.

## Administrador inicial, 14/10/2026

**Concluído e verificado — tudo ok: configuração operacional do administrador inicial. Incluído no diagnóstico.** A funcionalidade de autorização já existia; não houve novo código ou mudança de regra. O acesso em rede como um todo ainda não está concluído.

Autorização: usuário escolheu Marco Polinski como primeiro administrador, informou marcoimmarquin@gmail.com e confirmou este computador como servidor central do primeiro piloto na mesma rede.

Identificação somente leitura: MySQL8.0.43, localhost:3306, banco sistema, servidor DESKTOP-ESO6LOG. Consulta dirigida retornou ID2, nome Marco Polinski, e-mail exato e papel FUNCIONARIO; contagem de administradores0. Nenhuma senha de usuário foi consultada.

Aplicação: transação com SELECT FOR UPDATE e UPDATE somente de papel, limitado por ID2, BINARY email exato, BINARY nome exato e papel anterior FUNCIONARIO/nulo. ROW_COUNT=1. Leitura da mesma conta retornou ADMINISTRADOR; COMMIT concluído; contagem final1. Nenhuma migração ou alteração de estrutura.

Conferência independente pelo agente auditoria_acesso_piloto, somente SELECT: mesma conta ID2/Marco Polinski/marcoimmarquin@gmail.com com ADMINISTRADOR e exatamente1 administrador no banco local. Código de cadastro mantém FUNCIONARIO em UsuarioService.java:29. Não houve login em nome da conta ou acesso à sua senha. Conferência do uso pela interface ainda faz parte do teste humano.

Credenciais do banco usadas somente em memória para o cliente MySQL, sem senha em argumentos ou evidências. Nenhuma outra conta promovida/demovida; sem novo teste automatizado ou suíte completa por ser aplicação operacional de permissão existente, conferida por leitura antes/depois e auditor independente. Nenhum sensor ou dado fictício aplicado neste banco.

## Rede ainda não habilitada

Auditoria de leitura: frontend/src/services/api.js usa VITE_API_URL e padrão localhost:8081/api; CorsConfig.java:14 permite localhost:5173. Serviço atual de interface5173 ouve loopback. Os atuais serviços3306/8081/5173 não foram interrompidos ou reconfigurados. Computadores externos ainda não tiveram acesso verificado.
