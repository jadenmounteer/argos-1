/**
 * Placeholder hook for computer voice / TTS.
 * Later this will connect to window.speechSynthesis or an external TTS service.
 */
export function useComputerVoice() {
  return {
    speak: (text: string) => {
      void text
    },
    cancel: () => {},
    isSpeaking: false,
  }
}

