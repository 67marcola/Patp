# Contexto — acesso pela internet

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
