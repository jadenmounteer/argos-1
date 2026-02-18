const dummyEntries = [
  {
    id: 1,
    timestamp: '00:00:01',
    text: 'Awaiting first command. Systems nominal.',
  },
  {
    id: 2,
    timestamp: '00:00:05',
    text: 'Sample diagnostic entry. This area will stream ARGOS-1 thoughts and responses.',
  },
]

export function TacticalLog() {
  return (
    <div className="flex flex-col gap-3 text-sm">
      {dummyEntries.map((entry) => (
        <article
          key={entry.id}
          className="rounded-xl border border-[var(--lcars-color-a9)]/60 bg-[var(--lcars-color-black)] px-4 py-2 shadow-sm"
        >
          <header className="mb-1 flex items-center justify-between text-[0.65rem] uppercase tracking-[0.2em] text-[var(--lcars-color-a9)]">
            <span>Entry {entry.id.toString().padStart(3, '0')}</span>
            <span>{entry.timestamp}</span>
          </header>
          <p className="text-[var(--lcars-color-a5)]/90">{entry.text}</p>
        </article>
      ))}
    </div>
  )
}

