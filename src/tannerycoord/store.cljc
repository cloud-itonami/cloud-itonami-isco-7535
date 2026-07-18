(ns tannerycoord.store
  "SSoT for the ISCO-08 7535 pelt dressers, tanners and fellmongers
  tannery scheduling/logistics coordination actor (itonami actor
  pattern, ADR-2607121000 / CLAUDE.md Actors section; README's
  'Robotics premise' — a tannery scheduling/logistics coordination
  robot performs crew scheduling, batch/inventory/progress-record
  logging and tanning-chemicals/raw-hide supply-order coordination for
  a pelt-dressing/tanning/fellmongering crew under this
  advisor/governor pair, which never dispatches hardware itself, never
  performs tanning work itself, and never finalizes a
  tanning-execution decision or a chemical-safety-clearance decision,
  and never overrides a shop safety officer's judgment — those remain
  the shop safety officer's exclusive judgment). Modeled closely on
  cloud-itonami-isco-7521's woodtreatcoord.store.

  Domain:

    tanner   — a registered pelt-dressing/tanning/fellmongering crew
               member (:tanner-id, :name)
    facility — a registered tannery site {:facility-id :name
               :max-supply-cost number}. `:max-supply-cost` is an
               informational registered ceiling used only to decide
               whether a `:coordinate-supply-order` proposal escalates
               to human sign-off (the governor never blocks a
               within-threshold order outright; it only decides
               commit vs. escalate).
    record   — a committed operating record (a logged batch/inventory/
               progress entry, a scheduled crew/tannery-cycle
               operation, a flagged safety concern, or a coordinated
               tanning-chemicals/raw-hide supply order) — written ONLY
               via commit-record!.
    ledger   — append-only audit trail, commit or hold.")

(defprotocol Store
  (tanner [s tanner-id])
  (facility [s facility-id])
  (records-of [s tanner-id])
  (ledger [s])
  (register-tanner! [s tanner])
  (register-facility! [s facility])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (tanner [_ tanner-id] (get-in @a [:tanners tanner-id]))
  (facility [_ facility-id] (get-in @a [:facilities facility-id]))
  (records-of [_ tanner-id] (filter #(= tanner-id (:tanner-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-tanner! [s t]
    (swap! a assoc-in [:tanners (:tanner-id t)] t) s)
  (register-facility! [s f]
    (swap! a assoc-in [:facilities (:facility-id f)] f) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:tanners {} :facilities {} :records [] :ledger []}
                                    seed)))))
