import { LcarsButton } from '@starfleet-technology/lcars-react'

/**
 * Four buttons in a strip (mock: 2x2). Uses lcars-react LcarsButton.
 */
export function ButtonStrip() {
  return (
    <div className="button-strip">
      <LcarsButton color="primary">1</LcarsButton>
      <LcarsButton color="primary">2</LcarsButton>
      <LcarsButton color="primary">3</LcarsButton>
      <LcarsButton color="primary">4</LcarsButton>
    </div>
  )
}
