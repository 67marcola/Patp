# Evidências — acesso pela internet

## Planejamento e autorização — 14/10/2026

Usuário escolheu Vercel/Render/Aiven, base nova de testes e aprovação administrativa de cadastros. Após apresentação do fluxo proposto, sessão8h, preservação local e publicação separada, pediu continuar para rodar no celular com dados móveis. Usuário possui somente Vercel; foi orientado a criar Render/Aiven gratuitos. Não houve compartilhamento de senha, criação de conta, contratação ou publicação.

Spec20NET validada sem erros/avisos. Design reutiliza componentes atuais; aprovação em tabela somente online, sem entidade JPA nova exigida pela validação local. Plano11 tarefas, testes focados por componente e um integrado; alvo oito invocações autor + duas Verifier/sensor, sem repetir suítes históricas ou medir tokens. Gates Build não repetem testes. Quantidade real/tentativas serão registradas, sem apagar testes existentes.

Revisão independente de spec identificou quatro ambiguidades, todas resolvidas antes de implementar: explicitar schema local intacto; obrigatoriedade do modo internet no artefato Render; vazio sem tabelas e recusa de preparo parcial; distinção entre aprovação comum e bootstrap administrativo. Spec permanece sem conclusão técnica enquanto tarefas/Verifier/acesso externo não forem comprovados.
