import { MainWrapper } from '../MainWrapper'
import { Header } from '../Header'
import { ButtonStrip } from '../ButtonStrip'
import { PromptArea } from '../PromptArea'
import { LogsView } from '../LogsView'

/**
 * Root layout: main wrapper (frame with title break), then two columns —
 * left: buttons + prompt, right: logs and responses.
 */
export function AppShell() {
  return (
    <MainWrapper titleSlot={<Header />}>
      <div className="app-shell__body">
        <aside className="aside-panel">
          <ButtonStrip />
          <PromptArea />
        </aside>
        <main className="logs-column">
          <LogsView />
        </main>
      </div>
    </MainWrapper>
  )
}
