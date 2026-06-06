import { Nav } from './components/Nav'
import { Hero } from './components/Hero'
import { ValueStrip } from './components/ValueStrip'
import { FeatureScrolly } from './components/FeatureScrolly'
import { PowerFeatures } from './components/PowerFeatures'
import { CraftSection } from './components/CraftSection'
import { PrivacySection } from './components/PrivacySection'
import { Faq } from './components/Faq'
import { Footer } from './components/Footer'

export default function App() {
  return (
    <div id="top" className="min-h-screen bg-bg text-ink">
      <Nav />
      <main>
        <Hero />
        <ValueStrip />
        <FeatureScrolly />
        <PowerFeatures />
        <CraftSection />
        <PrivacySection />
        <Faq />
      </main>
      <Footer />
    </div>
  )
}
