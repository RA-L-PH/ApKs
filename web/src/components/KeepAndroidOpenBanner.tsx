import { useState, useEffect } from 'react'

export function KeepAndroidOpenBanner() {
  const [isVisible, setIsVisible] = useState(false) // For slide animation trigger
  const [isClosed, setIsClosed] = useState(false)   // When user clicks 'X'
  const [timeLeft, setTimeLeft] = useState({
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0,
  })

  // Target date: January 1, 2027 (00:00:00 UTC)
  const targetDate = new Date('2027-01-01T00:00:00Z').getTime()

  useEffect(() => {
    // Slide in after a brief delay
    const slideInTimer = setTimeout(() => {
      setIsVisible(true)
    }, 100)

    // Update countdown every second
    const updateCountdown = () => {
      const now = new Date().getTime()
      const difference = targetDate - now

      if (difference <= 0) {
        setTimeLeft({ days: 0, hours: 0, minutes: 0, seconds: 0 })
        return
      }

      const days = Math.floor(difference / (1000 * 60 * 60 * 24))
      const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((difference % (1000 * 60)) / 1000)

      setTimeLeft({ days, hours, minutes, seconds })
    }

    updateCountdown()
    const interval = setInterval(updateCountdown, 1000)

    return () => {
      clearTimeout(slideInTimer)
      clearInterval(interval)
    }
  }, [targetDate])

  // Update document root variable for banner height to push fixed layout elements down
  useEffect(() => {
    const root = document.documentElement
    if (isVisible && !isClosed) {
      // Banner height is 44px (h-11) on mobile, 56px (sm:h-14) on desktop.
      const height = window.innerWidth < 640 ? '44px' : '56px'
      root.style.setProperty('--banner-height', height)

      // Handle window resize
      const handleResize = () => {
        const h = window.innerWidth < 640 ? '44px' : '56px'
        root.style.setProperty('--banner-height', h)
      }
      window.addEventListener('resize', handleResize)
      return () => window.removeEventListener('resize', handleResize)
    } else {
      root.style.setProperty('--banner-height', '0px')
    }
  }, [isVisible, isClosed])

  if (isClosed) return null

  return (
    <div
      style={{
        transform: isVisible ? 'translateY(0)' : 'translateY(-100%)',
        transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
      }}
      className="fixed top-0 left-0 right-0 z-[100] h-11 sm:h-14 bg-[#0a0a0f]/80 backdrop-blur-md border-b border-red-500/20 flex items-center justify-center px-8 shadow-[0_4px_30px_rgba(0,0,0,0.5)] text-center select-none"
    >
      {/* Tiny pulsing red indicator dot to draw attention */}
      <span className="absolute left-4 sm:left-6 flex h-2 w-2">
        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
        <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
      </span>

      <a
        href="https://keepandroidopen.org"
        target="_blank"
        rel="noopener noreferrer"
        className="flex flex-col sm:flex-row items-center justify-center gap-0.5 sm:gap-2.5 text-[10px] sm:text-xs font-semibold tracking-widest text-white/70 hover:text-white transition-colors uppercase leading-none"
      >
        <span>Android will become a locked-down platform in</span>
        <span className="font-mono text-[#ff4a4a] font-bold drop-shadow-[0_0_6px_rgba(255,74,74,0.4)]">
          {timeLeft.days}D {timeLeft.hours}H {timeLeft.minutes}M {timeLeft.seconds}S
        </span>
      </a>

      <button
        onClick={() => {
          setIsVisible(false)
          setTimeout(() => setIsClosed(true), 400)
        }}
        className="absolute right-3 p-1.5 text-white/40 hover:text-white/90 hover:bg-white/[0.04] rounded-lg transition-all"
        aria-label="Close banner"
      >
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  )
}
