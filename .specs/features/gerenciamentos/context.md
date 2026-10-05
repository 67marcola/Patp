# CRUD de gerenciamentos da Creral: contexto

**Gathered:** 2026-10-04
**Spec:** `spec.md`.
**Status:** esclarecimentos concluídos; contexto e especificação prontos para revisão.

## Feature Boundary

Completar a criação, consulta, edição e retirada de uso dos gerenciamentos na API e interface. O gerenciamento é o quadro que reúne etapas e várias demandas, conforme AD-001 de `.specs/STATE.md`.

A retirada de uso será por arquivamento, conforme escolha 2A. O gerenciamento sairá da lista de ativos; demandas, comentários e histórico serão preservados e haverá restauração.

## Implementation Decisions

### Modelo confirmado

- O usuário escolheu explicitamente a opção A: várias instalações/demandas no mesmo gerenciamento.
- Cada demanda tem sua própria etapa atual; mover uma demanda não move as demais.
- Exemplo: o quadro Instalação de postes acompanha instalações para casas diferentes; suas fases incluem compra, verificação do local e instalação.
- Pular etapas será uma ação por botão na demanda escolhida. Sua implementação pertence ao requisito de movimentação posterior.
- "Demanda" é o termo provisório da conversa. O nome da interface ainda não foi decidido.

### Permissões confirmadas (1A)

- Todos os usuários logados podem visualizar e criar gerenciamentos.
- Apenas o criador de cada gerenciamento ou um administrador pode editar o quadro e retirá-lo de uso.
- A escolha foi registrada em AD-002 de `.specs/STATE.md`.
- O criador deverá ser identificado pela sessão autenticada na criação.
- O papel administrativo será atribuído a contas escolhidas pelo usuário, conforme a opção A confirmada e AD-004. Cadastros comuns terão papel de funcionário.
- As contas específicas ainda não foram indicadas; o mecanismo técnico de atribuição será definido na implementação. Não criar administrador automático por ordem de cadastro.
- Estas permissões tratam dos gerenciamentos; permissões para etapas e demandas serão esclarecidas depois.

### Retirada de uso confirmada (2A)

- Arquivar o gerenciamento, preservando seus dados e vínculos, em vez de excluir fisicamente.
- O quadro arquivado sai da lista de ativos.
- Demandas, comentários e histórico permanecem armazenados.
- O gerenciamento pode ser restaurado.
- A escolha foi registrada em AD-003 de `.specs/STATE.md`.
- O usuário confirmou somente consulta no estado arquivado, conforme AD-005. Alterar o quadro, suas etapas/demandas, movimentar/finalizar itens e adicionar comentários exige restauração.
- Restauração pelo criador/admin e filtro Ativos / Arquivados são padrões explicitamente propostos em `spec.md`, aguardando sua revisão conjunta.

### Proposta explícita para restauração

- Aplicar à restauração a mesma permissão administrativa já escolhida para edição/arquivamento: criador do quadro ou administrador.
- A justificativa é manter a responsabilidade administrativa do quadro durante todo o ciclo de vida.
- Esta é uma proposta derivada de 1A, não uma resposta expressa do usuário; registrar como padrão explícito na especificação e permitir ajuste na revisão.

### Propostas baseadas no código existente

- Nome obrigatório e descrição opcional para o gerenciamento, mantendo os campos atuais.
- Nomes repetidos permitidos, mantendo o modelo existente; quadros continuam separados por ID e nunca compartilham demandas por terem o mesmo nome.
- Lista com filtro Ativos / Arquivados, mostrando ativos inicialmente e permitindo consulta dos arquivados.
- Limites dos campos, validação, proteção contra envio duplicado e contratos de erro serão definidos tecnicamente com critérios de teste precisos.
- Essas propostas serão apresentadas na especificação como padrões explícitos; não foram respostas individuais do usuário.

### Restrições confirmadas

- Java, Spring Boot e MySQL.
- Implementar um requisito por vez, com testes.
- Usar a skill `tlc-spec-driven` para critérios de aceitação, tarefas e verificação.
- O artigo PATP é referência; decisões atuais do usuário prevalecem.
- O nome da empresa é Creral, conforme correção expressa do usuário.

### Código existente

- Gerenciamento possui nome, descrição e associação com criador; o serviço ainda não atribui o criador.
- Backend tem criar/listar/buscar/editar; não tem excluir.
- Frontend tem criar/listar/abrir; não tem editar/retirar de uso.
- Usuários autenticados têm acesso geral atualmente. Não existe papel de administrador no modelo.
- Comentários e históricos atuais pertencem a processos; a preservação precisa ser considerada na política de retirada de uso.

### Análise técnica independente

- Edição pode usar o `PUT /api/gerenciamentos/{id}` existente para nome/descrição; a interface ainda precisa chamar essa operação.
- A edição deste CRUD não deverá alterar etapas ou demandas associadas. A manutenção das etapas será especificada no requisito correspondente.
- O cliente HTTP precisa aceitar sucesso sem corpo, como HTTP 204, e apresentar erros de API sem perder os campos preenchidos.
- Atualização da lista e do gerenciamento selecionado precisa refletir a resposta da API.
- O cache local contém as bibliotecas de testes MVC, JPA, JUnit e Mockito compatíveis com a versão do projeto. Regras e API podem ser testadas sem conexão ao banco configurado.
- H2 não está declarado no projeto nem disponível no cache local. Não há instância isolada MySQL pronta identificada; MySQL80 existente não deverá ser usado como banco de testes por suposição.
- Ainda não foram adicionadas dependências nem implementados/executados testes nesta discussão. O plano de persistência será definido tecnicamente antes da execução do primeiro requisito.
- Com somente consulta confirmado, a proteção precisa alcançar também mutações de etapas, demandas, comentários e histórico manual. Restauração e registro automático do próprio arquivamento deverão ser exceções controladas.
- O POST atual de processos aceita a entidade inteira, incluindo ID e relações. Uma regra de arquivamento deve resolver os vínculos persistidos e impedir que esse POST seja usado para atualizar um registro existente.
- Gerenciamentos legados podem estar sem criador, pois o serviço atual não o atribui. O usuário confirmou que apenas administradores podem administrá-los e todos os usuários logados podem consultar; não atribuir autoria por inferência. Decisão AD-006.

## Perguntas atuais

Nenhuma decisão de negócio ficou sem resolução ou padrão explicitamente proposto em `spec.md`.

As políticas 1A/2A, administradores escolhidos, arquivados somente consulta e administração dos quadros sem criador somente por administradores estão confirmadas. Restauração de quadros com criador, campos, nomes repetidos e filtros aparecem como propostas explícitas na especificação.

## Próximos esclarecimentos por dependência

- Identificar as contas que receberão o papel administrativo quando for necessário configurar o ambiente; manter cadastros comuns como funcionários.
- Apresentar a proposta de restauração pelo criador/administrador e a área de consulta de arquivados na especificação.
- Administração dos gerenciamentos sem criador conhecida e confirmada: apenas administradores.
- Apresentar os campos existentes e nomes repetidos permitidos como proposta explícita na especificação.
- Definir os critérios de teste e o tratamento de dados já cadastrados.
- A primeira entrega completa CRUD de quadros e a proteção do arquivamento. Etapas finais automáticas, CRUD completo de etapas e demandas e novos logs completos são requisitos posteriores registrados em `spec.md`; não serão declarados concluídos nesta entrega.

## Agent's Discretion

Escolhas técnicas rotineiras serão feitas a partir do código e da documentação disponível. Nenhuma política de negócio ambígua foi delegada pelo usuário até aqui.

## Aprova??o recebida

Em 2026-10-04 o usu?rio confirmou as propostas e autorizou iniciar implementa??o e testes. A autoriza??o abrange as decis?es, padr?es e limites da primeira entrega de spec.md; os demais requisitos continuam no backlog.
