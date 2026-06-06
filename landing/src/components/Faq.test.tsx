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
