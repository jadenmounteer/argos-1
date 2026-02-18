import { VocalProvider } from './context/VocalContext'
import { useIntelligenceStream } from './hooks/useIntelligenceStream'
import { AppShell } from './components/layout/AppShell'

function AppWithVocal() {
  const { sendCommand, thoughtLog, mainResponse } = useIntelligenceStream()
  return (
    <VocalProvider onCommandReady={sendCommand}>
      <AppShell thoughtLog={thoughtLog} mainResponse={mainResponse} />
    </VocalProvider>
  )
}

export default function App() {
  return <AppWithVocal />
}
