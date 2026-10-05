# Evidência da interface de etapas

## T2: cliente HTTP

PASS. Quatro wrappers usam o contrato final, incluindo DELETE200 com snapshot JSON. Nenhum retry de gravação foi acrescentado. Arquivos: api.js e api.test.js. Assumimos o DTO e as rotas confirmados em T1; nenhuma biblioteca nova.

Gate: `npm.cmd test -- --reporter=dot`, 81 testes PASS, zero falhas/skips (base71 +10). RED anterior: os dez casos novos falharam por ausência dos wrappers; os71 anteriores passaram. Assertions anteriores conservadas.

### Adequação direta (Check A/B)

| Critério / ETA | Evidência física + assertion | Outcome definido |
| --- | --- | --- |
| GETestrutura sessão/signal/DTO, ETA01/29 | frontend/src/services/api.test.js:119 `expect(await buscarConfiguracaoEtapas("sessao", 9, { signal: controller.signal })).toEqual(configuracao)`; :120 `expect(fetchSpy).toHaveBeenCalledWith(...)` | GETestrutura, Bearer sessao, mesmo signal, gerenciamento versão3 e etapa id4/nome/ setor/ordem1/quantidade2 |
| POST/PUT/DELETE, ETA12/13/15/28 | frontend/src/services/api.test.js:127 `expect(await executar()).toEqual(configuracao)`; :128 `expect(fetchSpy.mock.calls[0][0]).toBe(...)`; :129 `expect(fetchSpy.mock.calls[0][1]).toEqual(...)` | POST201/PUT200/DELETE200 com JSONsnapshot; campos e versão2, token e método corretos; DELETEbody somente versão |
| Erro exato sem retry, ETA27/29 | frontend/src/services/api.test.js:139 `await expect(executar()).rejects.toMatchObject({ message: mensagem, status: 409 })`; :140 `expect(fetchSpy).toHaveBeenCalledTimes(1)` | mensagem de conflito exata/status409 para três gravações, nenhuma repetição |
| Rede ambígua, ETA27 | frontend/src/services/api.test.js:146 `await expect(executar()).rejects.toMatchObject({ message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.", status: 0 })`; :149 `expect(fetchSpy).toHaveBeenCalledTimes(1)` | rejeição com mensagem/status0, sem sucesso/reenvio nas três gravações |

### Adequação reversa (Check C/D)

| Assertion / caso | Requisito | Manter |
| --- | --- | --- |
| api.test.js:119/120, GETestrutura | ETA01/29 | Sim |
| api.test.js:127–133, três métodos/DTOs/bodies | ETA12/13/15/28 | Sim |
| api.test.js:139/140, três conflitos | ETA27/29 | Sim |
| api.test.js:146/149, três falhas de rede | ETA27 | Sim |

Adequação PASS. As assertions conferem valores do DTO/payload e mensagens, além de chamadas. Padrões Vitest e skill seguidos, sem AGENTS adicional. Nenhum teste removido/ignorado/enfraquecido; nenhuma SPEC_DEVIATION. Formulário/quadro/teclado são verificados nas próximas tarefas.
