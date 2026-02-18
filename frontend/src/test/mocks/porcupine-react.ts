let _isLoaded = false
let _keywordDetection: { label: string; index: number } | null = null

export function usePorcupine() {
  return {
    keywordDetection: _keywordDetection,
    isLoaded: _isLoaded,
    error: null as Error | null,
    init: async () => {
      _isLoaded = true
    },
    start: () => {},
    stop: () => {},
    release: () => {},
  }
}

/** Test helper: set mock state so wake word path can be simulated. */
export function __setPorcupineMock(
  isLoaded: boolean,
  keywordDetection: { label: string; index: number } | null,
) {
  _isLoaded = isLoaded
  _keywordDetection = keywordDetection
}
