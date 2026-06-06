import { motion } from 'motion/react'
import type { Feature } from '../data/features'
import { Badge } from '../ui/Badge'
import { PhoneFrame } from '../ui/PhoneFrame'

export function FeaturePanel({ feature, index, onActivate }: { feature: Feature; index: number; onActivate: (i: number) => void }) {
  return (
    <motion.div
      className="flex min-h-[80vh] flex-col justify-center py-10 lg:min-h-screen"
      onViewportEnter={() => onActivate(index)}
      viewport={{ margin: '-50% 0px -50% 0px' }}
    >
      <Badge tone="mono">{feature.id} · {feature.tab}</Badge>
      <h3 className="mt-4 text-2xl font-bold sm:text-3xl">{feature.title}</h3>
      <p className="mt-3 max-w-md text-muted">{feature.blurb}</p>
      <ul className="mt-5 space-y-2">
        {feature.bullets.map((b) => (
          <li key={b} className="flex items-start gap-2 text-sm">
            <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-accent" />
            <span>{b}</span>
          </li>
        ))}
      </ul>
      <div className="mt-8 lg:hidden">
        <PhoneFrame src={feature.screen} alt={feature.alt} />
      </div>
    </motion.div>
  )
}
