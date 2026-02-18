import { useCallback } from 'react'
import { VocalProvider } from './context/VocalContext'
import { useComputerVoice } from './hooks/audio/useComputerVoice'
import { useIntelligenceStream } from './hooks/useIntelligenceStream'
import { AppShell } from './components/layout/AppShell'

function AppWithVocal() {
  const { speak, cancel, isSpeaking } = useComputerVoice()
  const {
    sendCommand: streamSendCommand,
    thoughtLog,
    mainResponse,
    isStreaming,
  } = useIntelligenceStream({
    onResponseFinalized: (text) => {
      if (text.trim()) speak(text.trim())
    },
  })
  const sendCommand = useCallback(
    (text: string) => {
      cancel()
      streamSendCommand(text)
    },
    [cancel, streamSendCommand],
  )
  return (
    <VocalProvider onCommandReady={sendCommand} inhibitWhileSpeaking={isSpeaking}>
      <AppShell thoughtLog={thoughtLog} mainResponse={mainResponse} isStreaming={isStreaming} />
    </VocalProvider>
  )
}

export default function App() {
  return <AppWithVocal />
}
