import { GradientButton } from '../ui/GradientButton'

const GITHUB_URL = 'https://github.com/kanishkdebnath/Budget-Tracker'

export function Footer() {
  return (
    <footer className="border-t border-white/6 bg-panel">
      <div className="mx-auto max-w-6xl px-6 py-16 text-center">
        <h2 className="text-2xl font-bold sm:text-3xl">Plan every rupee. Privately.</h2>
        <p className="mx-auto mt-3 max-w-md text-muted">Offline-first budgeting that lives on your phone.</p>
        <div className="mt-6 flex justify-center gap-3">
          <GradientButton href={GITHUB_URL}>Coming soon — Google Play</GradientButton>
          <GradientButton href={GITHUB_URL} variant="ghost">View source on GitHub</GradientButton>
        </div>
        <p className="mt-10 text-xs text-muted">Budget Tracker · Built with care, no servers attached.</p>
      </div>
    </footer>
  )
}
