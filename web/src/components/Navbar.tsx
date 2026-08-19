import { useState, useEffect } from 'react'

export function Navbar() {
  const [scrolled, setScrolled] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <nav
      style={{ paddingTop: 'var(--banner-height, 0px)' }}
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled
          ? 'bg-[#0a0a0f]/80 backdrop-blur-xl border-b border-white/[0.06]'
          : 'bg-transparent'
      }`}
    >
      <div className="max-w-5xl mx-auto px-6 h-14 flex items-center justify-between">
        {/* Left: Logo + Name */}
        <a href="#" className="flex items-center gap-2.5 group" aria-label="ApKs Home">
          <img src="/icon.png" alt="ApKs Logo" className="w-14 h-14 invert" />
        </a>

        {/* Center: Links (desktop) */}
        <div className="hidden md:flex items-center gap-1">
          <a href="#features" className="px-3 py-1.5 text-[13px] font-medium text-white/50 hover:text-white/90 transition-colors rounded-lg hover:bg-white/[0.04]">
            Features
          </a>
          <a href="#how" className="px-3 py-1.5 text-[13px] font-medium text-white/50 hover:text-white/90 transition-colors rounded-lg hover:bg-white/[0.04]">
            How it works
          </a>
          <a href="#why" className="px-3 py-1.5 text-[13px] font-medium text-white/50 hover:text-white/90 transition-colors rounded-lg hover:bg-white/[0.04]">
            Why
          </a>
          <a href="#faq" className="px-3 py-1.5 text-[13px] font-medium text-white/50 hover:text-white/90 transition-colors rounded-lg hover:bg-white/[0.04]">
            FAQ
          </a>
          <a
            href="https://keepandroidopen.org"
            target="_blank"
            rel="noopener noreferrer"
            className="px-3 py-1.5 text-[13px] font-medium text-white/50 hover:text-white/90 transition-colors rounded-lg hover:bg-white/[0.04]"
          >
            Keep Android Open
          </a>
        </div>

        {/* Right: GitHub + Download */}
        <div className="flex items-center gap-2">
          <a
            href="https://github.com/RA-L-PH/ApKs"
            target="_blank"
            rel="noopener noreferrer"
            className="hidden sm:flex items-center gap-2 px-3 py-1.5 text-[13px] font-medium text-white/50 hover:text-white/90 transition-colors rounded-lg hover:bg-white/[0.04]"
            aria-label="ApKs GitHub Repository"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z" />
            </svg>
            GitHub
          </a>

          <a
            href="#download"
            className="flex items-center gap-1.5 px-4 py-1.5 bg-white/[0.08] hover:bg-white/[0.14] text-[13px] font-semibold text-white/90 rounded-lg transition-colors"
            aria-label="Download ApKs"
          >
            <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
            Download
          </a>

          {/* Mobile menu toggle */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="md:hidden p-1.5 text-white/50 hover:text-white/80 transition-colors"
          >
            <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              {mobileOpen ? (
                <>
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </>
              ) : (
                <>
                  <line x1="3" y1="6" x2="21" y2="6" />
                  <line x1="3" y1="12" x2="21" y2="12" />
                  <line x1="3" y1="18" x2="21" y2="18" />
                </>
              )}
            </svg>
          </button>
        </div>
      </div>

      {/* Mobile dropdown */}
      {mobileOpen && (
        <div className="md:hidden bg-[#0a0a0f]/95 backdrop-blur-xl border-t border-white/[0.04] px-6 py-4 space-y-1">
          <a href="#features" className="block py-2 text-sm text-white/60 hover:text-white/90 transition-colors" onClick={() => setMobileOpen(false)}>
            Features
          </a>
          <a href="#why" className="block py-2 text-sm text-white/60 hover:text-white/90 transition-colors" onClick={() => setMobileOpen(false)}>
            Why
          </a>
          <a href="#faq" className="block py-2 text-sm text-white/60 hover:text-white/90 transition-colors" onClick={() => setMobileOpen(false)}>
            FAQ
          </a>
          <a href="#download" className="block py-2 text-sm text-white/60 hover:text-white/90 transition-colors" onClick={() => setMobileOpen(false)}>
            Download
          </a>
          <a href="https://keepandroidopen.org" target="_blank" rel="noopener noreferrer" className="block py-2 text-sm text-white/60 hover:text-white/90 transition-colors">
            Keep Android Open
          </a>
          <a href="https://github.com/RA-L-PH/ApKs" target="_blank" rel="noopener noreferrer" className="block py-2 text-sm text-white/60 hover:text-white/90 transition-colors">
            GitHub
          </a>
        </div>
      )}
    </nav>
  )
}
