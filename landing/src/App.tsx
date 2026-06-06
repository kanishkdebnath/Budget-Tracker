import { Nav } from './components/Nav'
import { Hero } from './components/Hero'

export default function App() {
  return (
    <div id="top" className="min-h-screen bg-bg text-ink">
      <Nav />
      <main>
        <Hero />
        {/* more sections added in later tasks */}
      </main>
    </div>
  )
}
