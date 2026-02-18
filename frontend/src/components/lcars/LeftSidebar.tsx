import { LcarsButton } from '@starfleet-technology/lcars-react'

type LeftSidebarProps = {
  voiceEnabled: boolean
  onToggleVoice?: () => void
}

/** Map sidebar modes to lcars-react LcarsButton color tokens */
const SIDEBAR_COLORS = {
  overview: 'primary',
  voice: 'primary',
  diagnostics: 'primary',
  systems: 'primary',
  logs: 'primary',
} as const

export function LeftSidebar({ voiceEnabled, onToggleVoice }: LeftSidebarProps) {
  return (
    <nav aria-label="Primary console modes" className="flex flex-col gap-2">
      <LcarsButton color={SIDEBAR_COLORS.overview}>Overview</LcarsButton>
      <LcarsButton
        color={SIDEBAR_COLORS.voice}
        onClick={onToggleVoice}
      >
        Voice: {voiceEnabled ? 'On' : 'Off'}
      </LcarsButton>
      <LcarsButton color={SIDEBAR_COLORS.diagnostics}>Diagnostics</LcarsButton>
      <LcarsButton color={SIDEBAR_COLORS.systems}>Systems</LcarsButton>
      <LcarsButton color={SIDEBAR_COLORS.logs}>Logs</LcarsButton>
    </nav>
  )
}

