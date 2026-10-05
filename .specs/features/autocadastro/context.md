# Entrada após cadastro: contexto

O usuário pediu que o cadastro já direcione ao sistema, sem voltar para fazer login. A implementação foi autorizada na conversa e reiterada em 2026-10-05. Este requisito pode avançar enquanto as perguntas de negócio sobre movimentação aguardam resposta.

## Evidência do código

- `UsuarioController.cadastrar` devolve o usuário salvo, sem sessão. `login` já devolve `LoginResponse(token,id,nome,setor,email,papel)`.
- `UsuarioService.cadastrar` rejeita ID enviado, força FUNCIONARIO e grava senha BCrypt. `SessaoService` mantém tokens em memória associados ao ID salvo.
- `Cadastro` ignora a resposta e mostra sucesso no próprio formulário. `Login` grava token/usuário no navegador; `App.entrar` apenas troca a tela.
- O cliente HTTP já diferencia erros HTTP e respostas ilegíveis. Uma aplicação antiga ainda pode devolver cadastro sem token; não interpretar essa resposta como entrada válida.

## Decisões para esta entrega

Reusar a sessão e o DTO do login. Preservar HTTP 200 do cadastro, os campos públicos existentes e as regras do serviço. Gravar a sessão no ponto comum de entrada de App, usado por login e cadastro. Nenhuma nova tabela, dependência ou política de acesso.

Setor nulo continua aceitável na resposta de contas antigas. O cache mantém somente id/nome/setor/email, como hoje; o papel vem do servidor nas consultas. Não preencher dados desconhecidos.

Uma resposta não confirmada não garante que a conta deixou de ser criada. Mostrar orientação para login, preservar os campos e não repetir automaticamente a inscrição. Se o navegador rejeitar a gravação da sessão, não abrir o sistema e tentar restaurar os valores anteriores. Recuperação de cache inválido já presente antes de abrir App fica fora desta entrega.

## Deferred Ideas

- Retirar PM e melhorar o visual das telas será um requisito separado.
- Convites, recuperação de senha, validação adicional no servidor, expiração persistente, cookies e alteração da política de cadastro não foram pedidos para esta entrega.
- Etapas finais, demandas, comentários, gráficos e logs continuam no backlog. As cinco perguntas de movimentação e os testes humanos anteriores continuam pendentes.

## Segurança operacional

Testes Java usam H2; navegador usa helper isolado com H2 e contas fictícias. Não usar o MySQL configurado, alterar contas reais, reiniciar os serviços do usuário ou publicar commits. Artefatos gerados e alterações anteriores ficam preservados.
