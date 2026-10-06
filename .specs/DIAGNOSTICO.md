# Diagnóstico inicial do sistema Creral

Data: 2026-10-04. Branch analisada: `testes`. Base: `5d8beb9`.

A leitura do código confirma uma implementação parcial do gerenciador de etapas. O botão de criar processo está sem ação no frontend. Nenhuma funcionalidade de aplicação foi alterada nesta análise. As regras de negócio abaixo ainda precisam ser esclarecidas com o usuário.

Atualização em 2026-10-05: o CRUD de gerenciamentos está implementado e validado tecnicamente, com 41/41 critérios. A API e interface permitem criar/consultar/editar/arquivar/restaurar, aplicam permissões e protegem os registros associados. Seus gates foram 97 testes H2, 71 testes da interface e 1 fluxo real no Edge; houve também 96 testes de comportamento em MySQL temporário e verificação de schema antigo/reinicialização. Relatório: `features/gerenciamentos/validation.md`.

O CRUD de etapas de trabalho também está implementado e validado, 30/30 critérios na rodada 2. Criador/admin cria, renomeia, muda setor, escolhe posição e remove etapas vazias. A ordem das demais colunas se ajusta automaticamente; demandas bloqueiam remoção. Arquivados ficam somente consulta. Gates dessa entrega: 238 Java/H2, 149 testes da interface, build/lint e 2 fluxos Edge PASS; MySQL temporário, 237 testes PASS. A revisão independente detectou as cinco falhas simuladas, incluindo o falso sucesso com resposta incompleta que motivou T7. Uma falha anterior de conexão E2E permanece documentada, com causa indeterminada; a repetição diagnóstica e a execução independente passaram sem retries. Relatório: `features/etapas/validation.md`.

A entrada automática após cadastro está implementada e validada independentemente, 14/14 critérios. O servidor devolve sessão da conta salva; cadastro e login usam a mesma gravação segura no navegador. Cadastro confirmado abre Gerenciamentos sem segundo login. Pending bloqueia envios duplicados e erros preservam campos; respostas não confirmadas orientam login caso a conta já exista. Papel inicial permanece FUNCIONARIO, sem senha/hash na resposta/cache. Gates atuais: 247 Java/H2, 222 testes da interface, build/lint e 3 E2E Edge PASS. Nove casos também passaram em MySQL TEMP. O Verificador detectou 7/7 falhas simuladas e confirmou porcelain integral byteigual. A cópia temporária restaurada foi preservada porque sua limpeza foi rejeitada automaticamente. Relatório: `features/autocadastro/validation.md`.

A retirada de PM do login e cadastro também foi implementada e verificada independentemente, 4/4 critérios. Os dois blocos e seus estilos exclusivos foram removidos; os títulos iniciam os cartões, sem bloco vazio. As duas telas passaram em desktop e celular, sem rolagem horizontal. Gates próprios: 222 testes da interface, build/lint e 3 E2E Edge/H2 PASS. O sensor independente detectou 3/3 falhas visuais simuladas em cópias executadas em memória e confirmou que o projeto permaneceu intacto. Java/MySQL não foram repetidos nesta alteração de apresentação. Relatório: `features/marca-login/validation.md`.

O teste de uso humano permanece sem resultado; autorização para continuar não equivale a teste aprovado. Para experimentar com dados fictícios, ver `../docs/OPERACAO.md`. Demais requisitos continuam no backlog, inclusive o botão Criar processo. AD-001 a AD-011 estão em `STATE.md`; Concluídos/Cancelados terão destino automático na próxima entrega, com perguntas de movimentação e dados antigos ainda pendentes.

As tabelas e linhas de código abaixo registram o diagnóstico da base `5d8beb9`; não descrevem como pendentes os reparos já comprovados nos relatórios atuais.

## Escopo solicitado

O usuário pediu implementação incremental, um requisito por vez, com testes, usando a skill local `tlc-spec-driven`. Pediu explicações simples e perguntas sobre as decisões que faltarem. O sistema será usado pela empresa de energia Creral, conforme nome confirmado pelo usuário. Java, Spring Boot e MySQL são tecnologias indicadas pelo usuário; Workbench será a ferramenta de acesso ao banco.

| Pedido | Situação observada no código | Trabalho necessário |
| --- | --- | --- |
| CRUD de gerenciamentos | Backend cria, lista, busca e edita. Frontend cria, lista e abre. | Implementar arquivamento/restauração e permissões escolhidas; completar API e interface conforme a especificação em revisão. |
| CRUD de etapas | Backend tem CRUD. Frontend só configura etapas ao criar gerenciamento. | Permitir manutenção no quadro; definir regras para etapas ocupadas e ordem. |
| Concluir/cancelar a qualquer momento, escolhendo destino | Backend altera status/data, mas não a etapa. Frontend não oferece essas ações. | Definir destinos permitidos e regras após finalização; manter status e etapa coerentes. |
| CRUD de projetos/processos e botão criar | Backend tem CRUD de processos. Frontend não cria nem carrega processos. | Definir projeto versus processo e campos; conectar formulário, listagem, edição e exclusão. |
| Pular etapas | Backend permite qualquer etapa do mesmo gerenciamento. Frontend não permite movimentação. | Definir retorno, finalização e restrições; conectar movimentação à interface. |
| Etapas obrigatórias Concluídos e Cancelados | Não são criadas automaticamente; etapa não tem categoria. | Definir relação com escolha do destino; preservar as etapas obrigatórias. |
| Lateral de comentários de vários usuários com data | Backend tem comentários por processo com data/hora. Frontend não apresenta comentários. | Definir se conversa é do gerenciamento, do item ou de ambos; identificar autor pela sessão. |
| Gráficos, se possível | Nenhuma implementação encontrada. | Definir indicadores úteis e seus filtros; implementar após dados e regras. |
| Entrar no sistema após cadastro | Cadastro salva usuário; frontend permanece na tela de cadastro. | Criar sessão e navegar após cadastro bem-sucedido; definir quem pode se cadastrar. |
| Interface geral e retirar PM de login/cadastro | PM está presente nas duas telas. | Remover marca solicitada e definir identidade visual da Creral. |
| Logs em cada gerenciamento | Histórico atual é por processo; não há histórico de gerenciamento/etapa. | Definir eventos e visibilidade; registrar autor e data de forma confiável. |

CRUD significa criar, consultar/listar, editar e excluir. Arquivar, quando escolhido, significa retirar da lista de ativos preservando dados e histórico.

## Evidências principais

Os caminhos são relativos à raiz do projeto.

- `frontend/src/pages/Quadro.jsx:83`: botão Criar processo sem `onClick` ou formulário.
- `frontend/src/pages/Quadro.jsx:18`: quadro busca somente etapas.
- `frontend/src/pages/Quadro.jsx:127`: quantidade fixa em zero; não representa dados do banco.
- `frontend/src/services/api.js:1`: API fixa em localhost. Serviços atuais cobrem autenticação e listar/criar gerenciamentos.
- `frontend/src/pages/Gerenciamentos.jsx:89`: criar gerenciamento; sem controles de edição/exclusão.
- `frontend/src/pages/CriarGerenciamento.jsx:19`: etapas configuradas durante criação.
- `sistema/src/main/java/com/patp/sistema/controller/GerenciamentoController.java:29`: API sem exclusão de gerenciamento.
- `sistema/src/main/java/com/patp/sistema/controller/EtapaController.java:27`: CRUD de etapas já existente.
- `sistema/src/main/java/com/patp/sistema/controller/ProcessoController.java:29`: API de processos já existente.
- `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:177`: mudança para qualquer etapa do mesmo gerenciamento.
- `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:228`: concluir e cancelar não alteram etapa.
- `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:26`: criação não adiciona etapas finais obrigatórias nem atribui criador.
- `sistema/src/main/java/com/patp/sistema/model/Etapa.java:20`: etapa contém nome, setor, ordem e gerenciamento; sem categoria terminal.
- `sistema/src/main/java/com/patp/sistema/model/Processo.java:22`: número único global e pessoa obrigatórios no modelo atual; isso não confirma a regra desejada pela Creral.
- `sistema/src/main/java/com/patp/sistema/controller/ComentarioController.java:25`: comentários por processo; nome do autor recebido do cliente.
- `sistema/src/main/java/com/patp/sistema/service/HistoricoService.java:27`: histórico vinculado ao processo.
- `sistema/src/main/java/com/patp/sistema/controller/UsuarioController.java:31`: cadastro não cria sessão.
- `frontend/src/pages/Cadastro.jsx:49`: cadastro não autentica nem redireciona.
- `frontend/src/pages/Login.jsx:70` e `frontend/src/pages/Cadastro.jsx:91`: marca PM.
- `sistema/src/main/java/com/patp/sistema/service/SessaoService.java:13`: sessões guardadas somente na memória do backend.
- `sistema/src/test/java/com/patp/sistema/SistemaApplicationTests.java:6`: único teste encontrado, carrega o contexto sem verificar uma regra de negócio.

## Lacunas técnicas ligadas aos pedidos

- Não há entidade Projeto nem distinção de tipo no processo. Não presumir que os termos são equivalentes.
- Não há papéis ou participantes por gerenciamento. O código permite acesso geral aos usuários autenticados; isso não confirma a autorização desejada.
- Concluir depois de cancelar, ou cancelar depois de concluir, não limpa os dados do estado anterior. Editar também permite alterar status livremente.
- Movimentação não bloqueia itens finalizados nem distingue movimentar de reabrir.
- Exclusão de processo registra histórico vinculado antes de excluir. Referências de comentários/históricos podem impedir exclusão. Essa falha não foi reproduzida no banco.
- Exclusão de etapa não define tratamento amigável para processos existentes.
- Gravação do item e de seu histórico não é transacional. Criação do gerenciamento e suas etapas também não é transacional.
- Cadastro e busca de usuário por ID retornam a entidade com getter público da senha armazenada. As respostas precisam preservar somente dados apropriados ao cliente.
- Histórico manual aceita autor/ação enviados pelo cliente. Auditoria confiável precisa derivar autoria da sessão e definir quais eventos são automáticos.
- Não há testes frontend, perfil de banco isolado de teste ou migrações encontrados.

## Verificações executadas

| Verificação | Resultado | Limite da evidência |
| --- | --- | --- |
| `npm.cmd run build`, em frontend | PASS | Compila a interface; não verifica comportamento dos botões nem comunicação com banco. |
| `npm.cmd exec -- oxlint src vite.config.js`, em frontend | PASS, sem diagnósticos | Analisa os arquivos da aplicação; não comprova regras de negócio. |
| `mvn.cmd -B test-compile`, em sistema | PASS | Compila fontes e testes; Maven informou arquivos atualizados. Não executa testes. |

O comando padrão `npm.cmd run lint` também foi executado, mas percorreu dependências em `node_modules` e gerou muitos avisos externos. A análise restrita aos arquivos do projeto está registrada acima.

Na coleta inicial, o teste `contextLoads` não havia sido executado. A configuração padrão usa MySQL com `ddl-auto=update`; a implementação posterior estabeleceu H2 isolado de teste e uma instância MySQL temporária. O banco configurado na aplicação não foi consultado nem alterado.

O build gerou `frontend/dist/`. A revisão automática recusou a remoção dessa pasta por política. O artefato permaneceu no workspace.

## Decisões a esclarecer, por dependência

Esta lista organiza futuras conversas; não é uma especificação aprovada nem uma lista de regras assumidas.

1. **Vocabulário e exemplo de uso:** o que é gerenciamento; se projeto e processo são iguais ou diferentes; exemplo de um item e suas etapas na Creral.
2. **Acesso:** quem utiliza; quem pode criar/editar/excluir gerenciamentos, etapas e itens; quem pode ver cada gerenciamento.
3. **Primeiro CRUD:** dados do gerenciamento; limites de nomes/descrições; significado de excluir; tratamento quando há itens e histórico.
4. **Etapas:** ordem, setor, alterações durante uso, exclusão de etapa ocupada e criação automática das finais.
5. **Destinos finais:** esclarecer duas etapas obrigatórias versus escolha de uma etapa específica; se há exatamente uma de cada categoria ou vários destinos.
6. **Dados do item:** nome/número/cliente/pessoa/responsável/prazos/prioridade; obrigatoriedade; geração e alcance da unicidade do número; etapa inicial.
7. **Movimentação:** pular, voltar, escolher destino, atravessar gerenciamentos, repetir destino e comportamento após conclusão/cancelamento.
8. **Finalização:** motivo obrigatório de cancelamento; possibilidade e permissão de reabertura; edição e comentários após finalização; manutenção de datas e histórico.
9. **Comentários:** lateral do gerenciamento versus item; leitores/autores; texto, ordenação, data/hora, edição/exclusão e preservação até depois da finalização.
10. **Registros de atividade:** eventos, antes/depois, autoria, leitores, filtros e preservação após exclusão/arquivamento.
11. **Cadastro e sessão:** cadastro aberto versus convite/aprovação; acesso imediato; recuperação de sessão inválida; regras necessárias para uso interno.
12. **Interface e gráficos:** identidade Creral, dispositivos usados, comportamento de telas vazias/erros, indicadores e períodos relevantes.
13. **Validação com usuários:** exemplo fictício representativo, ambiente de teste, tratamento de dados existentes e critérios objetivos para aceitar cada requisito.

As questões técnicas descobríveis pelo código serão resolvidas pela análise do projeto. Não pedir senhas ou credenciais no chat. As decisões de negócio serão explicadas com exemplos e registradas quando respondidas.

## Forma de execução

- Esclarecer as regras do requisito atual em blocos pequenos, conforme preferência do usuário.
- Registrar critérios objetivos de aceitação e decisões antes de implementar.
- Escrever testes que verifiquem esses critérios e usar ambiente isolado.
- Entregar uma funcionalidade completa na API e interface, verificar os testes e registrar uma mudança local por tarefa conforme a skill.
- Fazer verificação independente após finalizar a funcionalidade, incluindo evidências exigidas pela skill.
- Avançar ao próximo requisito mantendo a cobertura dos pedidos da tabela inicial.

Gerenciamentos, etapas de trabalho, entrada após cadastro e retirada de PM já foram entregues e verificados, um requisito por vez. As duas etapas finais obrigatórias e os fluxos relacionados seguirão AD-011 e as respostas às perguntas pendentes. Campos e interface de demandas, comentários, logs, gráficos e demais melhorias de aparência terão suas próprias entregas e testes.
