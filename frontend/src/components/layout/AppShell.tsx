import { MainWrapper } from '../MainWrapper'
import { Header } from '../Header'
import { ButtonStrip } from '../ButtonStrip'
import { PromptArea } from '../PromptArea'
import { LogsView } from '../LogsView'

/**
 * Root layout: main wrapper (frame with title break), then two columns —
 * left: buttons + prompt, right: logs and responses.
 */
export function AppShell({
  thoughtLog = '',
  mainResponse = '',
  isStreaming = false,
}: {
  thoughtLog?: string
  mainResponse?: string
  isStreaming?: boolean
}) {
  return (
    <MainWrapper titleSlot={<Header />} loading={isStreaming}>
      <div className="app-shell__body">
        <aside className="aside-panel">
          <ButtonStrip />
          <PromptArea />
        </aside>
        <main className="logs-column">
          <LogsView thoughtLog={thoughtLog} mainResponse={mainResponse} isStreaming={isStreaming} />
        </main>
      </div>
    </MainWrapper>
  )
}
