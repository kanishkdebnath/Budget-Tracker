import { motion } from 'motion/react'
import { POWER_FEATURES } from '../data/features'
import { SectionLabel } from '../ui/SectionLabel'
import { Badge } from '../ui/Badge'
import { PhoneFrame } from '../ui/PhoneFrame'

export function PowerFeatures() {
  return (
    <section className="mx-auto max-w-6xl px-6 py-20">
      <SectionLabel>And there’s more</SectionLabel>
      <h2 className="mt-3 max-w-xl text-3xl font-bold sm:text-4xl">Power features that round it out</h2>
      <div className="mt-12 grid gap-6 lg:grid-cols-3">
        {POWER_FEATURES.map((f, i) => (
          <motion.div key={f.id} className="rounded-card bg-panel p-6 line-border"
            initial={{ opacity: 0, y: 18 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, margin: '-60px' }}
            transition={{ duration: 0.5, delay: i * 0.08 }}>
            <Badge tone="mono">{f.id} · {f.tab}</Badge>
            <h3 className="mt-4 text-xl font-bold">{f.title}</h3>
            <p className="mt-2 text-sm text-muted">{f.blurb}</p>
            <ul className="mt-4 space-y-1.5">
              {f.bullets.map((b) => (
                <li key={b} className="flex items-start gap-2 text-sm text-muted">
                  <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-accent" />{b}
                </li>
              ))}
            </ul>
            <div className="mt-6 origin-top scale-90">
              <PhoneFrame src={f.screen} alt={f.alt} />
            </div>
          </motion.div>
        ))}
      </div>
    </section>
  )
}
