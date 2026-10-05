# Evidências da interface do CRUD de gerenciamentos

Relatório do autor, por tarefa. Não substitui o Verificador independente. Dados fictícios e testes isolados; nenhum acesso ao MySQL configurado.

## T9: runners de comportamento

Premissas: preservar o runtime, usar dependências somente de desenvolvimento e não incluir artefatos gerados existentes no commit. Arquivos: package/lock, configurações Vitest/Playwright, setup, teste do formulário e ignore somente para relatórios novos.

Gates: `npm.cmd test` PASS **1/1**; `npm.cmd run build` PASS; `npm.cmd run lint` PASS. Probe de navegador lançou e encerrou Edge **154.0.4258.53** em headless pelo canal `msedge`. Nenhum banco ou aplicação foi iniciado. Sucesso do probe valida disponibilidade, não conta como teste de requisito ou E2E.

| Adequação direta: critério | Evidência e assertion | Resultado esperado | Resultado |
| --- | --- | --- | --- |
| Cancelar formulário retorna sem mutação, GER-31/base T9 | `frontend/src/pages/CriarGerenciamento.test.jsx:19`, `expect(screen.getByRole("heading", { name: "Quadro de instalações" }).textContent).toBe("Quadro de instalações")`; `:21`, `expect(fetchSpy).not.toHaveBeenCalled()` | Volta ao quadro; nenhuma request | PASS |
| Runner significativo, lint limitado e build, Done when T9 | `frontend/package.json:9`, lint explicitamente limitado; comandos acima com exit 0 | Infra executa comportamento e compila sem varrer dependências | PASS |
| Navegador instalado, Done when T9 | `frontend/playwright.config.js:10`, `channel: "msedge"`; probe acima | Lançamento do Edge existente sem banco/dados reais | PASS |

| Adequação reversa: assertion | Âncora | Manter |
| --- | --- | --- |
| `CriarGerenciamento.test.jsx:19`, heading `.toBe("Quadro de instalações")` | GER-31, cancelamento e retorno; primeiro teste significativo T9 | Sim |
| `CriarGerenciamento.test.jsx:21`, `expect(fetchSpy).not.toHaveBeenCalled()` | GER-31, nenhum envio; contagem é o resultado exigido nesta parte | Sim |

Adequação: assertion de estado renderizado e ausência de request, sem tautologias, skips ou remoções. Guidelines adicionais ausentes; padrões da skill aplicados. T9 concluída; cobertura restante GER-28–41 será entregue nas tarefas próprias.
