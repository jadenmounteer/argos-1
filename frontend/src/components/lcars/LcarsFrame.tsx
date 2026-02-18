import type { ReactNode } from 'react'

type LcarsFrameProps = {
  sidebar: ReactNode
  main: ReactNode
  status?: ReactNode
}

/**
 * High-level LCARS frame (sweep/elbow). Library has no frame component, so we use
 * layout (Tailwind) + library palette (CSS vars) only.
 */
export function LcarsFrame({ sidebar, main, status }: LcarsFrameProps) {
  return (
    <div className="min-h-screen flex flex-col bg-[var(--lcars-color-black)] text-[var(--lcars-color-a5)]">
      {/* Top bar – LCARS orange */}
      <div className="h-16 flex items-center justify-between px-6 rounded-br-3xl bg-[var(--lcars-color-a1)]">
        {status}
      </div>

      <div className="flex flex-1 min-h-0">
        {/* Left elbow + sidebar */}
        <div className="relative w-64 shrink-0">
          <div className="h-20 rounded-br-3xl bg-[var(--lcars-color-a9)]" aria-hidden />
          <div className="absolute top-16 left-0 right-0 bottom-0 bg-[var(--lcars-color-black)]">
            <div className="h-full pt-6 pr-3 flex flex-col gap-2">{sidebar}</div>
          </div>
        </div>

        {/* Main viewport – min-w-0 so flex doesn’t let content bleed into sidebar */}
        <div className="flex-1 min-w-0 p-6">{main}</div>
      </div>
    </div>
  )
}

