# Contexto — acesso pela internet

**Data:** 14/10/2026. **Status:** definição inicial; sem implementação ou publicação. Spec e plano serão escritos após escolha da hospedagem. Manter Java, Spring Boot, MySQL, React e tecnologias existentes, conforme AD-024.

## Pedido e estado confirmado

O usuário quer acessar o sistema por computador ou celular fora da mesma rede. Já relatou sucesso em outro computador no mesmo Wi-Fi. Perguntou se a Vercel permite hospedagem gratuita; essa pergunta não constitui escolha definitiva de todos os provedores. Segundo administrador admin@gmail.com já configurado no banco local, separado deste requisito.

## Proposta para piloto acadêmico gratuito, ainda não confirmada

| Parte | Opção proposta | Condições relevantes |
| --- | --- | --- |
| Interface React/Vite | Vercel Hobby | Plano gratuito para uso pessoal/não comercial e sujeito a limites; considerar outro plano para uso empresarial. |
| API Java/Spring Boot | Render Free com Docker | Java por container; suspensão após 15 minutos sem tráfego, demora na retomada e limites de uso. Sessões atuais em memória se perdem ao reiniciar. |
| Banco MySQL persistente | Aiven Free | MySQL real, atualmente 1GB de armazenamento, sem cartão para cadastro; serviço pode ser desligado por inatividade e não tem SLA do plano pago. |

Essa combinação é uma recomendação para o piloto acadêmico, não contratação ou promessa de disponibilidade permanente/gratuidade ilimitada. Antes do uso pelos empregados, avaliar plano e operação adequados à empresa.

Fontes oficiais consultadas em 14/10/2026: [Vercel/Vite](https://vercel.com/docs/frameworks/frontend/vite), [Vercel Hobby](https://vercel.com/docs/plans/hobby), [Render Free](https://render.com/docs/free), [Java/Docker no Render](https://render.com/docs/faq), [MySQL Free Aiven](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier).

A Vercel também anunciou execução de containers, inclusive Spring Boot: [anúncio oficial](https://vercel.com/blog/dockerfile-on-vercel). Portanto não afirmar que Java é impossível na plataforma. A disponibilidade desse recurso no plano gratuito e a adequação às sessões em memória do projeto ainda não foram comprovadas; MySQL persistente exige serviço próprio de banco.

## Pontos do projeto já conferidos

- Interface aceita VITE_API_URL, mas padrão é localhost:8081/api; hospedagem precisa de endereço público ou proxy /api. CORS atual só libera localhost:5173.
- Piloto existente aceita somente MySQL local e compila em TEMP. Não reutilizar esse modo como publicação definitiva.
- Configuração normal precisa de segredos externos e validação de estrutura; nenhuma migração automática de dados existentes.
- Sessões em memória não têm prazo implementado; frontend Sair ainda não chama endpoint de revogação. Definir a preparação necessária para hospedagem contínua antes da exposição.
- Cadastro atual é público e concede consulta a todos os autenticados. Acesso de qualquer dispositivo não significa consulta anônima; confirmar se novos cadastros externos serão livres ou controlados.

## Decisões ainda necessárias

1. Escolha de hospedagem: proposta Vercel/Render/Aiven, alternativa na Vercel com containers ou outro caminho.
2. Contas/dados do piloto online: base separada ou transferência controlada dos dados locais com backup. Administradores locais não se replicam automaticamente.
3. Cadastro externo: manter inscrição pública ou exigir autorização/convite.

Não pedir senhas pessoais nem criar serviços pagos; preparar arquivos e instruções localmente antes da publicação. Manter testes focados e saída compacta, sem repetir suítes históricas (AD-035). Nenhum requisito Internet será marcado concluído antes de testes pertinentes, revisão independente e acesso externo observado.
