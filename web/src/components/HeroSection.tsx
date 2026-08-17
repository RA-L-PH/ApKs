import { useEffect, useState } from 'react'

export function HeroSection() {
  const [scrollY, setScrollY] = useState(0)
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    setVisible(true)
    const handleScroll = () => setScrollY(window.scrollY)
    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <section className="relative min-h-screen flex flex-col items-center justify-center px-6">
      {/* Subtle dot pattern */}
      <div className="absolute inset-0 opacity-[0.03]" style={{
        backgroundImage: `radial-gradient(circle, #fff 1px, transparent 1px)`,
        backgroundSize: '24px 24px'
      }} />

      <div className="relative z-10 text-center max-w-2xl mx-auto">
        {/* App icon */}
        <div
          className={`mb-10 mx-auto transition-all duration-1000 ${visible ? 'opacity-100 translate-y-0 scale-100' : 'opacity-0 translate-y-8 scale-95'}`}
          style={{ transform: `translateY(${scrollY * 0.05}px)` }}
        >
          <img src="/icon.png" alt="ApKs" className="w-48 h-48 mx-auto invert" />
        </div>

        {/* Title - bold and clear */}
        <h1 className={`text-6xl md:text-7xl font-bold text-white tracking-tight mb-4 transition-all duration-1000 delay-200 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'}`}>
          ApKs
        </h1>

        {/* Tagline - italic, visible */}
        <p className={`text-xl md:text-2xl text-white/70 italic mb-6 font-medium transition-all duration-1000 delay-300 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'}`}>
          Install apps freely. No gatekeepers.
        </p>

        {/* Description - slightly more visible */}
        <p className={`text-base text-white/50 mb-10 max-w-md mx-auto leading-relaxed transition-all duration-1000 delay-400 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'}`}>
          As Google tightens control over Android, ApKs keeps sideloading open. Browse your storage, tap install, done.
        </p>

        {/* CTA buttons */}
        <div className={`flex flex-col sm:flex-row gap-3 justify-center transition-all duration-1000 delay-500 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'}`}>
          <a
            href="#features"
            className="px-6 py-3 bg-white/10 hover:bg-white/15 rounded-full text-sm font-semibold text-white transition-colors"
          >
            See what it does
          </a>
          <a
            href="https://github.com/RA-L-PH/ApKs"
            target="_blank"
            rel="noopener noreferrer"
            className="px-6 py-3 border border-white/15 hover:border-white/25 hover:bg-white/5 rounded-full text-sm font-semibold text-white/70 hover:text-white transition-colors"
          >
            Source code
          </a>
        </div>
      </div>

      {/* Scroll hint */}
      <div
        className="absolute bottom-12 left-1/2 -translate-x-1/2 transition-opacity"
        style={{ opacity: Math.max(0, 1 - scrollY / 200) }}
      >
        <div className="w-5 h-8 border border-white/20 rounded-full flex justify-center pt-2">
          <div className="w-1 h-2 bg-white/50 rounded-full animate-bounce" />
        </div>
      </div>
    </section>
  )
}
