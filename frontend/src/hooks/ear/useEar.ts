/**
 * Placeholder hook for the \"ears\" of ARGOS-1.
 * Task 2 will provide a real implementation using the Web Speech API.
 */
export type EarMode = 'passive' | 'active'

export function useEar() {
  return {
    mode: 'passive' as EarMode,
    transcript: '',
    // TODO: implement real wake-word and transcription handling
    activate: () => {},
    deactivate: () => {},
  }
}

