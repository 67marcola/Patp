# Validation: acesso-piloto — PASS

Entrega técnica aprovada. Os oito critérios têm evidência compatível com seus resultados esperados. Acesso de outro computador físico e aceite humano continuam pendentes, conforme PIL-08.

**Data:** 2026-10-14. **Verifier:** subagente independente, não autor. **Spec:** `.specs/features/acesso-piloto/spec.md`. **Diff:** `cdf465d..330d984`, branch `testes`. T1 `d526ccd`, T2 `62a8ba0` e T3 `330d984` concluídos; porte médio, três passos implícitos, sem tasks.md.

## Evidência por critério

| AC | Resultado definido na spec | Evidência e assertion/outcome conferidos | Resultado |
| --- | --- | --- | --- |
| PIL-01 | UI/API na mesma origem, URL informada e API sem localhost do cliente | `frontend/scripts/piloto-integracao.test.mjs:80` verifica HTML; `:81` exige API401; `:83` exclui localhost:8081/api do bundle; `:99` usa `assert.deepEqual([...origens], [url])` após dois logins reais. `frontend/scripts/piloto-lan.mjs:122` informa a URL somente após prontidão própria. | PASS |
| PIL-02 | MySQL local, contas/dados conservados após encerrar e reiniciar | `frontend/scripts/piloto-config.test.mjs:16` exige configuração MySQL; `:18` rejeita H2/remoto. `frontend/scripts/piloto-integracao.test.mjs:103` exige processo persistido count1 e MySQL vivo; `:105` exige novo login200 da conta; `:106` usa `assert.deepEqual(depois, antes)` para snapshot integral após reinício. | PASS |
| PIL-03 | Validar schema sem DDL automático; incompatível impede liberação | `frontend/scripts/piloto-config.test.mjs:22` exige ddl-auto=validate e `:23` SQLinitnever. `frontend/scripts/piloto-integracao.test.mjs:109` exige sem pronto, exit1, mensagem estrutura/migração e zero tabelas no schema vazio. Sensor abaixo prova discriminação desse resultado. | PASS |
| PIL-04 | Entradas/falhas recusadas, mensagem identificável, sem falso sucesso ou interrupção externa | `frontend/scripts/piloto-config.test.mjs:12` rejeita interface externa/inespecífica, portas inválidas e argumentos duplicados. `:35` verifica porta ocupada recusada com servidor ainda ouvindo. `frontend/scripts/piloto-integracao.test.mjs:79` exige exit1/sem pronto na porta já usada; `:80` confirma servidor original acessível; `:109` cobre preparação incompatível; `:111` exige JAVA_HOME ausente exit1/mensagem/sem pronto. `frontend/scripts/piloto-config.test.mjs:48` rejeita nonce de outro processo. | PASS |
| PIL-05 | Encerrar somente filhos, preservar MySQL/dados/serviços e confinar cleanup | `frontend/scripts/piloto-integracao.test.mjs:62` exige exit0 em10s ao encerrar; `:64` exige TEMP ENOENT; `:103` exige MySQL/dados vivos; `:110` exige cleanup após falha. `frontend/scripts/piloto-config.test.mjs:41` rejeita cleanup fora do escopo. `frontend/scripts/piloto-lan.mjs:33` percorre somente filhos registrados e `:57` liga SIGINT ao mesmo encerramento. PIDs ouvindo8081/5173/33823 iguais antes/depois. | PASS |
| PIL-06 | Auth/papéis existentes, segredos só no servidor e modos separados | `frontend/scripts/piloto-integracao.test.mjs:81` exige anon401; `:91` exige login e tela autenticada real; `:96` exige colega sem Nova etapa; `:56`, `:83` e `:109` excluem senha/usuário fictícios da saída/bundle. `frontend/scripts/piloto-config.test.mjs:27` exclui credenciais dos argumentos e `:30` conserva resolução pelo ambiente. `sistema/src/main/java/com/patp/sistema/config/WebConfig.java:25` restringe interceptor a /api/**; todos os controllers atuais de negócio foram conferidos nesse prefixo. Preview/configuração normal não foram alterados. | PASS |
| PIL-07 | Duas sessões autenticadas veem quadro e alteração salva após nova consulta, respeitando permissão | `frontend/scripts/piloto-integracao.test.mjs:85` cria dois contextos independentes; `:95` salva quadro; `:96` exige colega consultar sem Nova etapa; `:97` cria cartão; `:98` exige exatamente o mesmo artigo visível no colega após reconsulta. | PASS |
| PIL-08 | Guia completo e estado humano/rede explicitamente pendente | Inspeção documental: `docs/OPERACAO.md:5` explica clientes, servidor ligado e rede confiável; `:10` comandos; `:17` IP/porta; `:19` URL; `:21` persistência/credenciais/sem migração; `:23` encerramento; `:25` diagnóstico; `:27` declara rede física/aceite pendentes; `:29` roteiro de estudantes. Critério documental, sem assertion automatizada artificial. | PASS |

Não há lacuna de precisão que impeça avaliar os outcomes. Casos de borda da spec cobertos: interface/porta inválidas, porta ocupada, dependência ausente, schema incompatível sem tabelas criadas, persistência e desligamento restrito. Ctrl+C foi inspecionado no mesmo caminho de encerramento; o gate automatizado usa o comando encerrar. Não foi observado Ctrl+C físico nem acesso pela rede em outro computador.

## Gates e integridade dos testes

Comandos executados uma vez cada em cópia física completa de frontend/sistema, incluindo node_modules, sem links/reparsepoints nem cache Vite/target compartilhados com a árvore real:

- `npm.cmd run test:piloto`: 6/6 testes Node aprovados,0falhas/skips.
- `npm.cmd run lint`: exit0.
- `npm.cmd run test:piloto:integracao`: 1/1 cenário Node aprovado,48,7s; dois contextos Edge, MySQL fictício33823, builds Java/Vite reais em TEMP, duas inicializações e falhas controladas.0falhas/skips.

Testes antigos não foram alterados, removidos, enfraquecidos ou desabilitados no diff. Delta estático da entrega: seis testes Node e um cenário integrado novos. Não foi refeita contagem histórica nem suíte completa, conforme AD-035. Verifier: três invocações comportamentais, incluindo o sensor; autor registra sete. Total10. A comparação com20 é a referência de planejamento informada pelo autor, sem nova aferição histórica.

## Sensor de discriminação

Profundidade leve, uma mutação dirigida ao risco de DDL implícito do iniciador. Controle válido é o mesmo cenário integrado aprovado acima. Na cópia isolada, `frontend/scripts/piloto-config.mjs:27` teve `ddl-auto=validate` trocado por `ddl-auto=update`; nenhum teste foi modificado. Nova execução do mesmo `npm.cmd run test:piloto:integracao` terminou exit1, com falha funcional em `frontend/scripts/piloto-integracao.test.mjs:109`: `assert.ok(!falha.texto.includes("Piloto pronto:"))`. O schema vazio tornou-se indevidamente pronto. A fixture concluiu seu finally; falha de infraestrutura não foi usada como prova.

**Sensor:** 1injetada,1morta,0sobreviventes. Sem ampliação para suíte de mutação de domínio: esta entrega prepara o piloto e conserva regras existentes; escopo leve foi aprovado.

## Isolamento e qualidade

Baseline e comparação final comprovaram HEAD e porcelain idênticos e4701 hashes SHA256 inalterados, incluindo arquivos rastreados e gerados preexistentes em frontend/dist e sistema/target. A modificação preexistente `frontend/node_modules/.vite/deps/_metadata.json` foi preservada. O scratch físico foi removido após validar seu caminho absoluto. O relatório é a única escrita intencional na árvore real pelo Verifier.

Listeners antes/depois mantiveram os mesmos donos:8081 PID5184,5173 PID23848 e33823 PID11776. O Verifier não encerrou MySQL nem serviços preexistentes, não conectou à3306, não usou conta real e não iniciou piloto real. O harness fictício verificou porta/datadir e usou apenas schemas/usuário exclusivos do cenário.

Mudanças são proporcionais ao pedido: iniciador/configuração, dois arquivos de testes focados, scripts npm, ajuste do interceptor para servir UI e documentação. Sem abstração de uso único adicional, nova regra de negócio, dependência ou framework. Guidelines aplicadas: `.agents/skills/tlc-spec-driven/references/coding-principles.md` e AD-035. Cada teste novo mapeia aos PIL/edges indicados; preparo da fixture serve somente às precondições. Nenhuma lacuna ou mutante sobrevivente exige correção; PASS limpo não produz lição nova.

Provas preservadas em `C:/Users/Marco/AppData/Local/Temp/piloto-verifier-59482fc4aa284ea28548de85f3be491c/`: `unit.log`, `lint.log`, `integration.log`, `sensor.log`, `baseline.json`, `integrity.json`, `listeners-before.json`. Captura do controle: `C:/Users/Marco/AppData/Local/Temp/piloto-duas-sessoes-9322b438013c.png`. Uma preparação inicial em TEMP virtual efêmero não iniciou qualquer gate; foi substituída pelo TEMP físico antes dos testes.

## Fechamento

PIL-01–08 tecnicamente verificados; nenhum fix obrigatório. Orquestrador pode atualizar rastreabilidade para Verified e encerrar a fixture após conferir identidade. Entrega preparada para início operacional autorizado; UAT físico e aceite dos estudantes permanecem pendentes, sem serem inferidos dos dois contextos localhost.
