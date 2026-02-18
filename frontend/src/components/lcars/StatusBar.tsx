import { LcarsButton } from '@starfleet-technology/lcars-react'

export function StatusBar() {
  const stardate = '72230.5'

  return (
    <div className="argos-status-bar flex w-full items-center justify-between gap-4">
      <LcarsButton color="primary">Argos-1 Tactical Console</LcarsButton>
      <LcarsButton color="primary">Stardate {stardate}</LcarsButton>
    </div>
  )
}

