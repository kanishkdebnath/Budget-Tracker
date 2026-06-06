# Budget Tracker — Landing Page

Marketing site for the Budget Tracker Android app. Vite + React + TypeScript + Tailwind v4 + Framer Motion.

## Develop

```bash
cd landing
npm install
npm run dev      # http://localhost:5173
npm run build    # type-checks (tsc) + builds to dist/
npm run test     # Vitest
```

Screenshots live in `public/screens/` (captured from the running app). Feature copy is the single source of truth in `src/data/features.ts`. Design tokens are in `src/index.css` (`@theme`), mirrored from the app's navy design system.

## Structure

- `src/components/` — one component per page section (Nav, Hero, ValueStrip, FeatureScrolly, PowerFeatures, CraftSection, PrivacySection, Faq, Footer).
- `src/ui/` — shared primitives (PhoneFrame, GradientButton, Badge, SectionLabel, CountUp).
- `src/data/features.ts` — F1–F8 content consumed by the scrollytelling and power cards.

## Deploy (future)

The build is fully static (`dist/`). Host on GitHub Pages or Vercel; no server required.
