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
      <div style={{ display: 'flex', gap: '1rem' }}>
        <aside>
          <ButtonStrip />
          <PromptArea />
        </aside>
        <main>
          <LogsView />
        </main>
      </div>
    </MainWrapper>
  )
}
