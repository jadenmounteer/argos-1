import { LcarsButton } from '@starfleet-technology/lcars-react'
import { useVocal } from '../context/VocalContext'

/**
 * Four buttons in a strip (2x2). First button toggles Listening Mode (wake word only).
 */
export function ButtonStrip() {
  const { listeningMode, toggleListeningMode, porcupineLoaded, wakeWordUnavailableReason, isActive } = useVocal()
  const status =
    !listeningMode
      ? null
      : isActive
        ? 'Listening…'
        : porcupineLoaded
          ? 'Say "Argos one"'
          : wakeWordUnavailableReason ?? 'Loading…'
  return (
    <div className="button-strip-wrapper">
      <div className="button-strip">
        <LcarsButton color="primary" onClick={toggleListeningMode} aria-pressed={listeningMode} aria-label={listeningMode ? 'Listening on' : 'Listening off'}>
          {listeningMode ? '🎙️' : '🔕'}
        </LcarsButton>
        <LcarsButton color="primary">2</LcarsButton>
        <LcarsButton color="primary">3</LcarsButton>
        <LcarsButton color="primary">4</LcarsButton>
      </div>
      {status && (
        <p className="button-strip-status" role="status">
          {status}
        </p>
      )}
    </div>
  )
}
