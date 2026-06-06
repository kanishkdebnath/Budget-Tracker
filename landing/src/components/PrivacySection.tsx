import { motion } from 'motion/react'
import { SectionLabel } from '../ui/SectionLabel'

const POINTS = [
  { t: 'No account, ever', d: 'There’s nothing to sign up for. Open the app and start.' },
  { t: 'No servers', d: 'Your data is stored in a local database (Room) and preferences on-device. Nothing leaves your phone.' },
  { t: 'No ads, no tracking', d: 'No analytics SDKs, no third-party trackers.' },
]

export function PrivacySection() {
  return (
    <section id="privacy" className="relative overflow-hidden bg-hero-glow py-20">
      <div className="mx-auto max-w-6xl px-6">
        <SectionLabel>Private by design</SectionLabel>
        <h2 className="mt-3 max-w-2xl text-3xl font-bold sm:text-4xl">Your money is nobody else’s business</h2>
        <div className="mt-12 grid gap-6 md:grid-cols-3">
          {POINTS.map((p, i) => (
            <motion.div key={p.t} className="rounded-card bg-panel/70 p-6 line-border backdrop-blur"
              initial={{ opacity: 0, y: 16 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, margin: '-60px' }}
              transition={{ duration: 0.45, delay: i * 0.07 }}>
              <h3 className="font-semibold">{p.t}</h3>
              <p className="mt-2 text-sm text-muted">{p.d}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
