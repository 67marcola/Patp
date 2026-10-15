# Acesso pela internet — Design

**Spec:** spec.md. **Estado:** decisões de hospedagem/dados/cadastro confirmadas; escolhas técnicas abaixo executam o alcance autorizado, com ferramentas atuais/TLC. Publicação depende das contas Render/Aiven do usuário.

## Architecture Overview

Navegador computador/celular → React/Vite na Vercel → API HTTPS no Render → MySQL com TLS verificado no Aiven. API pública configurada no build e origem Vercel exata no CORS; sem localhost ou origem curinga. Uma instância Render gratuita, sessões em memória limitadas a oito horas; reinício exige novo login.

Alternativas consideradas: servir UI/API juntos no Render diminui domínios mas contraria a separação escolhida pelo usuário; container na Vercel requer confirmar elegibilidade/gratuidade e adaptar estado. Seguir Vercel/Render/Aiven, escolha explícita. Aprovação em tabela JDBC online isolada evita alterar a validação JPA/schema do piloto local.

## Code Reuse Analysis

| Existente | Reuso |
| --- | --- |
| UsuarioService/UsuarioController e BCrypt | Cadastro, verificação de senha e respostas públicas atuais; aprovação adicionada ao fluxo internet. |
| SessaoService e AutenticacaoInterceptor | Token Bearer, usuário atual lido do banco,401; expiração somente no modo internet. |
| UsuarioRepository e JdbcTemplate do Spring | Dados de conta preservados; tabela de acesso online transacional no mesmo DataSource. |
| ApiException/ApiError e requisicao | Mensagens estruturadas e tratamento conservador de resultados incertos. |
| Cadastro/App e painéis existentes | Formulário atual, sessão segura, padrão de carregar/erro/vazio e foco de teclado. |
| Iniciador/testes isolados | Padrão de cópia TEMP, banco fictício guardado e cleanup próprio; não usar piloto LAN como deploy. |

## Components

- **AprovacaoCadastroService:** política opt-in por `creral.internet.enabled`, false por padrão. Registra pendência, verifica aprovação, lista e aprova atomicamente; nunca consulta tabela online no modo local. Não depende do SessaoService para evitar ciclo.
- **Autenticação existente:** cadastro internet202 `{estado:"PENDENTE",mensagem:"Cadastro aguardando aprovação do administrador."}`, sem sessão. Login correto pendente403 `{erro:...}`. Modo local mantém resposta200 com seis campos. Cadastro transacional inclui conta e acesso; corpo não escolhe papel/aprovação.
- **SessaoService:** guarda ID/instante do token; verifica aprovação e expiração absoluta8h somente no modo internet; endpoint logout existente revoga. Relógio controlável nos testes, sem esperar horas.
- **AprovacaoCadastroController:** somente modo internet; GET `/api/administracao/cadastros/pendentes`, POST `/api/administracao/cadastros/{id}/aprovar`.401 sem sessão,403 não admin,404 alvo inexistente. Admin sempre obtido da sessão/DB, nunca do body.
- **PrepararBancoInternet:** ferramenta JDBC explícita que não carrega credenciais locais. Plano sem escrita; criação apenas em schema totalmente vazio, sem DROP; preparação de admin exige ID/e-mail conferidos de conta existente, sem senha no chat. Inicialização normal nunca cria tabelas. Recursos SQL pertencem somente ao preparo explícito online.
- **Cliente/UI:** helpers de API validam contratos; Cadastro mostra espera sem entrar. Painel AprovacoesCadastros consulta e aprova, com botão exposto pelo App somente no modo online e papel conferido no servidor. Sessão local continua com os quatro campos públicos atuais. Sair online solicita revogação, limpa cache e informa falha incerta.
- **Pacote online:** perfil internet obrigatório no entrypoint Render; porta PORT, segredos por ambiente, conexão TLS VERIFY_IDENTITY e CA fornecida como arquivo de segredo, schema validate/SQLinitnever. Docker multistage exclui application.properties local, node_modules, target e git. Nenhuma dependência nova.
- **Vercel:** framework Vite, root frontend, build normal, VITE_API_URL HTTPS do Render e VITE_MODO_INTERNET=true. Nenhuma variável do banco na interface.

## Data Models

Somente na base online: `acessos_usuarios(usuario_id BIGINT PK/FK usuarios, aprovado BOOLEAN NOT NULL, aprovado_por BIGINT NULL/FK usuarios, aprovado_em DATETIME(6) NULL)`. Ausência de linha equivale a pendente no modo internet. Cadastro cria false/null/null. Aprovação comum conserva papel; primeira aprovação guarda autor/hora, repetição não sobrescreve. Bootstrap explícito é separado e pode conceder ADMINISTRADOR apenas à conta indicada. Entidade Usuario não ganha campo JPA obrigatório.

Listagem pendente: array `{id,nome,setor,email}` por ID crescente. Aprovação:200 `{id,estado:"APROVADO"}`. Nenhuma senha/hash em respostas. Transação de aprovação bloqueia a conta alvo e confirma uma primeira autoria; transação de cadastro reverte conta se pendência falhar.

## Error Handling Strategy

Início sem configuração/TLS/estrutura recusa sem fallback ou segredos. Preparo recusa qualquer tabela preexistente antes da primeira criação; DDL parcial fica identificado e não é apagado automaticamente. Listagem vazia explícita; erro preserva dados atuais. Aprovação incerta exige atualizar, sem retry automático. Inatividade gratuita não promete disponibilidade; novo login após reinício explicado no guia.

## Risks & Concerns

| Local | Risco | Mitigação |
| --- | --- | --- |
| application.properties:2 | Credenciais locais já existem no arquivo normal | Excluir do contexto/imagem e fornecer bootstrap seguro no build; não publicar repositório/credenciais por suposição. |
| UsuarioService:29,UsuarioController:32 | Cadastro livre criaria sessão antes de autorização | Política internet opt-in e resposta202 sem token, validação no login/sessão. |
| SessaoService:14 | Estado em memória sem prazo |8h no modo internet, revogação ao sair e aviso de reinício/single-instance. |
| WebConfig:25 e CorsConfig:14 | Origens/rotas podem bloquear ou alargar acesso | API autenticada, CORS HTTPS exato, endpoint novo pelo mesmo interceptor. |
| Validação JPA do piloto | Entidade/coluna nova quebraria base local | Tabela JDBC somente online, sem migração local. |
| Docker não encontrado no PATH local | Não é possível afirmar build de imagem local executado | Provar jar/config/build local; marcar validação do container no Render pendente até observada. |
| Render/Aiven ainda sem conta | Nenhuma URL pública real disponível agora | Preparar localmente; usuário cria contas gratuitas; não inventar link/resultado 4G. |

## Tech Decisions

Sem novos frameworks. Conservar papéis e regras dos quadros; aprovados comuns são funcionários. Falha de aprovação não promove ninguém. Política online opt-in e perfil Render obrigatório conservam o cadastro direto local. Testes focados por componente e um integrado de fechamento; Verifier independente com controle integrado e um sensor dirigido, sem refazer suítes históricas.
