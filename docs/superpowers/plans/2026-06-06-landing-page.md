# Budget Tracker Landing Page — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a modern, interactive single-page marketing site in `landing/` that showcases the Budget Tracker app and explains every feature (F1–F8), rendered in the app's navy design system.

**Architecture:** A standalone Vite + React + TypeScript SPA under `landing/` (independent of the Gradle/Android build). One component per page section, all reading feature copy from a single `src/data/features.ts`. Tailwind v4 holds the design tokens via CSS-first `@theme`. Framer Motion (`motion`) drives reveals and a center-activated scrollytelling phone. Real emulator screenshots live in `public/screens/`.

**Tech Stack:** Vite 5 · React 18 · TypeScript · Tailwind CSS v4 (`@tailwindcss/vite`) · `motion` (Framer Motion) · Vitest + React Testing Library.

**Source of truth:** `docs/superpowers/specs/2026-06-06-landing-page-design.md`. Cite section numbers (e.g. §3, §5) when in doubt.

**Working agreement (user request):** surface rendered screenshots at each visual checkpoint (Tasks 7, 9, 10, 14, 16) before moving on — see `[[visual-feedback-checkpoints]]`.

**Conventions:** Conventional Commits. All work on branch `feat/landing-page` (already created). Run commands from `landing/` unless noted. `JAVA_HOME` is only needed for the Android build, never here.

---

## File structure

```
landing/
  index.html                 # title/meta/OG/theme-color + font links
  package.json · package-lock.json
  vite.config.ts             # react + tailwind + vitest config
  tsconfig*.json
  README.md
  vitest.setup.ts            # jest-dom matchers
  public/
    favicon.svg
    screens/                 # log/categories/plan/report/recurring/export/settings/calculator .png
  src/
    main.tsx
    App.tsx                  # composes all sections
    index.css                # @import tailwindcss + @theme tokens + gradient/utility classes
    data/features.ts         # Feature[] (single source of truth)
    data/features.test.ts    # data-integrity test
    ui/
      SectionLabel.tsx       # uppercase mono eyebrow
      Badge.tsx              # F# / chip pill
      GradientButton.tsx     # pill CTA (filled gradient / ghost)
      PhoneFrame.tsx         # device bezel wrapping a screenshot
      CountUp.tsx            # animated tabular number
    components/
      Nav.tsx · Hero.tsx · ValueStrip.tsx
      FeatureScrolly.tsx · FeaturePanel.tsx
      PowerFeatures.tsx · CraftSection.tsx · PrivacySection.tsx · Faq.tsx · Footer.tsx
      Hero.test.tsx · FeatureScrolly.test.tsx · Faq.test.tsx
```

Each section component is self-contained and reads copy from `data/features.ts` or local constants; the only shared dependencies are the `ui/` primitives.

---

## Task 1: Scaffold the Vite + React + TS project

**Files:**
- Create: `landing/` (via scaffolder), then trim.
- Modify: root `.gitignore`.

- [ ] **Step 1: Scaffold**

Run from the repo root:
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
npm create vite@latest landing -- --template react-ts
cd landing
npm install
```

- [ ] **Step 2: Install dependencies (pinned majors)**

```bash
npm install motion
npm install -D tailwindcss@4 @tailwindcss/vite@4 vitest @testing-library/react @testing-library/jest-dom jsdom
```

- [ ] **Step 3: Remove the template's default styling we won't use**

Delete `src/App.css` and empty `src/index.css` (we rewrite `index.css` in Task 2). Delete `src/assets/react.svg` and `public/vite.svg`.
```bash
rm -f src/App.css src/assets/react.svg public/vite.svg
```

- [ ] **Step 4: Configure Vite (react + tailwind + vitest)**

Replace `landing/vite.config.ts` with:
```ts
/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './vitest.setup.ts',
  },
})
```

- [ ] **Step 5: Vitest setup file**

Create `landing/vitest.setup.ts`:
```ts
import '@testing-library/jest-dom'
```

- [ ] **Step 6: Add the test script**

In `landing/package.json`, add to `"scripts"`: `"test": "vitest run"` (keep the generated `dev`/`build`/`preview`). Leave `"type": "module"` as generated.

- [ ] **Step 7: Ignore build artifacts**

Append to the **root** `.gitignore`:
```
# Landing page (separate npm project)
landing/node_modules/
landing/dist/
```

- [ ] **Step 8: Verify dev server and build**

```bash
npm run build
```
Expected: `tsc -b && vite build` succeeds, `dist/` produced. (The page is still the Vite template — that's fine; we replace it next.)

- [ ] **Step 9: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing .gitignore
git commit -m "chore(landing): scaffold Vite + React + TS + Tailwind v4 project"
```

---

## Task 2: Design tokens, fonts, and global styles

**Files:**
- Modify: `landing/index.html`
- Create/replace: `landing/src/index.css`

- [ ] **Step 1: Fonts + meta in `index.html`**

Replace the `<head>` of `landing/index.html` with (keep `<body><div id="root"></div><script type="module" src="/src/main.tsx"></script></body>`):
```html
<head>
  <meta charset="UTF-8" />
  <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="theme-color" content="#0d2736" />
  <title>Budget Tracker — Plan every rupee. Privately.</title>
  <meta name="description" content="An offline-first personal budgeting app for Android. Plan monthly targets, track income and expenses, see plan-vs-actual reports, and export to Excel or PDF — all on your phone, no account." />
  <meta property="og:title" content="Budget Tracker — Plan every rupee. Privately." />
  <meta property="og:description" content="Offline-first personal budgeting for Android. Targets, reports, recurring entries, export — private by design." />
  <meta property="og:type" content="website" />
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet" />
</head>
```

- [ ] **Step 2: Tokens + base styles in `src/index.css`**

Replace `landing/src/index.css` entirely (tokens from spec §3):
```css
@import "tailwindcss";

@theme {
  --color-bg: #0A1218;
  --color-panel: #11191F;
  --color-panel2: #1A2229;
  --color-ink: #E0E7EC;
  --color-muted: #93A1AB;
  --color-accent: #9CC8DE;
  --color-brand: #0d2736;
  --color-brand2: #1B4561;
  --color-brand25: #143548;
  --color-income: #74D9B5;
  --color-overage: #FFB4A8;
  --color-amber: #FFB68D;
  --radius-card: 18px;
  --font-sans: 'Inter', ui-sans-serif, system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', ui-monospace, monospace;
}

@layer base {
  html { scroll-behavior: smooth; }
  body {
    margin: 0;
    background: var(--color-bg);
    color: var(--color-ink);
    font-family: var(--font-sans);
    -webkit-font-smoothing: antialiased;
  }
  ::selection { background: #1B4561; color: #E0E7EC; }
}

@layer utilities {
  .line-border { border: 1px solid rgba(255, 255, 255, 0.06); }
  .tnum { font-variant-numeric: tabular-nums; }
  .bg-brand-gradient { background-image: linear-gradient(135deg, #0d2736, #1B4561); }
  .bg-hero-glow { background-image: radial-gradient(120% 80% at 50% -10%, rgba(27, 69, 97, 0.45), transparent 60%); }
  .bg-accent-bar { background-image: linear-gradient(90deg, #143548, #9CC8DE); }
}

@media (prefers-reduced-motion: reduce) {
  html { scroll-behavior: auto; }
}
```

- [ ] **Step 3: Minimal favicon**

Create `landing/public/favicon.svg` (navy rounded square with a light "B"):
```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><rect width="64" height="64" rx="16" fill="#0d2736"/><text x="32" y="44" font-family="Inter, sans-serif" font-size="38" font-weight="700" fill="#9CC8DE" text-anchor="middle">B</text></svg>
```

- [ ] **Step 4: Verify build**
```bash
npm run build
```
Expected: succeeds. Token utilities (`bg-bg`, `text-muted`, `rounded-card`, `font-mono`) are now available.

- [ ] **Step 5: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): design tokens, fonts, and global styles"
```

---

## Task 3: Feature data (TDD)

**Files:**
- Create: `landing/src/data/features.ts`
- Test: `landing/src/data/features.test.ts`

- [ ] **Step 1: Write the failing data-integrity test**

Create `landing/src/data/features.test.ts`:
```ts
import { describe, it, expect } from 'vitest'
import { FEATURES, type Feature } from './features'

describe('FEATURES', () => {
  it('has 8 features with unique ids F1..F8', () => {
    expect(FEATURES).toHaveLength(8)
    const ids = FEATURES.map((f) => f.id)
    expect(new Set(ids).size).toBe(8)
    expect(ids).toEqual(['F1', 'F2', 'F3', 'F4', 'F5', 'F6', 'F7', 'F8'])
  })

  it('splits into 5 core + 3 power features', () => {
    expect(FEATURES.filter((f) => f.group === 'core')).toHaveLength(5)
    expect(FEATURES.filter((f) => f.group === 'power')).toHaveLength(3)
  })

  it('every feature has complete, well-formed content', () => {
    for (const f of FEATURES as Feature[]) {
      expect(f.tab.length).toBeGreaterThan(0)
      expect(f.title.length).toBeGreaterThan(0)
      expect(f.blurb.length).toBeGreaterThan(0)
      expect(f.bullets).toHaveLength(3)
      expect(f.bullets.every((b) => b.length > 0)).toBe(true)
      expect(f.screen).toMatch(/^\/screens\/.+\.png$/)
      expect(f.alt.length).toBeGreaterThan(0)
    }
  })
})
```

- [ ] **Step 2: Run it to verify it fails**
```bash
npm run test
```
Expected: FAIL — cannot resolve `./features`.

- [ ] **Step 3: Write `features.ts`** (copy from spec §5)

Create `landing/src/data/features.ts`:
```ts
export interface Feature {
  id: string
  group: 'core' | 'power'
  tab: string
  title: string
  blurb: string
  bullets: string[]
  screen: string
  alt: string
}

export const FEATURES: Feature[] = [
  {
    id: 'F1', group: 'core', tab: 'Log',
    title: 'Every transaction, in one tidy month view',
    blurb: 'Add income and expenses in seconds. A live Income / Expense / Net band keeps the month in focus, with each day grouped into its own card.',
    bullets: ['Net band updates instantly', 'Cross-month entries jump to the right month', 'Grouped, tabular, calculator-assisted amounts'],
    screen: '/screens/log.png',
    alt: 'Budget Tracker Log screen showing the Income, Expense and Net band above per-date transaction cards',
  },
  {
    id: 'F2', group: 'core', tab: 'Categories',
    title: 'Organize spending your way',
    blurb: 'Two levels — groups holding categories — each with a color and an income or expense kind. Archive without losing history, and drag to reorder.',
    bullets: ['Groups and categories with colors', 'Soft archive keeps past data', 'Drag to reorder'],
    screen: '/screens/categories.png',
    alt: 'Categories screen with colored groups, kind chips and category counts',
  },
  {
    id: 'F3', group: 'core', tab: 'Plan',
    title: 'Set targets, then watch reality',
    blurb: 'One target per category per month, saved in a single tap. Last month’s plan pre-fills the next so you tweak instead of retype.',
    bullets: ['Per-category monthly targets', 'Carry-forward pre-fill', 'Live target Net band'],
    screen: '/screens/plan.png',
    alt: 'Plan screen with per-category target inputs and a floating Save bar',
  },
  {
    id: 'F4', group: 'core', tab: 'Report',
    title: 'See plan vs actual at a glance',
    blurb: 'Per group and category, target versus actual with signed, color-coded deltas and totals — plus a plain-language narrative generated on-device, no AI.',
    bullets: ['Color-coded favorable / overage deltas', 'Deterministic narrative', 'Income / Expense / Net totals'],
    screen: '/screens/report.png',
    alt: 'Report screen with a summary narrative and per-group plan-versus-actual deltas',
  },
  {
    id: 'F5', group: 'core', tab: 'Recurring',
    title: 'Set repeating entries once',
    blurb: 'Monthly templates like Salary or Rent, applied with one idempotent tap. Toggle them active, and an applied-month pill confirms the run.',
    bullets: ['One-tap apply, once per month', 'Active and inactive states', 'Day-of-month scheduling'],
    screen: '/screens/recurring.png',
    alt: 'Recurring screen with template cards and applied-month pills',
  },
  {
    id: 'F6', group: 'power', tab: 'Export',
    title: 'Take your numbers anywhere',
    blurb: 'Export the month to a three-sheet Excel workbook or a PDF report and hand it off through the Android share sheet.',
    bullets: ['Three-sheet .xlsx', 'PDF report', 'Native share sheet'],
    screen: '/screens/export.png',
    alt: 'Export sheet offering Excel and PDF',
  },
  {
    id: 'F7', group: 'power', tab: 'Currency',
    title: 'Your currency, your theme',
    blurb: 'A single ISO-4217 currency — shown with its nation’s flag in the picker — reformats every amount retroactively. Pick light or dark and a comfortable or compact density.',
    bullets: ['Common codes plus any 3-letter code, with flags', 'Retroactive reformat', 'Light / Dark and density'],
    screen: '/screens/settings.png',
    alt: 'Settings screen with the flag-based currency picker',
  },
  {
    id: 'F8', group: 'power', tab: 'Calculator',
    title: 'Do the math in place',
    blurb: 'A calculator popover handles + − × ÷ and feeds the parsed value straight back into the amount field — to the exact minor unit.',
    bullets: ['Inline arithmetic', 'Grouped display', 'Parses to exact minor units'],
    screen: '/screens/calculator.png',
    alt: 'Calculator popover over an amount field',
  },
]

export const CORE_FEATURES = FEATURES.filter((f) => f.group === 'core')
export const POWER_FEATURES = FEATURES.filter((f) => f.group === 'power')
```

- [ ] **Step 4: Run the test to verify it passes**
```bash
npm run test
```
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): feature data model and content for F1-F8"
```

---

## Task 4: UI primitives

**Files:**
- Create: `landing/src/ui/SectionLabel.tsx`, `Badge.tsx`, `GradientButton.tsx`, `PhoneFrame.tsx`, `CountUp.tsx`

- [ ] **Step 1: `SectionLabel.tsx`** — uppercase mono eyebrow (design §3 label style)
```tsx
export function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <p className="font-mono text-[11px] uppercase tracking-[0.18em] text-muted">{children}</p>
  )
}
```

- [ ] **Step 2: `Badge.tsx`** — small pill for `F#` and chips
```tsx
export function Badge({ children, tone = 'default' }: { children: React.ReactNode; tone?: 'default' | 'mono' }) {
  const base = 'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[12px] line-border'
  const toneClass = tone === 'mono' ? 'font-mono text-accent bg-panel2' : 'text-muted bg-panel'
  return <span className={`${base} ${toneClass}`}>{children}</span>
}
```

- [ ] **Step 3: `GradientButton.tsx`** — pill CTA, filled gradient or ghost (design §3)
```tsx
import { motion } from 'motion/react'

interface Props {
  children: React.ReactNode
  href?: string
  variant?: 'filled' | 'ghost'
  onClick?: () => void
}

export function GradientButton({ children, href, variant = 'filled', onClick }: Props) {
  const base = 'inline-flex items-center justify-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold transition-colors'
  const cls =
    variant === 'filled'
      ? `${base} bg-brand-gradient text-white shadow-lg shadow-black/30`
      : `${base} line-border text-ink hover:bg-panel`
  const Comp: any = href ? motion.a : motion.button
  return (
    <Comp className={cls} href={href} onClick={onClick} whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}
      {...(href ? { target: '_blank', rel: 'noreferrer' } : {})}>
      {children}
    </Comp>
  )
}
```

- [ ] **Step 4: `PhoneFrame.tsx`** — device bezel around a screenshot
```tsx
export function PhoneFrame({ src, alt, className = '' }: { src: string; alt: string; className?: string }) {
  return (
    <div className={`relative mx-auto w-[260px] max-w-full ${className}`}>
      <div className="rounded-[2.2rem] bg-[#05090d] p-2.5 shadow-2xl shadow-black/50 ring-1 ring-white/10">
        <div className="overflow-hidden rounded-[1.7rem] bg-bg">
          <img src={src} alt={alt} className="block w-full" loading="lazy" />
        </div>
      </div>
    </div>
  )
}
```

- [ ] **Step 5: `CountUp.tsx`** — animates a number to its value on mount (Hero Net band, spec §7)
```tsx
import { useEffect } from 'react'
import { animate, useMotionValue, useTransform, useReducedMotion, motion } from 'motion/react'

export function CountUp({ to, prefix = '', className = '' }: { to: number; prefix?: string; className?: string }) {
  const reduce = useReducedMotion()
  const value = useMotionValue(reduce ? to : 0)
  const text = useTransform(value, (v) => `${prefix}${Math.round(v).toLocaleString('en-IN')}`)
  useEffect(() => {
    if (reduce) return
    const controls = animate(value, to, { duration: 1.1, ease: 'easeOut' })
    return controls.stop
  }, [to, reduce, value])
  return <motion.span className={`tnum ${className}`}>{text}</motion.span>
}
```

- [ ] **Step 6: Verify typecheck/build**
```bash
npm run build
```
Expected: succeeds (primitives compile; not yet used — that's fine, Vite tree-shakes).

- [ ] **Step 7: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): UI primitives (SectionLabel, Badge, GradientButton, PhoneFrame, CountUp)"
```

---

## Task 5: Capture app screenshots

**Files:**
- Create: `landing/public/screens/{log,categories,plan,report,recurring,export,settings,calculator}.png`

This task uses the running emulator (`emulator-5554`) via mobile-mcp. The app should already hold the seeded demo data.

- [ ] **Step 1: Confirm the emulator is up**

Verify a device is listed (mobile-mcp `mobile_list_available_devices`). If none, start one:
```bash
"$HOME/Library/Android/sdk/emulator/emulator" -avd Resizable_Experimental -no-snapshot-load &
```
and wait for boot.

- [ ] **Step 2: Capture each screen**

Launch the app, navigate to each destination, and save a screenshot to `landing/public/screens/<name>.png`:
- `log.png` — Log tab (a month with several transactions so the Net band + cards are populated).
- `categories.png` — Categories tab.
- `plan.png` — Plan tab.
- `report.png` — Report tab.
- `recurring.png` — Recurring tab.
- `export.png` — Report tab → open the export sheet (share icon).
- `settings.png` — Settings (gear) → ideally with the currency picker open showing flags; otherwise the Settings list.
- `calculator.png` — Log → add transaction → open the calculator popover.

Use mobile-mcp `mobile_save_screenshot` (or `mobile_take_screenshot` then write the bytes) targeting the exact paths above. Prefer the **dark** theme (the app's signature look, matches the page).

- [ ] **Step 3: Sanity-check the assets**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker/landing"
ls -lh public/screens
```
Expected: 8 PNGs, each non-empty and ≤ ~400 KB. If any exceeds ~400 KB, downscale with `sips -Z 1080 public/screens/<f>.png` (macOS).

- [ ] **Step 4: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing/public/screens
git commit -m "chore(landing): app screenshots for the feature sections"
```

---

## Task 6: App shell + Nav

**Files:**
- Create: `landing/src/components/Nav.tsx`
- Replace: `landing/src/App.tsx`, `landing/src/main.tsx`

- [ ] **Step 1: `Nav.tsx`** — sticky bar, blurs after scroll (spec §4.1)
```tsx
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
```

- [ ] **Step 2: `App.tsx`** — shell that we extend each task
```tsx
import { Nav } from './components/Nav'

export default function App() {
  return (
    <div id="top" className="min-h-screen bg-bg text-ink">
      <Nav />
      <main>
        {/* sections added in later tasks */}
      </main>
    </div>
  )
}
```

- [ ] **Step 3: Clean `main.tsx`**
```tsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

- [ ] **Step 4: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 5: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): app shell and sticky nav"
```

---

## Task 7: Hero  ·  visual checkpoint #1

**Files:**
- Create: `landing/src/components/Hero.tsx`
- Modify: `landing/src/App.tsx`

- [ ] **Step 1: `Hero.tsx`** (spec §4.2)
```tsx
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
          {/* Mini Net band that counts up, sitting above the phone */}
          <div className="mx-auto mb-5 w-[260px] max-w-full rounded-card bg-brand-gradient px-5 py-4 line-border">
            <div className="flex justify-between text-xs text-white/70">
              <span>Income</span><span>Expense</span><span>Net</span>
            </div>
            <div className="mt-1 flex justify-between text-lg font-semibold">
              <CountUp to={7400} prefix="₹" className="text-income" />
              <CountUp to={2000} prefix="₹" className="text-white" />
              <CountUp to={5400} prefix="₹" className="text-income" />
            </div>
          </div>
          <PhoneFrame src="/screens/log.png" alt="Budget Tracker Log screen" />
        </motion.div>
      </div>
    </section>
  )
}
```

- [ ] **Step 2: Mount it in `App.tsx`** — add `<Hero />` as the first child of `<main>` and import it.

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: VISUAL CHECKPOINT #1**

Run the dev server, screenshot the hero with the Playwright browser tool, and share it with the user:
```bash
npm run dev
```
Then: `browser_navigate` to `http://localhost:5173`, `browser_take_screenshot`, and `SendUserFile` the image (caption: "Hero + nav"). Pause for feedback before continuing.

- [ ] **Step 5: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): hero with animated Net band and phone"
```

---

## Task 8: Value strip

**Files:**
- Create: `landing/src/components/ValueStrip.tsx`
- Modify: `landing/src/App.tsx`

- [ ] **Step 1: `ValueStrip.tsx`** (spec §4.3) — reusable reveal via `whileInView`
```tsx
import { motion } from 'motion/react'

const VALUES = [
  { t: 'Private by design', d: 'Every transaction lives in a local database on your device. No accounts, no cloud.' },
  { t: 'Plan vs actual', d: 'Set monthly targets and see exactly where reality diverged, color-coded.' },
  { t: 'Recurring made simple', d: 'Salary, rent and bills as one-tap monthly templates.' },
  { t: 'Export anywhere', d: 'Hand off a month as a 3-sheet Excel workbook or a PDF report.' },
]

export function ValueStrip() {
  return (
    <section className="mx-auto max-w-6xl px-6 py-16">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {VALUES.map((v, i) => (
          <motion.div key={v.t} className="rounded-card bg-panel p-5 line-border"
            initial={{ opacity: 0, y: 16 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, margin: '-80px' }}
            transition={{ duration: 0.45, delay: i * 0.06 }}>
            <h3 className="font-semibold">{v.t}</h3>
            <p className="mt-2 text-sm text-muted">{v.d}</p>
          </motion.div>
        ))}
      </div>
    </section>
  )
}
```

- [ ] **Step 2: Mount `<ValueStrip />`** in `App.tsx` after `<Hero />`.

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): value-proposition strip"
```

---

## Task 9: Feature scrollytelling (F1–F5)  ·  visual checkpoint #2

**Files:**
- Create: `landing/src/components/FeaturePanel.tsx`, `landing/src/components/FeatureScrolly.tsx`
- Modify: `landing/src/App.tsx`

Mechanic (spec §4.4, §7): each panel reports when it crosses the viewport center (`onViewportEnter` with a centered margin) → sets the active index in the parent → the **sticky** phone (lg only) crossfades to that feature's screen. On mobile each panel renders its own inline phone, no pinning.

- [ ] **Step 1: `FeaturePanel.tsx`**
```tsx
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
      {/* Inline phone on mobile only */}
      <div className="mt-8 lg:hidden">
        <PhoneFrame src={feature.screen} alt={feature.alt} />
      </div>
    </motion.div>
  )
}
```

- [ ] **Step 2: `FeatureScrolly.tsx`**
```tsx
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
        {/* Sticky phone — desktop only */}
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
        {/* Panels */}
        <div>
          {CORE_FEATURES.map((f, i) => (
            <FeaturePanel key={f.id} feature={f} index={i} onActivate={setActive} />
          ))}
        </div>
      </div>
    </section>
  )
}
```

- [ ] **Step 3: Mount `<FeatureScrolly />`** in `App.tsx` after `<ValueStrip />`.

- [ ] **Step 4: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 5: VISUAL CHECKPOINT #2**

`npm run dev`; with the Playwright browser tool navigate to the page, scroll through the features section (`browser_evaluate` `window.scrollTo` or scroll actions), screenshot at two scroll positions showing the phone swapped to different screens, and `SendUserFile` both (caption: "Scrollytelling — phone swaps per feature"). Pause for feedback.

- [ ] **Step 6: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): scrollytelling feature walkthrough (F1-F5)"
```

---

## Task 10: Power features (F6–F8)  ·  visual checkpoint #3

**Files:**
- Create: `landing/src/components/PowerFeatures.tsx`
- Modify: `landing/src/App.tsx`

- [ ] **Step 1: `PowerFeatures.tsx`** (spec §4.5)
```tsx
import { motion } from 'motion/react'
import { POWER_FEATURES } from '../data/features'
import { SectionLabel } from '../ui/SectionLabel'
import { Badge } from '../ui/Badge'
import { PhoneFrame } from '../ui/PhoneFrame'

export function PowerFeatures() {
  return (
    <section className="mx-auto max-w-6xl px-6 py-20">
      <SectionLabel>And there’s more</SectionLabel>
      <h2 className="mt-3 max-w-xl text-3xl font-bold sm:text-4xl">Power features that round it out</h2>
      <div className="mt-12 grid gap-6 lg:grid-cols-3">
        {POWER_FEATURES.map((f, i) => (
          <motion.div key={f.id} className="rounded-card bg-panel p-6 line-border"
            initial={{ opacity: 0, y: 18 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, margin: '-60px' }}
            transition={{ duration: 0.5, delay: i * 0.08 }}>
            <Badge tone="mono">{f.id} · {f.tab}</Badge>
            <h3 className="mt-4 text-xl font-bold">{f.title}</h3>
            <p className="mt-2 text-sm text-muted">{f.blurb}</p>
            <ul className="mt-4 space-y-1.5">
              {f.bullets.map((b) => (
                <li key={b} className="flex items-start gap-2 text-sm text-muted">
                  <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-accent" />{b}
                </li>
              ))}
            </ul>
            <div className="mt-6 scale-90 origin-top">
              <PhoneFrame src={f.screen} alt={f.alt} />
            </div>
          </motion.div>
        ))}
      </div>
    </section>
  )
}
```

- [ ] **Step 2: Mount `<PowerFeatures />`** after `<FeatureScrolly />`.

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: VISUAL CHECKPOINT #3**

`npm run dev`; screenshot the power-features section with the Playwright tool and `SendUserFile` (caption: "Power features F6–F8"). Pause for feedback.

- [ ] **Step 5: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): power-feature cards (F6-F8)"
```

---

## Task 11: Craft section

**Files:**
- Create: `landing/src/components/CraftSection.tsx`
- Modify: `landing/src/App.tsx`

- [ ] **Step 1: `CraftSection.tsx`** (spec §4.6)
```tsx
import { SectionLabel } from '../ui/SectionLabel'

const SWATCHES = [
  { name: 'Brand', hex: '#0d2736' },
  { name: 'Brand 25', hex: '#143548' },
  { name: 'Brand 30', hex: '#1B4561' },
  { name: 'Accent', hex: '#9CC8DE' },
  { name: 'Income', hex: '#74D9B5' },
  { name: 'Overage', hex: '#FFB4A8' },
]

export function CraftSection() {
  return (
    <section className="mx-auto max-w-6xl px-6 py-20">
      <SectionLabel>Designed down to the pixel</SectionLabel>
      <h2 className="mt-3 max-w-xl text-3xl font-bold sm:text-4xl">One navy system, light and dark</h2>
      <div className="mt-12 grid gap-6 lg:grid-cols-3">
        <div className="rounded-card bg-panel p-6 line-border">
          <h3 className="font-semibold">Brand palette</h3>
          <div className="mt-4 grid grid-cols-3 gap-2">
            {SWATCHES.map((s) => (
              <div key={s.name} className="rounded-lg p-3 text-[10px] font-mono" style={{ background: s.hex, color: '#000a' }}>
                <div className="text-ink/0">{s.name}</div>{s.hex}
              </div>
            ))}
          </div>
        </div>
        <div className="rounded-card bg-panel p-6 line-border">
          <h3 className="font-semibold">Money in tabular numerals</h3>
          <p className="mt-2 text-sm text-muted">Color is never the only signal — every delta carries a sign.</p>
          <div className="mt-4 space-y-1 font-mono text-lg tnum">
            <div className="text-income">+₹7,400</div>
            <div className="text-overage">−₹2,000</div>
            <div className="text-ink">₹5,400</div>
          </div>
        </div>
        <div className="rounded-card bg-panel p-6 line-border">
          <h3 className="font-semibold">Lively chrome, calm data</h3>
          <p className="mt-2 text-sm text-muted">Navigation and CTAs animate with intent; numbers stay still and legible. Material 3 Expressive, anchored to brand navy.</p>
          <div className="mt-4 h-2 w-full rounded-full bg-accent-bar" />
        </div>
      </div>
    </section>
  )
}
```

- [ ] **Step 2: Mount `<CraftSection />`** after `<PowerFeatures />`.

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): design-craft section"
```

---

## Task 12: Privacy section

**Files:**
- Create: `landing/src/components/PrivacySection.tsx`
- Modify: `landing/src/App.tsx`

- [ ] **Step 1: `PrivacySection.tsx`** (spec §4.7)
```tsx
import { motion } from 'motion/react'
import { SectionLabel } from '../ui/SectionLabel'

const POINTS = [
  { t: 'No account, ever', d: 'There’s nothing to sign up for. Open the app and start.' },
  { t: 'No servers', d: 'Your data is stored in a local database (Room) and preferences on-device. Nothing leaves your phone.' },
  { t: 'No ads, no tracking', d: 'No analytics SDKs, no third-party trackers.' },
]

export function PrivacySection() {
  return (
    <section id="privacy" className="relative overflow-hidden bg-hero-glow py-20">
      <div className="mx-auto max-w-6xl px-6">
        <SectionLabel>Private by design</SectionLabel>
        <h2 className="mt-3 max-w-2xl text-3xl font-bold sm:text-4xl">Your money is nobody else’s business</h2>
        <div className="mt-12 grid gap-6 md:grid-cols-3">
          {POINTS.map((p, i) => (
            <motion.div key={p.t} className="rounded-card bg-panel/70 p-6 line-border backdrop-blur"
              initial={{ opacity: 0, y: 16 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, margin: '-60px' }}
              transition={{ duration: 0.45, delay: i * 0.07 }}>
              <h3 className="font-semibold">{p.t}</h3>
              <p className="mt-2 text-sm text-muted">{p.d}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
```

- [ ] **Step 2: Mount `<PrivacySection />`** after `<CraftSection />`.

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): privacy / offline section"
```

---

## Task 13: FAQ

**Files:**
- Create: `landing/src/components/Faq.tsx`
- Modify: `landing/src/App.tsx`

- [ ] **Step 1: `Faq.tsx`** — accessible accordion (spec §4.8, §9)
```tsx
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
```

- [ ] **Step 2: Mount `<Faq />`** after `<PrivacySection />`.

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): FAQ accordion"
```

---

## Task 14: Footer  ·  visual checkpoint #4 (full page)

**Files:**
- Create: `landing/src/components/Footer.tsx`
- Modify: `landing/src/App.tsx`

- [ ] **Step 1: `Footer.tsx`** (spec §4.9)
```tsx
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
```

- [ ] **Step 2: Mount `<Footer />`** in `App.tsx` (outside `<main>`, after it).

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: VISUAL CHECKPOINT #4**

`npm run dev`; with the Playwright tool capture a desktop full-page screenshot (`browser_take_screenshot` `fullPage: true`) AND a mobile-width capture (`browser_resize` to 390×844 then screenshot, to confirm the scrollytelling fell back to stacked blocks). `SendUserFile` both (caption: "Full page — desktop + mobile"). Pause for feedback.

- [ ] **Step 5: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "feat(landing): footer and final CTA"
```

---

## Task 15: SEO, README, and reduced-motion audit

**Files:**
- Create: `landing/README.md`
- Verify: reduced-motion across components

- [ ] **Step 1: `README.md`**
```markdown
# Budget Tracker — Landing Page

Marketing site for the Budget Tracker Android app. Vite + React + TypeScript + Tailwind v4 + Framer Motion.

## Develop
```bash
cd landing
npm install
npm run dev      # http://localhost:5173
npm run build    # type-checks + builds to dist/
npm run test     # Vitest
```

Screenshots live in `public/screens/` (captured from the app). Feature copy is in `src/data/features.ts`. Design tokens are in `src/index.css` (`@theme`), mirrored from the app's design system.

## Deploy (future)
The build is fully static (`dist/`). Host on GitHub Pages or Vercel; no server required.
```

- [ ] **Step 2: Reduced-motion audit**

Confirm `CountUp` already short-circuits on `useReducedMotion()` (Task 4). The other animations are opacity/short translate reveals, which are acceptable under reduced motion; the global CSS disables smooth scrolling (Task 2). No code change expected — just verify by reading. If any animation uses a large parallax/transform, gate it behind `useReducedMotion()`. (None currently do.)

- [ ] **Step 3: Verify build**
```bash
npm run build
```
Expected: succeeds.

- [ ] **Step 4: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "docs(landing): README and reduced-motion notes"
```

---

## Task 16: Smoke tests, final gate, and PR  ·  visual checkpoint #5

**Files:**
- Create: `landing/src/components/Hero.test.tsx`, `FeatureScrolly.test.tsx`, `Faq.test.tsx`

- [ ] **Step 1: Write the render smoke tests** (spec §10)

`Hero.test.tsx`:
```tsx
import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { Hero } from './Hero'

describe('Hero', () => {
  it('renders the headline and a primary CTA', () => {
    render(<Hero />)
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(/Plan every rupee/i)
    expect(screen.getByText(/Coming soon/i)).toBeInTheDocument()
  })
})
```

`FeatureScrolly.test.tsx`:
```tsx
import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { FeatureScrolly } from './FeatureScrolly'
import { CORE_FEATURES } from '../data/features'

describe('FeatureScrolly', () => {
  it('renders a panel for every core feature', () => {
    render(<FeatureScrolly />)
    for (const f of CORE_FEATURES) {
      expect(screen.getByText(f.title)).toBeInTheDocument()
    }
  })
})
```

`Faq.test.tsx`:
```tsx
import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { Faq } from './Faq'

describe('Faq', () => {
  it('toggles an answer open on click', () => {
    render(<Faq />)
    const second = screen.getByRole('button', { name: /work offline/i })
    expect(second).toHaveAttribute('aria-expanded', 'false')
    fireEvent.click(second)
    expect(second).toHaveAttribute('aria-expanded', 'true')
  })
})
```

- [ ] **Step 2: Run tests**
```bash
npm run test
```
Expected: PASS — data-integrity (Task 3) + 3 render tests. If a Framer Motion prop (`whileInView`/`onViewportEnter`) warns under jsdom, it is non-fatal; tests assert on text/roles, not animation.

- [ ] **Step 3: Final gate**
```bash
npm run build && npm run test
```
Expected: both succeed.

- [ ] **Step 4: VISUAL CHECKPOINT #5 (polish pass)**

`npm run dev`; do a final Playwright pass — desktop full-page + the features section mid-scroll + mobile width — and `SendUserFile` the set (caption: "Final polish"). Address any spacing/contrast nits the user calls out before opening the PR.

- [ ] **Step 5: Commit**
```bash
cd "/Users/kanishkdebnath/Developer/Budget Tracker"
git add landing
git commit -m "test(landing): render smoke tests for Hero, FeatureScrolly, Faq"
```

- [ ] **Step 6: Finish the branch**

Use **superpowers:finishing-a-development-branch**: verify `npm run test` + `npm run build` are green, then push `feat/landing-page` and open a PR to `main` summarizing the landing page (sections, stack, where it lives). The human reviews and merges.

---

## Self-review

**Spec coverage:**
- §1 scope → Tasks 1–16 (in scope); out-of-scope items (deploy automation, theme toggle) correctly omitted.
- §2 tech stack → Task 1 (Vite/React/TS/Tailwind v4/motion/Vitest).
- §3 tokens → Task 2 (`@theme` + gradient/utility classes, fonts, tabular-nums).
- §4 page structure → Nav (T6), Hero (T7), ValueStrip (T8), FeatureScrolly (T9), PowerFeatures (T10), CraftSection (T11), PrivacySection (T12), Faq (T13), Footer (T14).
- §5 feature content → Task 3 (`features.ts` + integrity test) with copy for F1–F8.
- §6 architecture/file structure → matches the File structure section and per-task files.
- §7 animation → CountUp (T4), reveals (T8/10/12), scrollytelling (T9), reduced-motion (T4/T15).
- §8 screenshots → Task 5.
- §9 accessibility → semantic sections, one h1 (Hero), `aria-expanded` FAQ (T13), alt text via `feature.alt` + PhoneFrame.
- §10 testing/verification → Task 3 (data) + Task 16 (render) + visual checkpoints in T7/9/10/14/16.
- §11 folder/build/deploy → Task 1 (.gitignore) + Task 15 (README).
- §12 assumptions → APP_NAME constant + GITHUB_URL + "Coming soon" CTA across Nav/Hero/Footer.

**Placeholder scan:** No TBD/TODO. Every code step contains complete code. Screenshot capture (Task 5) is an environment action, not code, and lists exact output paths.

**Type/name consistency:** `Feature` fields (`id/group/tab/title/blurb/bullets/screen/alt`) defined in Task 3 are used identically in FeaturePanel/PowerFeatures/FeatureScrolly. `CORE_FEATURES`/`POWER_FEATURES` exported in Task 3 and consumed in Tasks 9/10. `GradientButton` props (`children/href/variant/onClick`) consistent across Nav/Hero/Footer. `PhoneFrame` props (`src/alt/className`) consistent. `CountUp` props (`to/prefix/className`) consistent. Token class names (`bg-bg`, `text-muted`, `text-ink`, `text-income`, `text-overage`, `text-accent`, `rounded-card`, `font-mono`, `bg-brand-gradient`, `bg-hero-glow`, `bg-accent-bar`, `line-border`, `tnum`) defined in Task 2 and used consistently throughout.
