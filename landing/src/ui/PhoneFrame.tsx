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
