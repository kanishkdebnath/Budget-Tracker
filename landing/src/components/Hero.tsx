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
          <div className="mx-auto mb-5 grid w-[260px] max-w-full grid-cols-3 gap-2 rounded-card bg-brand-gradient px-5 py-4 line-border">
            <div className="flex flex-col">
              <span className="text-[11px] text-white/65">Income</span>
              <CountUp to={78000} prefix="₹" className="text-sm font-semibold text-income" />
            </div>
            <div className="flex flex-col">
              <span className="text-[11px] text-white/65">Expense</span>
              <CountUp to={36000} prefix="₹" className="text-sm font-semibold text-white" />
            </div>
            <div className="flex flex-col">
              <span className="text-[11px] text-white/65">Net</span>
              <CountUp to={42000} prefix="₹" className="text-sm font-semibold text-income" />
            </div>
          </div>
          <PhoneFrame src="/screens/log.png" alt="Budget Tracker Log screen" />
        </motion.div>
      </div>
    </section>
  )
}
