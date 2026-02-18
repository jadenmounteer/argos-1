import { LcarsButton } from '@starfleet-technology/lcars-react'

/**
 * Top-left header: status/icon placeholder + title "Argos 1".
 * Uses lcars-react for the title pill.
 */
export function Header() {
  return (
    <header style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
      <div data-status aria-hidden style={{ width: 48, height: 24 }} />
      <LcarsButton color="primary">Argos 1</LcarsButton>
    </header>
  )
}
