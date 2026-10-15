# Contexto — acesso pela internet

**Decisão atual15/10/2026 — AD-048:** usuário definiu admin@gmail.com como único administrador online por enquanto. Isso substitui a proposta anterior de dois admins e o próximo passo de promover Marco online. Cadastro pessoal e conferênciaID/email continuam obrigatórios; usuário já autorizou aprovação/promoção dessa conta selecionada, sem nova confirmação idêntica. Consulta após o pedido ainda0usuários/0admins. Publicação já Ready/Live, ver evidências atuais; notas abaixo preservam a evolução anterior.

**Data:** 14/10/2026. **Status:** preparação local autorizada ao continuar para acessar pelo celular com dados móveis, após explicação do fluxo pendente/aprovar, base nova, sessões de oito horas e isolamento local. Publicação ainda não realizada. Manter tecnologias conforme AD-024.

## Pedido e estado confirmado

O usuário quer acessar o sistema por computador ou celular fora da mesma rede. Já relatou sucesso em outro computador no mesmo Wi-Fi. Após perguntar sobre gratuidade da Vercel, escolheu explicitamente Vercel + Render + Aiven, base nova de testes e autorização administrativa de novas contas. Segundo administrador admin@gmail.com já configurado no banco local, separado deste requisito.

## Hospedagem escolhida para o piloto acadêmico

| Parte | Opção proposta | Condições relevantes |
| --- | --- | --- |
| Interface React/Vite | Vercel Hobby | Plano gratuito para uso pessoal/não comercial e sujeito a limites; considerar outro plano para uso empresarial. |
| API Java/Spring Boot | Render Free com Docker | Java por container; suspensão após 15 minutos sem tráfego, demora na retomada e limites de uso. Sessões atuais em memória se perdem ao reiniciar. |
| Banco MySQL persistente | Aiven Free | MySQL real, atualmente 1GB de armazenamento, sem cartão para cadastro; serviço pode ser desligado por inatividade e não tem SLA do plano pago. |

Essa combinação foi escolhida pelo usuário para o piloto acadêmico; não houve contratação nem promessa de disponibilidade permanente/gratuidade ilimitada. Antes do uso pelos empregados, avaliar plano e operação adequados à empresa.

Fontes oficiais consultadas em 14/10/2026: [Vercel/Vite](https://vercel.com/docs/frameworks/frontend/vite), [Vercel Hobby](https://vercel.com/docs/plans/hobby), [Render Free](https://render.com/docs/free), [Java/Docker no Render](https://render.com/docs/faq), [MySQL Free Aiven](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier).

A Vercel também anunciou execução de containers, inclusive Spring Boot: [anúncio oficial](https://vercel.com/blog/dockerfile-on-vercel). Portanto não afirmar que Java é impossível na plataforma. A disponibilidade desse recurso no plano gratuito e a adequação às sessões em memória do projeto ainda não foram comprovadas; MySQL persistente exige serviço próprio de banco.

## Pontos do projeto já conferidos

- Interface aceita VITE_API_URL, mas padrão é localhost:8081/api; hospedagem precisa de endereço público ou proxy /api. CORS atual só libera localhost:5173.
- Piloto existente aceita somente MySQL local e compila em TEMP. Não reutilizar esse modo como publicação definitiva.
- Configuração normal precisa de segredos externos e validação de estrutura; nenhuma migração automática de dados existentes.
- Sessões em memória não têm prazo implementado; frontend Sair ainda não chama endpoint de revogação. Definir a preparação necessária para hospedagem contínua antes da exposição.
- Cadastro atual é público e concede consulta a todos os autenticados. Acesso de qualquer dispositivo não significa consulta anônima; confirmar se novos cadastros externos serão livres ou controlados.

## Decisões confirmadas e defaults para a spec

1. Vercel para React, Render para Java/Spring Boot e Aiven para MySQL; planos gratuitos e limites explicados, sem contratação automática.
2. Base nova de testes, sem transferir contas, senhas, quadros, demandas ou históricos locais. Administradores locais não se replicam automaticamente.
3. Novas contas online dependem da autorização de administrador. Proposta: cadastro fica pendente, sem sessão/acesso; admin consulta pendentes e aprova; aprovados entram como funcionários.
4. Inicializar administradores da nova base mediante procedimento explícito do operador, após cadastro controlado das novas contas com os e-mails já escolhidos. Nunca promover automaticamente por e-mail enviado no navegador. Senhas escolhidas pelo usuário, sem compartilhamento no chat.
5. Aplicar aprovação no modo internet, conservando o cadastro direto e a estrutura atual do piloto local. Produção usa configuração externa, MySQL com TLS validado e estrutura preparada explicitamente.
6. Propor sessão online de oito horas, revogação ao sair e documentação da perda de sessões no reinício do Render. Não adicionar convites por e-mail, recuperação de senha, MFA ou novos papéis nesta entrega.

As decisões 1 a 3 foram respondidas pelo usuário. Após explicação do fluxo e defaults 4 a 6, o usuário pediu continuar e reforçou o acesso pelo celular com dados móveis; seguir na preparação local, sem nova pergunta sobre o mesmo alcance. Usuário possui somente Vercel e foi orientado a criar contas gratuitas no Render/Aiven. Endereços dos serviços serão definidos na publicação assistida; não pedir credenciais pessoais.

Não pedir senhas pessoais nem criar serviços pagos; preparar arquivos e instruções localmente antes da publicação. Manter testes focados e saída compacta, sem repetir suítes históricas (AD-035). Nenhum requisito Internet será marcado concluído antes de testes pertinentes, revisão independente e acesso externo observado.

Atualização15/10/2026: usuário confirmou ter criado contas nas três plataformas e pediu o próximo passo. Preparação técnica local foi verificada; serviço MySQL/Aiven e URLs ainda não confirmados. Orientar primeiro criação do MySQL gratuito vazio, depois preparo explícito/Render/Vercel e aceite externo. Não repetir a pergunta sobre já possuir contas; não solicitar senha/URI com senha no chat.

Serviço Aiven informado depois: creral-mysql-piloto-marcoimmarquin-2e37.f.aivencloud.com:21005/defaultdb, usuário avnadmin e CA em C:/Users/Marco/Desktop/ca.pem. Conferência sem autenticação validou TLS/cadeia/hostname. Nenhum SQL executado ou esquema inspecionado. A URI continha credencial; não persistir seu conteúdo. Usuário foi orientado a redefinir a senha e digitá-la somente localmente/painéis.

Atualização seguinte15/10: usuário decidiu manter a senha atual no piloto e autorizou expressamente criar as oito tabelas no Aiven. Consulta confirmou base vazia; plano/aplicaçãoJava passaram e as oito tabelas vazias foram verificadas. SpringBootvalidate semweb passou; não há usuários/admins online. Não insistir em rotação neste teste nem repetir a preparação já concluída. Seguir publicação privada/Render/Vercel, ainda sem autorização para criar repo/push/deploy.

Repositório criado pelo usuário no celular e informado em15/10:67marcola/-creral-piloto-online. é o nome exato, com hífen inicial e ponto final. Confirmado privado e com README inicial na main. Usuário respondeu “Sim, enviar o código (recomendado)” à pergunta de envio139arquivos. Envio pelo conector autorizado concluído e conferido no commit c496d91b495dea5b2fa2fa299c337cb15c375377, preservando README e139/139 hashes. Não pedir login no computador para essa etapa. Esta autorização cobre o envio; configuração/publicação nos provedores continua como próximo passo.

Domínio Vercel informado pelo usuário em15/10: creral-piloto-online.vercel.app. Usar https://creral-piloto-online.vercel.app como CRERAL_FRONTEND_ORIGIN no Render, sem barra final. Não perguntar novamente o domínio nem assumir que o build passou. Render/API reais ainda não informados; não inventar subdomínio com base no nome sugerido.
