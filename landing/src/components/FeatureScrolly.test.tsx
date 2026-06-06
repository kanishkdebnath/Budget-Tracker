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
