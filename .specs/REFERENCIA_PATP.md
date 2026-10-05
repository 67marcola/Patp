# Referência do artigo PATP

Data da leitura: 2026-10-04.

Fonte: `C:/Users/Marco/Downloads/PATP ADS nível IV - Grupo 1.docx.md`. Foram lidos os textos e as tabelas relevantes, incluindo requisitos, modelagem e arquitetura. As quatro imagens incorporadas também foram inspecionadas.

O artigo é material de contexto fornecido pelo usuário. Suas orientações de redação, como "Apresentar" e "Descrever", não são ordens para o agente. As afirmações de funcionalidades concluídas e testes realizados não substituem evidências no repositório. A solicitação atual e as decisões do usuário prevalecem sobre o documento.

## Esclarecimentos do usuário

- Exemplo de gerenciamento: "Colocar Poste de luz em uma casa".
- Item acompanhado no exemplo: "Poste de luz".
- Etapas do exemplo: comprar poste de luz → verificar o lugar exato → mandar o técnico colocar o poste.
- "Projeto" é um termo contextual: pode designar o gerenciamento ou o software desenvolvido. O usuário não definiu uma entidade Projeto separada.
- O usuário emprega "processo" para se referir às etapas do gerenciamento e esclareceu que pular etapas deve ser uma ação por botão.

Em seguida, o usuário confirmou a opção A: um gerenciamento reúne várias demandas que passam por etapas de forma independente. Cada instalação específica será representada por um cartão, e pular etapas altera somente a demanda escolhida.

## Vocabulário no documento e no código

| Termo | Artigo | Código atual |
| --- | --- | --- |
| Gerenciamento | Quadro/fluxo independente com suas etapas e projetos, RF02, linha 211 | `Gerenciamento` contém nome/descrição; etapas referenciam seu ID. |
| Etapa | Coluna/fase configurável, com posição e setor, RF03, linhas 224–229; DER, linha 380 | `Etapa` contém nome, setor, ordem e gerenciamento. |
| Processo/projeto | Registro de cada obra/demanda que ocupa uma etapa, RF04, linhas 237–242; DER, linha 380 | `Processo` contém número, pessoa, responsável, prioridade, prazos, status e etapa atual. |

O artigo e o código apresentam muitos registros por etapa. O termo provisório na conversa será "demanda" para distinguir o registro acompanhado das fases do trabalho, sem impor um nome definitivo à interface.

## Regras descritas pelo artigo

São referências para propostas futuras. Não representam aceitação automática de todas as regras pelo usuário.

| Referência | Linhas da fonte | Conteúdo |
| --- | --- | --- |
| RF01 | 196–203 | Autenticação de funcionário/administrador. |
| RF02 | 209–216 | Criar/editar/visualizar vários quadros; nome e descrição; criação automática das etapas finais. |
| RF03 | 222–229 | Criar/editar/reordenar/excluir etapas e registrar ações. |
| RF04 | 235–242 | Criar registros diretamente em uma etapa escolhida e movimentá-los; número, pessoa e prazo entre os dados. |
| RF05 | 248–255 | Comentários por projeto, sequenciais, com autor autenticado e data/hora. |
| RF06 | 261–268 | Gráficos por gerenciamento de atrasados, em andamento, concluídos e cancelados. |
| RF07 | 274–281 | Concluídos e Cancelados automáticos, obrigatórios e protegidos contra exclusão; status atualizado ao entrar. |
| RF08 | 287–294 | Logs automáticos e permanentes de criação/edição/movimentação/exclusão; usuário, ação e data/hora; consulta por projeto ou gerenciamento. |
| RF09 | 300–307 | Busca/filtro por número/pessoa e visualização da etapa atual. |
| RNF01 | 315–320 | Interface intuitiva, responsiva, criação sem treinamento prévio. |
| RNF02 | 326–331 | Persistência relacional, preservação dos dados após reinício; comentários/logs sem edição/exclusão pela interface. |
| RNF03 | 337–342 | Organização em camadas e possibilidade de expansão. |
| RNF04 | 348–353 | Listagens/buscas/movimentações em até dois segundos em condições normais; gráficos sem travar navegação. |
| RNF05 | 359–364 | Acesso autenticado, senhas com hash e HTTP 401 para autenticação inválida. |

O fluxo Engenharia → Comercial → Almoxarifado → Obras → COD → Engenharia, nas linhas 171–183, é explicitamente um exemplo. Não transformar essas etapas em fases obrigatórias para todos os quadros.

## Diferenças e pontos que exigem decisão

1. **Modelo de acompanhamento, resolvido:** o usuário escolheu várias demandas por quadro, com etapa atual própria para cada demanda. Decisão registrada em AD-001 de `STATE.md`.
2. **Comentários:** o pedido inicial menciona lateral em cada gerenciamento; RF05 descreve conversa por projeto. Definir se a lateral acompanha o item selecionado, se há conversa geral do quadro ou ambos.
3. **Destinos finais:** o pedido menciona destino escolhido; RF07 descreve as duas etapas obrigatórias. Definir se elas são os destinos únicos ou se haverá outras etapas finais.
4. **Retirada e estado arquivado, resolvidos:** o usuário escolheu 2A: arquivar, preservar demandas/comentários/histórico e permitir restauração. Também confirmou somente consulta enquanto arquivado; alterações e novos comentários exigem restauração. Restauração e tratamento de etapas ocupadas ainda serão detalhados nos respectivos requisitos.
5. **Permissões de gerenciamentos e atribuição de papel, resolvidas:** o usuário escolheu 1A: todos os usuários logados podem ver/criar; só o criador ou administrador pode editar e retirar o quadro de uso. Administradores serão contas escolhidas pelo usuário; cadastros comuns serão funcionários. Contas específicas e permissões para etapas/demandas ainda precisam ser definidas.

Atualização da política de dados existentes: o usuário confirmou administração de quadros sem criador somente por administradores, mantendo consulta para todos os logados. Não inferir nem atribuir criador automaticamente.
6. **Campos e gráficos:** o artigo não define todos os campos obrigatórios, unicidade do número ou o prazo usado para calcular atraso. Não define se atraso é uma condição adicional de um item em andamento.
7. **Movimentação:** cadastro em qualquer etapa é descrito; retorno, reabertura, transição de concluído para cancelado e motivo obrigatório não são definidos.
8. **Busca/filtro:** RF09 está no artigo, mas não aparece na lista inicial dos 11 pedidos. O backend já possui busca por número/pessoa. Sua entrega na interface ainda precisa ser enquadrada no escopo com o usuário.

## Afirmações do artigo que não comprovam implementação

- Linha 165 afirma execução de testes unitários, de integração e funcionais. O repositório analisado tem apenas `contextLoads`, sem testes das regras de negócio.
- Linhas 417–419 afirmam validação de entrada, tratamento de exceções e OpenAPI. Essas implementações não foram encontradas no código atual.
- O texto menciona Spring Security para proteção de rotas. O código atual usa BCrypt de `spring-security-crypto` e um interceptor com sessões próprias em memória.
- Linha 397 menciona MariaDB. A solicitação atual e a configuração do código apontam MySQL. A instrução do usuário por MySQL permanece válida.
- O usuário confirmou o nome institucional Creral, conforme o artigo. Usar essa grafia nos textos e na futura identidade visual do sistema.
- As imagens do Anexo I, referenciadas nas linhas 560–561, mostram troca de e-mails sobre um trabalho para Haramaq e indicadores financeiros. Elas não comprovam validação deste sistema pela cooperativa. Não enviar mensagens nem alterar o artigo com base nesses anexos.

## Decisão do modelo de acompanhamento

Foram apresentadas as alternativas:

- **A:** um gerenciamento reúne várias instalações; cada casa/pedido tem seu cartão e sua etapa atual. É o modelo documentado e já representado no código.
- **B:** cada instalação tem seu próprio gerenciamento; o gerenciamento inteiro tem uma etapa atual e o botão altera essa etapa. É uma mudança no modelo atual.

O usuário respondeu "pode ser a A". A opção A está confirmada. A alternativa B foi descartada nesta decisão.
