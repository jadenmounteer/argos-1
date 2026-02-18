import { LcarsButton } from '@starfleet-technology/lcars-react'
import { TacticalLog } from '../log/TacticalLog'

export function MainViewport() {
  return (
    <section
      aria-label="Tactical viewport"
      className="h-full min-h-0 w-full rounded-3xl border-2 border-[var(--lcars-color-a9)] bg-[var(--lcars-color-black)] px-6 py-4 flex flex-col gap-4"
    >
      <header className="argos-viewport-header flex items-center justify-between gap-4 flex-wrap">
        <LcarsButton color="primary">Tactical Log</LcarsButton>
        <LcarsButton color="primary">Sector: Prototype</LcarsButton>
      </header>

      <div className="flex-1 overflow-y-auto">
        <TacticalLog />
      </div>
    </section>
  )
}

