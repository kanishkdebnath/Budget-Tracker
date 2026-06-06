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
