import { motion } from 'motion/react'
import { GradientButton } from '../ui/GradientButton'
import { PhoneFrame } from '../ui/PhoneFrame'
import { CountUp } from '../ui/CountUp'

const GITHUB_URL = 'https://github.com/kanishkdebnath/Budget-Tracker'
const CHIPS = ['Offline-first', 'On-device', 'No account', 'Multi-currency']

export function Hero() {
  return (
    <section className="relative overflow-hidden bg-hero-glow pt-28 pb-20">
      <div className="mx-auto grid max-w-6xl items-center gap-12 px-6 lg:grid-cols-2">
        <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
          <h1 className="text-4xl font-bold leading-tight sm:text-5xl">Plan every rupee.<br /><span className="text-accent">Privately.</span></h1>
          <p className="mt-5 max-w-md text-lg text-muted">An offline-first personal budgeting app for Android. Targets, reports, recurring entries and exports — all on your phone, no account, no servers.</p>
          <div className="mt-8 flex flex-wrap items-center gap-3">
            <GradientButton href={GITHUB_URL}>Coming soon — Google Play</GradientButton>
            <GradientButton href={GITHUB_URL} variant="ghost">View source</GradientButton>
          </div>
          <div className="mt-7 flex flex-wrap gap-2">
            {CHIPS.map((c) => (
              <span key={c} className="rounded-full bg-panel px-3 py-1 text-xs text-muted line-border">{c}</span>
            ))}
          </div>
        </motion.div>
        <motion.div initial={{ opacity: 0, scale: 0.96 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.7, delay: 0.1 }}>
          <div className="mx-auto mb-5 w-[260px] max-w-full rounded-card bg-brand-gradient px-5 py-4 line-border">
            <div className="flex justify-between text-xs text-white/70">
              <span>Income</span><span>Expense</span><span>Net</span>
            </div>
            <div className="mt-1 flex justify-between text-lg font-semibold">
              <CountUp to={78000} prefix="₹" className="text-income" />
              <CountUp to={36000} prefix="₹" className="text-white" />
              <CountUp to={42000} prefix="₹" className="text-income" />
            </div>
          </div>
          <PhoneFrame src="/screens/log.png" alt="Budget Tracker Log screen" />
        </motion.div>
      </div>
    </section>
  )
}
