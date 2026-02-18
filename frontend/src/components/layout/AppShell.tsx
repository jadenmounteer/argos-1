import { useState } from 'react'
import { LcarsFrame } from '../lcars/LcarsFrame'
import { LeftSidebar } from '../lcars/LeftSidebar'
import { MainViewport } from '../lcars/MainViewport'
import { StatusBar } from '../lcars/StatusBar'

export function AppShell() {
  const [voiceEnabled, setVoiceEnabled] = useState(false)

  return (
    <LcarsFrame
      sidebar={
        <LeftSidebar
          voiceEnabled={voiceEnabled}
          onToggleVoice={() => setVoiceEnabled((prev) => !prev)}
        />
      }
      main={<MainViewport />}
      status={<StatusBar />}
    />
  )
}

