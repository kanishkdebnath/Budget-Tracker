import { SectionLabel } from '../ui/SectionLabel'

const SWATCHES = [
  { name: 'Brand', hex: '#0d2736' },
  { name: 'Brand 25', hex: '#143548' },
  { name: 'Brand 30', hex: '#1B4561' },
  { name: 'Accent', hex: '#9CC8DE' },
  { name: 'Income', hex: '#74D9B5' },
  { name: 'Overage', hex: '#FFB4A8' },
]

export function CraftSection() {
  return (
    <section className="mx-auto max-w-6xl px-6 py-20">
      <SectionLabel>Designed down to the pixel</SectionLabel>
      <h2 className="mt-3 max-w-xl text-3xl font-bold sm:text-4xl">One navy system, light and dark</h2>
      <div className="mt-12 grid gap-6 lg:grid-cols-3">
        <div className="rounded-card bg-panel p-6 line-border">
          <h3 className="font-semibold">Brand palette</h3>
          <div className="mt-4 grid grid-cols-3 gap-2">
            {SWATCHES.map((s) => (
              <div key={s.name} className="flex flex-col justify-end rounded-lg p-3 font-mono text-[10px]"
                style={{ background: s.hex, color: s.hex === '#0d2736' || s.hex === '#143548' || s.hex === '#1B4561' ? '#cfe0ea' : '#0b1117', aspectRatio: '1 / 1' }}>
                {s.hex}
              </div>
            ))}
          </div>
        </div>
        <div className="rounded-card bg-panel p-6 line-border">
          <h3 className="font-semibold">Money in tabular numerals</h3>
          <p className="mt-2 text-sm text-muted">Color is never the only signal — every delta carries a sign.</p>
          <div className="mt-4 space-y-1 font-mono text-lg tnum">
            <div className="text-income">+₹7,400</div>
            <div className="text-overage">−₹2,000</div>
            <div className="text-ink">₹5,400</div>
          </div>
        </div>
        <div className="rounded-card bg-panel p-6 line-border">
          <h3 className="font-semibold">Lively chrome, calm data</h3>
          <p className="mt-2 text-sm text-muted">Navigation and CTAs animate with intent; numbers stay still and legible. Material 3 Expressive, anchored to brand navy.</p>
          <div className="mt-4 h-2 w-full rounded-full bg-accent-bar" />
        </div>
      </div>
    </section>
  )
}
