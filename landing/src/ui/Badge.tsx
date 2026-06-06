export function Badge({ children, tone = 'default' }: { children: React.ReactNode; tone?: 'default' | 'mono' }) {
  const base = 'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[12px] line-border'
  const toneClass = tone === 'mono' ? 'font-mono text-accent bg-panel2' : 'text-muted bg-panel'
  return <span className={`${base} ${toneClass}`}>{children}</span>
}
