# LESSONS - auto-maintained by scripts/lessons.py

> Machine-owned. Do NOT hand-edit. Changes are overwritten on the next `lessons.py` write.
> Canonical state lives in `.specs/lessons.json`. Edit lessons only via the script.
> promote_threshold=2 distinct features · window_days=45 · quarantine_threshold=2

## Confirmed (load these at Specify/Design)

Corroborated across multiple features. Safe to apply as guidance.

_none_

## Candidates (under observation - do NOT load as guidance yet)

Seen once or not yet corroborated. Tracked, not trusted.

### L-001 - Exercite todos os controles do CRUD com Tab, confira o foco e ative por Enter; cliques não comprovam operação por teclado.
- signal: `surviving_mutant` · recurrence: 1 feature(s) · scope: `frontend` · harmful: 0
- features: gerenciamentos
- evidence: M9 (frontend)
- last seen: 2026-10-05T04:48:05Z

### L-002 - Em mutações HTTP, teste respostas 2xx com JSON ilegível e exija erro de comunicação, rascunho preservado e nenhum reenvio automático.
- signal: `surviving_mutant` · recurrence: 1 feature(s) · scope: `frontend` · harmful: 0
- features: etapas
- evidence: .specs/features/etapas/validation.md:M10 / ETA-27 (frontend)
- last seen: 2026-10-05T11:21:53Z

### L-003 - Confirme o registro criado e a versão da operação antes de aceitar um snapshot de cadastro como sucesso.
- signal: `ac_gap` · recurrence: 1 feature(s) · scope: `frontend` · harmful: 0
- features: demandas-cadastro
- evidence: CAD-33; frontend/src/services/api.js:161; frontend/src/services/api.test.js:190 (frontend)
- last seen: 2026-10-06T21:11:06Z

## Quarantined (failed when applied - ignore)

A confirmed lesson that recurred alongside failure. Kept for the maintainer to review.

_none_
