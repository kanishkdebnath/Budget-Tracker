import { Nav } from './components/Nav'
import { Hero } from './components/Hero'
import { ValueStrip } from './components/ValueStrip'
import { FeatureScrolly } from './components/FeatureScrolly'

export default function App() {
  return (
    <div id="top" className="min-h-screen bg-bg text-ink">
      <Nav />
      <main>
        <Hero />
        <ValueStrip />
        <FeatureScrolly />
        {/* more sections added in later tasks */}
      </main>
    </div>
  )
}
