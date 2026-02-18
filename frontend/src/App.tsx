import { VocalProvider } from './context/VocalContext'
import { useIntelligenceStream } from './hooks/useIntelligenceStream'
import { AppShell } from './components/layout/AppShell'

function AppWithVocal() {
  const { sendCommand } = useIntelligenceStream()
  return (
    <VocalProvider onCommandReady={sendCommand}>
      <AppShell />
    </VocalProvider>
  )
}

export default function App() {
  return <AppWithVocal />
}
