import { useEffect, useState } from 'react'
import { GradientButton } from '../ui/GradientButton'

const APP_NAME = 'Budget Tracker'
const GITHUB_URL = 'https://github.com/kanishkdebnath/Budget-Tracker'

export function Nav() {
  const [scrolled, setScrolled] = useState(false)
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])
  return (
    <header className={`fixed inset-x-0 top-0 z-50 transition-colors ${scrolled ? 'bg-bg/80 backdrop-blur-md border-b border-white/6' : ''}`}>
      <nav className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <a href="#top" className="flex items-center gap-2.5 font-semibold">
          <span className="h-3 w-3 rounded-full bg-brand-gradient ring-1 ring-white/20" />
          {APP_NAME}
        </a>
        <div className="hidden items-center gap-7 text-sm text-muted md:flex">
          <a href="#features" className="hover:text-ink">Features</a>
          <a href="#privacy" className="hover:text-ink">Privacy</a>
          <a href="#faq" className="hover:text-ink">FAQ</a>
        </div>
        <GradientButton href={GITHUB_URL}>Get the app</GradientButton>
      </nav>
    </header>
  )
}
