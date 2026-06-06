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
