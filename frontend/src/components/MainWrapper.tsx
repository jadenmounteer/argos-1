import type { ReactNode } from 'react'

type MainWrapperProps = {
  /** Renders in the break at the top (e.g. title "Argos 1") */
  titleSlot: ReactNode
  /** Main content below the top frame */
  children: ReactNode
}

/**
 * Purely aesthetic LCARS frame around the page. Top bar has a break for the title;
 * right and bottom bars complete the wrapper.
 */
export function MainWrapper({ titleSlot, children }: MainWrapperProps) {
  return (
    <div className="main-wrapper">
      <div className="main-wrapper__top">
        <div className="main-wrapper__sweep-left" aria-hidden />
        <div className="main-wrapper__title-slot">{titleSlot}</div>
        <div className="main-wrapper__sweep-right" aria-hidden />
      </div>
      <div className="main-wrapper__body">{children}</div>
      <div className="main-wrapper__right-bar" aria-hidden />
      <div className="main-wrapper__bottom-bar" aria-hidden />
    </div>
  )
}
