import { motion } from 'motion/react'

const VALUES = [
  { t: 'Private by design', d: 'Every transaction lives in a local database on your device. No accounts, no cloud.' },
  { t: 'Plan vs actual', d: 'Set monthly targets and see exactly where reality diverged, color-coded.' },
  { t: 'Recurring made simple', d: 'Salary, rent and bills as one-tap monthly templates.' },
  { t: 'Export anywhere', d: 'Hand off a month as a 3-sheet Excel workbook or a PDF report.' },
]

export function ValueStrip() {
  return (
    <section className="mx-auto max-w-6xl px-6 py-16">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {VALUES.map((v, i) => (
          <motion.div key={v.t} className="rounded-card bg-panel p-5 line-border"
            initial={{ opacity: 0, y: 16 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, margin: '-80px' }}
            transition={{ duration: 0.45, delay: i * 0.06 }}>
            <h3 className="font-semibold">{v.t}</h3>
            <p className="mt-2 text-sm text-muted">{v.d}</p>
          </motion.div>
        ))}
      </div>
    </section>
  )
}
