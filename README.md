# cloud-itonami-isco-7535

Open Occupation Blueprint for **ISCO-08 7535**: Pelt Dressers, Tanners and
Fellmongers.

This repository designs a forkable OSS business for a tannery scheduling and
logistics coordination practice: a tannery scheduling and supply-coordination
robot manages crew/task records under a governor-gated actor, so a
pelt-dressing/tanning/fellmongering crew keeps its own operating records
instead of renting a closed workforce-management SaaS.

**Maturity: `:implemented`.** `src/tannerycoord/` implements the
`TanneryCoordActor` as a `langgraph.graph/state-graph`
(`tannerycoord.actor`) wired to a `Tannery Scheduling Coordination Advisor`
(`tannerycoord.advisor`) and an independent `TanneryCoordGovernor`
(`tannerycoord.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok? true) +-> :request-approval (:escalate? true, human-in-the-loop
interrupt) +-> :hold (:hard? true)`. HARD invariants (always hold, never
overridable): tanner provenance, facility provenance, no-actuation
(`:effect` must be `:propose`), a closed op-allowlist (`:log-work-record`,
`:schedule-crew-operation`, `:flag-safety-concern`,
`:coordinate-supply-order` — nothing else may ever be proposed), and a
permanent, unconditional block on any proposal that would directly finalize
a tanning-execution decision (e.g. deciding to proceed with a specific
pelt-tanning run) or a chemical-safety-clearance decision (e.g. declaring a
tanned batch safe for handling or shipment), or that would override a shop
safety officer's judgment. Always-escalate paths (human sign-off regardless
of confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)): `:flag-safety-concern`
(always) and `:coordinate-supply-order` above the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a tannery scheduling/logistics coordination
robot performs crew scheduling, batch/inventory/progress-record logging and
tanning-chemicals/raw-hide supply-order coordination for a pelt-dressing/
tanning/fellmongering crew, under an actor that proposes actions and an
independent **Tannery Scheduling Coordination Governor** that gates them.
The governor never dispatches hardware itself, never performs tanning work
on the tannery floor, and never finalizes a tanning-execution decision or a
chemical-safety-clearance decision, and never overrides a shop safety
officer's judgment; `:high`/`:safety-critical` actions (such as a flagged
chemical-exposure/ventilation/equipment-condition concern, or an
above-threshold supply order) require human sign-off. **This actor
coordinates TANNERY SCHEDULING/LOGISTICS ONLY — it never performs tanning
work itself, and it never makes a chemical-safety-clearance decision
itself.**

Pelt dressers, tanners and fellmongers process raw hides and pelts using
tanning chemicals (historically including chromium compounds and other
hazardous agents), alongside biological-material handling. This is a real
chemical-exposure and biological-material-handling hazard domain; this actor
never performs that work and never clears it as safe — it only schedules and
logs around it, and always routes chemical-exposure/safety concerns to a
human shop safety officer.

## Core Contract

```text
crew roster + facility registration + safety-reporting policy
        |
        v
Tannery Scheduling Coordination Advisor -> TanneryCoordGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses,
finalize a tanning-execution decision, finalize a chemical-safety-clearance
decision, override a shop safety officer's judgment, suppress an operating
record, or disclose sensitive data without governor approval and audit
evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7535`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
