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
  const Comp = (href ? motion.a : motion.button) as typeof motion.a
  return (
    <Comp className={cls} href={href} onClick={onClick} whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.96 }}
      {...(href ? { target: '_blank', rel: 'noreferrer' } : {})}>
      {children}
    </Comp>
  )
}
