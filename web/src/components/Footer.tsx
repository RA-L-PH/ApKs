export function Footer() {
  return (
    <footer className="py-16 px-6">
      <div className="max-w-3xl mx-auto">
        <div className="h-px bg-white/[0.04] mb-12" />

        <div className="flex flex-col md:flex-row justify-between items-start gap-8">
          {/* Left */}
          <div>
            <div className="flex items-center gap-3 mb-3">
              <img src="/icon.png" alt="" className="w-14 h-14 invert" />
            </div>
            <p className="text-xs text-white/25">
              Apache 2.0 · Based on Shizuku
            </p>
          </div>

          {/* Right */}
          <div className="flex gap-6">
            <a
              href="https://github.com/RA-L-PH/ApKs"
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-white/30 hover:text-white/50 transition-colors"
            >
              GitHub
            </a>
            <a
              href="https://ra-l-ph.pages.dev"
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-white/30 hover:text-white/50 transition-colors"
            >
              Portfolio
            </a>
            <a
              href="https://keepandroidopen.org"
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-white/30 hover:text-white/50 transition-colors"
            >
              Keep Android Open
            </a>
          </div>
        </div>
      </div>
    </footer>
  )
}
