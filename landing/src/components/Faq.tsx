import { useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import { SectionLabel } from '../ui/SectionLabel'

const QA = [
  { q: 'Is my data private?', a: 'Yes. Everything is stored locally on your device — there are no accounts and no servers.' },
  { q: 'Does it work offline?', a: 'Completely. The app is offline-first; it never needs a network connection.' },
  { q: 'Which currencies are supported?', a: 'A single display currency you choose — common ISO-4217 codes are promoted (with nation flags), and any valid 3-letter code works. Changing it reformats every amount.' },
  { q: 'Can I export my data?', a: 'Yes — export any month as a 3-sheet Excel workbook or a PDF report and share it via the Android share sheet.' },
  { q: 'Is it free and open-source?', a: 'The source lives on GitHub. A Play Store listing is on the way.' },
]

export function Faq() {
  const [open, setOpen] = useState<number | null>(0)
  return (
    <section id="faq" className="mx-auto max-w-3xl px-6 py-20">
      <SectionLabel>FAQ</SectionLabel>
      <h2 className="mt-3 text-3xl font-bold sm:text-4xl">Questions, answered</h2>
      <div className="mt-10 divide-y divide-white/6 overflow-hidden rounded-card bg-panel line-border">
        {QA.map((item, i) => {
          const isOpen = open === i
          return (
            <div key={item.q}>
              <button className="flex w-full items-center justify-between gap-4 px-5 py-4 text-left font-medium"
                aria-expanded={isOpen} onClick={() => setOpen(isOpen ? null : i)}>
                {item.q}
                <span className={`text-muted transition-transform ${isOpen ? 'rotate-45' : ''}`}>+</span>
              </button>
              <AnimatePresence initial={false}>
                {isOpen && (
                  <motion.div initial={{ height: 0, opacity: 0 }} animate={{ height: 'auto', opacity: 1 }} exit={{ height: 0, opacity: 0 }}
                    transition={{ duration: 0.25 }} className="overflow-hidden">
                    <p className="px-5 pb-5 text-sm text-muted">{item.a}</p>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          )
        })}
      </div>
    </section>
  )
}
