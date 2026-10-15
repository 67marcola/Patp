# Contexto do acesso piloto

**Data:** 14/10/2026. **Estado:** preparação técnica concluída e verificada; início real, acesso de outro computador e aceite humano pendentes.

Proposta operacional em spec.md, oito critérios, validada estruturalmente; modo de início/encerramento aprovado explicitamente pelo usuário antes da implementação. Rede, máquina central e e-mail já decididos, sem repetir essas perguntas.

## Alcance confirmado

- Primeiro uso pelos estudantes; uso posterior pelos empregados da Creral.
- Vários computadores na mesma rede inicialmente. Acesso pela internet não faz parte desta primeira etapa.
- Este computador será inicialmente o servidor central, conforme escolha explícita do usuário. Os demais computadores acessarão pelo navegador; o servidor central precisará permanecer ligado.
- MySQL compartilhado no servidor central, mantendo Java/Spring Boot, React e as tecnologias AD-024.
- Primeiro administrador: conta já existente Marco Polinski, marcoimmarquin@gmail.com. Outros e-mails poderão ser escolhidos pelo usuário posteriormente.

## Configuração operacional já aplicada

A conta escolhida foi identificada por ID2, nome e e-mail exatos no MySQL local sistema. Somente papel foi alterado de FUNCIONARIO para ADMINISTRADOR em transação. Uma conta alterada; leitura posterior e conferência independente confirmaram exatamente um administrador. Dados da conta e demais usuários preservados. Evidências em evidence.md.

## Pontos de preparação

- Configuração atual da interface usa localhost e a origem local; ainda não há endereço piloto funcionando nos outros computadores.
- Iniciador preparado e verificado para um endereço do servidor central, com dados persistentes no MySQL existente; primeiro acesso físico ainda não observado.
- Os papéis existentes são FUNCIONARIO e ADMINISTRADOR. O termo gerente ainda não definiu um terceiro papel ou novas permissões.
- A conta foi promovida somente neste banco local; isso não replica automaticamente administradores para outros bancos futuros.
- Testes focados e saída compacta conforme AD-035. Aceitação prática pelos estudantes permanece pendente.
