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
