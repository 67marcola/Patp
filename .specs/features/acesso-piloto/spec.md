# Acesso piloto na rede local Specification

**Data:** 14/10/2026. **Status:** Approved pelo usuário em14/10/2026, incluindo o modo de operação. Porte médio, sem novos papéis ou regras de negócio.

## Problem Statement

O sistema está configurado para acesso local neste computador. Os estudantes precisam acessar o mesmo sistema e os mesmos dados por vários computadores da mesma rede, antes do uso posterior pelos empregados da Creral. O preview de demonstração atual usa H2 temporário e não atende a essa persistência.

## Goals

- Oferecer um modo de iniciar o piloto neste computador e informar um endereço acessível na rede local.
- Compartilhar dados persistentes no MySQL existente, preservando contas e funcionalidades.
- Manter inicialização, encerramento e falhas compreensíveis, sem interromper os serviços atuais ou alterar estrutura automaticamente.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Acesso pela internet, domínio e ambiente definitivo da empresa | Usuário escolheu mesma rede no primeiro piloto. |
| Terceiro papel Gerente e painel para promover usuários | Permissões atuais continuam; outros administradores serão indicados depois. |
| Migração de schema/dados reais, reset ou criação automática de admin | Operações distintas; Marco já foi promovido de forma dirigida. |
| Mudança automática de firewall, roteador ou serviços Windows | Disponibilização do processo e instruções serão separadas dessas alterações do computador. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Público | Estudantes primeiro, empregados depois | Escolha do usuário | Sim |
| Rede e servidor | Mesma rede, este computador como central | Escolhas explícitas do usuário | Sim |
| Conta inicial | Marco Polinski, marcoimmarquin@gmail.com, ID2 no banco local sistema | Identificada e promovida, um admin confirmado | Aplicado e conferido |
| Operação | Um comando manual no servidor central, um endereço por IP local para clientes | Não exigir instalação de Java/MySQL nos computadores dos colegas | Sim, conjunto aprovado |
| Porta | 8082 inicialmente; se ocupada, recusar sem encerrar outro processo | 8081/5173 atuais já estão em uso e serão preservados | Default técnico |
| Dados | MySQL configurado neste computador, sem apagar ao encerrar; validar estrutura antes de liberar o serviço | Evitar confundir piloto com H2 descartável ou executar migração implícita | Sim, conjunto aprovado |
| Rede escolhida | Endereço IPv4 de uma interface local, informado/selecionado pelo operador | Evitar escolher VPN/interface errada automaticamente | Default técnico |

**Open questions:** none — defaults técnicos e operacionais registrados acima; confirmar o conjunto antes de implementar o novo modo.

## User Stories

### P1: Usar um único sistema em vários computadores

**User Story:** Como estudante participante do piloto, quero acessar pelo navegador o sistema central e os mesmos dados que meus colegas.

**Acceptance Criteria:**

1. **PIL-01:** WHEN o operador inicia o modo piloto com endereço local válido e porta livre THEN o sistema SHALL servir interface e API pelo mesmo endereço/porta do servidor central e informar a URL de acesso; os clientes SHALL usar API relativa a esse endereço, sem localhost apontando para o computador do cliente.
2. **PIL-02:** WHILE o piloto opera o sistema SHALL usar o MySQL configurado no servidor central e conservar contas e dados após encerrar/reiniciar o processo; H2 e apagamento de dados SHALL não fazer parte desse modo.
3. **PIL-03:** WHEN o piloto inicia THEN o sistema SHALL validar a estrutura do banco antes de disponibilizar o serviço, sem criar/alterar/apagar tabelas automaticamente; incompatibilidade SHALL impedir a liberação e orientar a preparação separada do banco.
4. **PIL-04:** IF endereço não pertence a uma interface local, porta está ocupada, dependência falta ou preparação falha THEN o iniciador SHALL encerrar com falha e mensagem identificável, sem URL de sucesso, promoção de conta ou interrupção de serviços não iniciados por ele.
5. **PIL-05:** WHEN o operador encerra o piloto THEN o iniciador SHALL encerrar somente seus processos filhos, preservando o serviço MySQL, seus dados e os serviços preexistentes8081/5173; os temporários criados pelo iniciador SHALL ter escopo identificado e limpeza restrita a esse escopo.
6. **PIL-06:** The piloto SHALL preservar autenticação e papéis existentes, usar credenciais do banco somente no servidor e não colocá-las na interface, URL pública, mensagens ou arquivos versionados novos; a configuração normal e o preview fictício existente SHALL continuar separados do modo piloto.
7. **PIL-07:** WHEN duas sessões de navegador consultam o piloto THEN o sistema SHALL permitir acesso autenticado ao mesmo quadro e mostrar uma alteração salva por uma sessão após recarga/consulta da outra, mantendo os controles de permissão existentes.
8. **PIL-08:** The documentação SHALL orientar início/encerramento, endereço dos colegas, necessidade de servidor ligado, dados persistentes, rede confiável, diagnóstico de conexão e roteiro de uso por estudantes; acesso real de outro computador e aceite humano SHALL permanecer pendentes até observados.

**Independent Test:** Iniciar em endereço local com MySQL fictício persistente, entrar com duas sessões, criar quadro/demanda, consultar na outra, reiniciar e conferir preservação; testar endereço inválido/porta ocupada e estrutura incompatível sem efeito no banco.

## Edge Cases

- Porta ocupada, interface inválida, dependências ausentes e falha de preparação seguem PIL-04.
- Banco incompatível não recebe atualização implícita, conforme PIL-03.
- Desligamento preserva dados e serviços externos, conforme PIL-02/05.
- Dimensões presentes: falhas de inicialização, acesso, credenciais, persistência e processos próprios especificados. Demais dimensões N/A: não há nova transição de domínio, serviço externo ou política de negócio.

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| PIL-01 | P1: Rede local | Execute | Implementing |
| PIL-02 | P1: Rede local | Execute | Implementing |
| PIL-03 | P1: Rede local | Execute | Implementing |
| PIL-04 | P1: Rede local | Execute | Implementing |
| PIL-05 | P1: Rede local | Execute | Implementing |
| PIL-06 | P1: Rede local | Execute | Implementing |
| PIL-07 | P1: Rede local | Execute | Implementing |
| PIL-08 | P1: Rede local | Specify | Pending |

## Success Criteria

- [ ] Comando e endereço central preparados, com dados persistentes e configuração de API correta.
- [ ] Gates focados em MySQL fictício e Verifier independente aprovados; falhas preservam dados/processos.
- [ ] Guia e diagnóstico registram estado real, separando prova automatizada do acesso observado pelos estudantes.

## Plano proposto

Três passos atômicos:1 preparar iniciador e configuração do modo local, com checks focados de entradas/falhas;2 verificar ciclo e duas sessões em MySQL fictício, sem suíte completa;3 guia e evidências. Cada passo tem gate e commit próprio. Verifier independente ao final, sensor leve em scratch. Tecnologias atuais mantidas; sem novos frameworks, alterações automáticas no banco real ou publicação na internet. Aplicar AD-035, reaproveitando provas vigentes e repetindo somente testes afetados. Após a entrega técnica, combinar início no computador central e teste observado em outro computador.
