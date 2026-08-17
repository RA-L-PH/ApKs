import { useEffect, useRef, useState } from 'react'

export function WhySection() {
  const [isVisible, setIsVisible] = useState(false)
  const sectionRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) setIsVisible(true)
      },
      { threshold: 0.2 }
    )

    if (sectionRef.current) observer.observe(sectionRef.current)
    return () => observer.disconnect()
  }, [])

  return (
    <section id="why" ref={sectionRef} className="py-24 px-6">
      <div className="max-w-3xl mx-auto">
        {/* Section header */}
        <div className={`mb-12 transition-all duration-700 ${isVisible ? 'opacity-100' : 'opacity-0'}`}>
          <h2 className="text-xl font-medium text-white/90 mb-2">Why this exists</h2>
          <div className="w-8 h-px bg-white/20" />
        </div>

        {/* Main statement */}
        <div className={`mb-12 transition-all duration-700 delay-100 ${isVisible ? 'opacity-100' : 'opacity-0'}`}>
          <p className="text-base text-white/50 leading-relaxed mb-6">
            Google is closing Android. Starting late 2026, devices will restrict apps from unverified developers. Sideloading gets harder. Independent apps disappear.
          </p>
          <p className="text-base text-white/50 leading-relaxed">
            ApKs is a response. A tool that keeps the door open.
          </p>
        </div>

        {/* Impact points - simple, direct */}
        <div className={`space-y-4 mb-12 transition-all duration-700 delay-200 ${isVisible ? 'opacity-100' : 'opacity-0'}`}>
          <div className="flex gap-4 items-start">
            <div className="w-1.5 h-1.5 rounded-full bg-red-400/60 mt-2 flex-shrink-0" />
            <div>
              <p className="text-sm text-white/70 font-medium mb-1">Sideloading gets locked down</p>
              <p className="text-xs text-white/35">24-hour verification delays. Multi-step overrides. Designed to make you give up.</p>
            </div>
          </div>
          <div className="flex gap-4 items-start">
            <div className="w-1.5 h-1.5 rounded-full bg-red-400/60 mt-2 flex-shrink-0" />
            <div>
              <p className="text-sm text-white/70 font-medium mb-1">Developers must register with Google</p>
              <p className="text-xs text-white/35">Government ID. Real name. Signing keys handed over. Hobbyists and open-source projects hit hardest.</p>
            </div>
          </div>
          <div className="flex gap-4 items-start">
            <div className="w-1.5 h-1.5 rounded-full bg-red-400/60 mt-2 flex-shrink-0" />
            <div>
              <p className="text-sm text-white/70 font-medium mb-1">One company decides what runs on your phone</p>
              <p className="text-xs text-white/35">AOSP is open. But the Play Store layer isn't. And that's what most people use.</p>
            </div>
          </div>
        </div>

        {/* Links */}
        <div className={`flex flex-wrap gap-3 transition-all duration-700 delay-300 ${isVisible ? 'opacity-100' : 'opacity-0'}`}>
          <a
            href="https://keepandroidopen.org"
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 bg-white/[0.06] hover:bg-white/[0.1] rounded-full text-xs font-medium text-white/60 hover:text-white/80 transition-colors"
          >
            Keep Android Open
          </a>
          <a
            href="https://keepandroidopen.org/cta/"
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 bg-white/[0.06] hover:bg-white/[0.1] rounded-full text-xs font-medium text-white/60 hover:text-white/80 transition-colors"
          >
            Take action
          </a>
          <a
            href="https://linustechtips.com/topic/1636581-android-isnt-open-source-anymore/"
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-2 bg-white/[0.06] hover:bg-white/[0.1] rounded-full text-xs font-medium text-white/60 hover:text-white/80 transition-colors"
          >
            Read more
          </a>
        </div>
      </div>
    </section>
  )
}
