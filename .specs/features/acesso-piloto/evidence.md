# Evidências operacionais do piloto

## Administrador inicial, 14/10/2026

**Concluído e verificado — tudo ok: configuração operacional do administrador inicial. Incluído no diagnóstico.** A funcionalidade de autorização já existia; não houve novo código ou mudança de regra. O acesso em rede como um todo ainda não está concluído.

Autorização: usuário escolheu Marco Polinski como primeiro administrador, informou marcoimmarquin@gmail.com e confirmou este computador como servidor central do primeiro piloto na mesma rede.

Identificação somente leitura: MySQL8.0.43, localhost:3306, banco sistema, servidor DESKTOP-ESO6LOG. Consulta dirigida retornou ID2, nome Marco Polinski, e-mail exato e papel FUNCIONARIO; contagem de administradores0. Nenhuma senha de usuário foi consultada.

Aplicação: transação com SELECT FOR UPDATE e UPDATE somente de papel, limitado por ID2, BINARY email exato, BINARY nome exato e papel anterior FUNCIONARIO/nulo. ROW_COUNT=1. Leitura da mesma conta retornou ADMINISTRADOR; COMMIT concluído; contagem final1. Nenhuma migração ou alteração de estrutura.

Conferência independente pelo agente auditoria_acesso_piloto, somente SELECT: mesma conta ID2/Marco Polinski/marcoimmarquin@gmail.com com ADMINISTRADOR e exatamente1 administrador no banco local. Código de cadastro mantém FUNCIONARIO em UsuarioService.java:29. Não houve login em nome da conta ou acesso à sua senha. Conferência do uso pela interface ainda faz parte do teste humano.

Credenciais do banco usadas somente em memória para o cliente MySQL, sem senha em argumentos ou evidências. Nenhuma outra conta promovida/demovida; sem novo teste automatizado ou suíte completa por ser aplicação operacional de permissão existente, conferida por leitura antes/depois e auditor independente. Nenhum sensor ou dado fictício aplicado neste banco.

## Rede ainda não habilitada

Auditoria de leitura: frontend/src/services/api.js usa VITE_API_URL e padrão localhost:8081/api; CorsConfig.java:14 permite localhost:5173. Serviço atual de interface5173 ouve loopback. Os atuais serviços3306/8081/5173 não foram interrompidos ou reconfigurados. Computadores externos ainda não tiveram acesso verificado.
