import { useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import { CORE_FEATURES } from '../data/features'
import { SectionLabel } from '../ui/SectionLabel'
import { PhoneFrame } from '../ui/PhoneFrame'
import { FeaturePanel } from './FeaturePanel'

export function FeatureScrolly() {
  const [active, setActive] = useState(0)
  const feature = CORE_FEATURES[active]
  return (
    <section id="features" className="mx-auto max-w-6xl px-6 py-20">
      <SectionLabel>The five screens</SectionLabel>
      <h2 className="mt-3 max-w-xl text-3xl font-bold sm:text-4xl">Built around how a month actually unfolds</h2>
      <div className="mt-12 grid gap-10 lg:grid-cols-2">
        <div className="hidden lg:block">
          <div className="sticky top-[14vh]">
            <AnimatePresence mode="wait">
              <motion.div key={feature.id}
                initial={{ opacity: 0, x: 24 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -24 }}
                transition={{ duration: 0.35 }}>
                <PhoneFrame src={feature.screen} alt={feature.alt} />
              </motion.div>
            </AnimatePresence>
          </div>
        </div>
        <div>
          {CORE_FEATURES.map((f, i) => (
            <FeaturePanel key={f.id} feature={f} index={i} onActivate={setActive} />
          ))}
        </div>
      </div>
    </section>
  )
}
