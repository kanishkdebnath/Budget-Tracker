# Budget Tracker — Marketing Landing Page Design

**Status:** Approved (brainstorm 2026-06-06)
**Goal:** A modern, interactive single-page marketing site that showcases the Budget Tracker Android app and explains every feature (F1–F8) in depth, rendered in the app's own navy design language.

This spec is the source of truth for the landing page. It is a **separate sub-project** living in `landing/` at the repo root; it does not touch the Gradle/Android build.

---

## 1. Scope

**In scope (v1):**

- A one-page React site (Vite + React + TypeScript + Tailwind) under `landing/`.
- Sticky nav, hero, value strip, scrollytelling feature walkthrough (F1–F5), power-feature cards (F6–F8), a design-craft strip, a privacy/offline section, FAQ, and a CTA/footer.
- Real screenshots captured from the running emulator, placed in CSS phone frames.
- Framer Motion scroll/reveal animations; `prefers-reduced-motion` honored.
- `npm run dev` / `npm run build` deliverable; Vitest smoke tests + `tsc` typecheck as the quality gate.

**Out of scope (v1):**

- Deployment automation (GitHub Pages/Vercel workflow) — easy follow-up, noted in §11.
- A page-level light/dark theme toggle — the page is fixed dark-navy in v1 (future enhancement).
- A backend, analytics, forms, or i18n.
- Recreating app screens in CSS (we use real screenshots).

---

## 2. Tech stack

- **Vite + React 18 + TypeScript** — lightweight SPA, fast HMR, static build output.
- **Tailwind CSS** — theme extended with the exact design tokens (§3) so utilities map to brand colors.
- **Framer Motion** (`motion/react`) — reveals + scroll-linked scrollytelling.
- **Vitest + React Testing Library** — smoke tests.
- Node 18+; package manager: npm (lockfile committed).

No SSR. SEO handled with static `index.html` `<title>`/`<meta>` (description, Open Graph, theme-color `#0d2736`) — adequate for a single marketing page.

---

## 3. Design tokens (locked to the app's design system)

Mirror these into `tailwind.config.ts` `theme.extend` and a small `:root` in `index.css`. Values come from `docs/design-system/foundations.html` and `CLAUDE.md`.

**Dark surfaces (page is dark-first):**

| Token | Hex | Use |
|---|---|---|
| `bg` | `#0A1218` | page background |
| `panel` | `#11191F` | cards/panels |
| `panel-2` | `#1A2229` | nested tiles |
| `line` | `rgba(255,255,255,0.06)` | hairline borders |
| `text` | `#E0E7EC` | body text |
| `muted` | `#93A1AB` | secondary text |
| `accent` | `#9CC8DE` | brand-light accent |

**Brand & semantic:**

| Token | Hex | Use |
|---|---|---|
| `brand` | `#0d2736` | primary navy identity |
| brand ramp | `#001220 · #0d2736 · #143548 · #1B4561` | gradients |
| `income` | `#74D9B5` | positive money (green) |
| `overage` | `#FFB4A8` | negative money (red) |
| `amber` | `#FFB68D` | tertiary accent (plan/recurring) |

**Gradients (copy 1:1):**

- CTA / hero device glow / pills: `linear-gradient(135deg, #0d2736, #1B4561)`.
- Hero background: a radial brand top-glow over `bg` (mirrors `BudgetBackground`) — `radial-gradient(120% 80% at 50% -10%, rgba(27,69,97,0.45), transparent 60%)`.
- Accent bar / progress: `linear-gradient(90deg, #143548, #9CC8DE)`.

**Type:**

- **Inter** (400/500/600/700) for all text; **JetBrains Mono** for `F#` badges, spec chips, and numeric accents. Load via Google Fonts `<link>` (or `@fontsource`).
- **Tabular numerals** (`font-variant-numeric: tabular-nums`) anywhere money/numbers appear — matches the app invariant.

**Shape/spacing:** 18px card radius, 999px pills, 16/24/32px rhythm. Faint 1px borders on panels (`line`).

---

## 4. Page structure

Top → bottom. Each section is its own component.

1. **Nav (`Nav.tsx`)** — sticky top bar: wordmark "Budget Tracker" + a navy brand dot; anchor links (Features · Privacy · FAQ); a pill **"Get the app"** CTA. Transparent at top; on scroll past hero it gains a `panel`/blur background + bottom hairline.

2. **Hero (`Hero.tsx`)** — over the radial glow:
   - H1 headline (proposed: **"Plan every rupee. Privately."**), subhead (1–2 sentences: offline-first personal budgeting that lives entirely on your phone).
   - CTA pair: primary gradient pill **"Coming soon — Google Play"** (styled badge, non-navigating) + ghost **"View source"** → GitHub.
   - Trust chips row: `Offline-first` · `On-device` · `No account` · `Multi-currency`.
   - A floating **`PhoneFrame`** showing the **Log** screenshot; the Net band numbers count up on mount (Framer Motion).

3. **Value strip (`ValueStrip.tsx`)** — 4 compact cards: *Private by design* · *Plan vs actual* · *Recurring made simple* · *Export to Excel/PDF*. Reveal on scroll (staggered).

4. **Feature scrollytelling (`FeatureScrolly.tsx`)** — the core. A tall section; on desktop a **`PhoneFrame` pins** (sticky) on the left while feature panels (`FeaturePanel.tsx`) scroll on the right. `useScroll` over the section drives an active index; the phone screenshot crossfades/slides to the active feature's screen. Covers **F1 Log · F2 Categories · F3 Plan · F4 Report · F5 Recurring**. On mobile (`< lg`) it degrades to stacked `[phone, text]` blocks (no pinning). Reduced-motion: no slide, instant screen swap, panels just fade.

   Each panel: `F#` mono badge, title, 2–3 sentence detail, 3 spec bullets (§5).

5. **Power features (`PowerFeatures.tsx`)** — a 3-card detailed row for **F6 Export · F7 Currency & Settings · F8 Calculator**, each with its own screenshot, title, blurb, and bullets.

6. **Craft section (`CraftSection.tsx`)** — "Designed down to the pixel": a compact strip showing the navy palette swatches, a light/dark pairing note, tabular-numeral money sample (`+₹7,400` green / `−₹2,000` red), and the motion line ("lively chrome, calm data"). Ties the page to the design knowledge.

7. **Privacy section (`PrivacySection.tsx`)** — emphasizes all-data-on-device: no servers, no accounts, no ads; a Room/DataStore-on-device note in plain language.

8. **FAQ (`Faq.tsx`)** — 4–5 accordion Q&As (Is my data private? Does it work offline? Which currencies? Can I export? Is it free/open-source?).

9. **Footer (`Footer.tsx`)** — final CTA echo + GitHub link + small print.

---

## 5. Feature content (F1–F8)

Single source of truth: `src/data/features.ts` — an array of `Feature` objects consumed by the scrolly + cards. Copy is grounded in `PRODUCT_SPEC.md`.

```ts
export interface Feature {
  id: 'F1'|'F2'|'F3'|'F4'|'F5'|'F6'|'F7'|'F8'
  tab: string            // short nav-style name, e.g. "Log"
  title: string          // headline, e.g. "Every rupee, in one tidy month view"
  blurb: string          // 2–3 sentences
  bullets: string[]      // 3 concrete capabilities
  screen: string         // /screens/<file>.png
}
```

Content direction (final copy written during implementation, but anchored here so it isn't a placeholder):

- **F1 Log** — *"Every transaction, in one tidy month view."* Add income/expense in seconds; a live Income / Expense / Net band tracks the month; per-date cards. Bullets: `Net band updates instantly` · `Cross-month entries jump to the right month` · `Grouped, tabular, calculator-assisted amounts`.
- **F2 Categories** — *"Organize spending your way."* Two-level groups → categories, each with a color and income/expense kind; archive without losing history; drag to reorder. Bullets: `Groups + categories with colors` · `Soft archive keeps past data` · `Drag to reorder`.
- **F3 Plan** — *"Set targets, then watch reality."* One target per category per month; bulk save; last month's plan pre-fills the next. Bullets: `Per-category monthly targets` · `Carry-forward pre-fill` · `Live target Net band`.
- **F4 Report** — *"See plan vs actual at a glance."* Per-group/category target-vs-actual with signed, color-coded deltas, totals, and a plain-language narrative — no AI. Bullets: `Color-coded deltas (favorable/overage)` · `Deterministic narrative` · `Income / Expense / Net totals`.
- **F5 Recurring** — *"Set repeating entries once."* Monthly templates (Salary, Rent…) applied with one idempotent tap; active toggle; applied-month pill. Bullets: `One-tap apply, once per month` · `Active/inactive states` · `Day-of-month scheduling`.
- **F6 Export** — *"Take your numbers anywhere."* Export the month to a 3-sheet Excel workbook or a PDF report and share via the Android share sheet. Bullets: `3-sheet .xlsx` · `PDF report` · `Native share sheet`.
- **F7 Currency & Settings** — *"Your currency, your theme."* A single ISO-4217 currency (with nation flags in the picker) that retroactively reformats everything; light/dark + density. Bullets: `Common + any 3-letter code, with flags` · `Retroactive reformat` · `Light/Dark + density`.
- **F8 Calculator** — *"Do the math in place."* A calculator popover (`+ − × ÷`) feeds the parsed value straight into the amount field. Bullets: `Inline arithmetic` · `Grouped display` · `Parses to exact minor units`.

---

## 6. Component architecture & file structure

```
landing/
  index.html                 # title/meta/OG/theme-color, font links
  package.json
  vite.config.ts
  tailwind.config.ts         # tokens in theme.extend
  tsconfig.json
  README.md                  # how to run/build
  public/
    screens/                 # F1..F8 emulator screenshots (.png)
    favicon.svg
  src/
    main.tsx
    App.tsx                  # composes the sections
    index.css                # Tailwind layers + :root tokens + font-face
    data/features.ts         # Feature[] (single source of truth)
    lib/useReducedMotion.ts  # wrapper, used by animations
    components/
      Nav.tsx
      Hero.tsx
      ValueStrip.tsx
      FeatureScrolly.tsx
      FeaturePanel.tsx
      PowerFeatures.tsx
      CraftSection.tsx
      PrivacySection.tsx
      Faq.tsx
      Footer.tsx
    ui/
      PhoneFrame.tsx         # device bezel wrapping a screenshot
      GradientButton.tsx     # pill CTA (filled gradient / ghost)
      SectionLabel.tsx       # uppercase mono section eyebrow
      Badge.tsx              # F# / chip
  __tests__/                 # or *.test.tsx colocated
```

Each component owns one section and reads its copy from `data/features.ts` or local constants — no cross-component coupling beyond shared `ui/` primitives.

---

## 7. Animation

- **Reveals:** `motion.div` with `whileInView` (fade + 16px rise, staggered) for section blocks and cards.
- **Scrollytelling:** in `FeatureScrolly`, `useScroll({ target: sectionRef })` → `scrollYProgress`; map progress to an active feature index; the pinned `PhoneFrame` crossfades the screenshot (`AnimatePresence`) and the matching panel highlights. Phone is `position: sticky; top: ~12vh` on `lg+`.
- **Hero Net band:** numbers animate from 0 to value on mount (`useMotionValue` + `animate`), tabular-nums so they don't jitter.
- **Micro:** CTA press/hover scale (matches the app's FAB press feel), chip hover lift.
- **Reduced motion:** `useReducedMotion()` disables transforms/slides; content still fades in and screens swap instantly. No parallax.

---

## 8. Screenshots / assets

Capture from the running emulator (dark theme, the app's signature look) into `landing/public/screens/`:

`log.png` (F1, also hero) · `categories.png` (F2) · `plan.png` (F3) · `report.png` (F4) · `recurring.png` (F5) · `export.png` (F6, the export sheet) · `settings.png` (F7, incl. the flag currency picker) · `calculator.png` (F8, popover over Log).

Use a clean dataset (the seeded demo data already on the emulator is fine). Crop to the device content; the `PhoneFrame` adds the bezel. Keep PNGs reasonably sized (≤ ~400 KB each).

---

## 9. Accessibility

- Semantic landmarks (`header`/`main`/`section`/`footer`), one `h1`, logical heading order.
- Color is never the only signal (mirrors the app): deltas/chips carry text + sign, not just hue.
- All interactive elements keyboard-focusable with visible focus rings; FAQ accordion is button-driven with `aria-expanded`.
- Screenshots have descriptive `alt`. Contrast meets WCAG AA on the navy surfaces.
- `prefers-reduced-motion` respected (§7).

---

## 10. Testing & verification

- **Gate:** `tsc --noEmit` typecheck + `vite build` must succeed.
- **Vitest smoke tests:**
  - `features.ts` integrity: 8 features, ids F1–F8 unique, each has non-empty title/blurb, exactly 3 bullets, and a `screen` path.
  - Rendering: `Hero` renders the headline; `FeatureScrolly` renders all 8/relevant feature titles; `Faq` toggles an item open.
- **Visual checkpoints (working agreement):** at each milestone (scaffold+hero, scrollytelling, power features, craft+privacy+FAQ+footer, polish) run the dev server, screenshot with the Playwright browser tool, and share the renders so the user can eyeball progress before moving on.

---

## 11. Folder, build, deploy

- Lives in `landing/`; independent npm project. Add `landing/node_modules/` and `landing/dist/` to the repo `.gitignore`.
- `landing/README.md` documents `npm install`, `npm run dev`, `npm run build`, `npm run test`.
- **Deploy (future):** static `dist/` → GitHub Pages (Actions workflow) or Vercel. Not built in v1; a one-paragraph note in the README explains the path.

---

## 12. Assumptions (confirmed in brainstorm)

- **Name:** displayed as "Budget Tracker", parameterized via a single constant so a later rename (e.g. Lakshya) is one edit.
- **CTA:** "Coming soon — Google Play" styled badge (no public listing yet) + a real GitHub link to `https://github.com/kanishkdebnath/Budget-Tracker`.
- **Theme:** page is fixed dark-navy in v1; a light/dark toggle is a future enhancement.
- **Screenshots:** dark-theme captures from the emulator.

---

## 13. Future / deferred

- GitHub Pages/Vercel deploy workflow.
- Page light/dark toggle that also swaps the phone screenshots between the app's themes.
- Localized copy; richer "interactive demo" (clickable simulated app) instead of screenshots.
