import { SiFdroid } from 'react-icons/si'

export function DownloadSection() {
  return (
    <section id="download" className="py-24 px-6 border-t border-white/[0.02] bg-[#0c0c12]/30">
      <div className="max-w-4xl mx-auto">
        {/* Section header */}
        <div className="text-center mb-16">
          <p className="text-[11px] font-semibold tracking-[0.2em] uppercase text-white/30 mb-4">Get ApKs</p>
          <h2 className="text-3xl md:text-4xl font-bold text-white/90 tracking-tight mb-4">
            Download & Installation
          </h2>
          <p className="text-sm text-white/35 max-w-md mx-auto">
            Choose your preferred installation method. ApKs is open source and completely free.
          </p>
        </div>

        {/* Download Options Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Card 1: Stable Release Downloads (Spans 2 columns on large screens) */}
          <div className="lg:col-span-2 group p-6 sm:p-8 rounded-2xl bg-white/[0.02] border border-white/[0.04] hover:border-white/[0.08] transition-all duration-300 flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-3.5 mb-6">
                <div className="w-10 h-10 rounded-xl bg-white/[0.04] flex items-center justify-center text-white/60">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.5">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                </div>
                <div>
                  <a
                    href="https://github.com/RA-L-PH/ApKs/releases/tag/latest"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:underline"
                  >
                    <h3 className="text-sm font-semibold text-white/90">Stable Release (v13.6.0)</h3>
                  </a>
                  <p className="text-[10px] text-emerald-400/80 font-mono mt-0.5">latest stable release from GitHub</p>
                </div>
              </div>

              {/* Universal APK block */}
              <div className="mb-6 p-4 rounded-xl bg-white/[0.02] border border-white/[0.03]">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <h4 className="text-xs font-semibold text-white/80">Universal APK</h4>
                    <p className="text-[11px] text-white/35 mt-1 leading-normal">
                      Works on all Android devices. Recommended for simple, one-tap installation.
                    </p>
                  </div>
                  <a
                    href="https://github.com/RA-L-PH/ApKs/releases/download/latest/ApKs-v13.6.0.r1093.f635161c-release.apk"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex-shrink-0 py-2 px-4 bg-white/10 hover:bg-white/15 text-[11px] font-bold text-white/90 rounded-lg transition-colors border border-white/[0.04] text-center"
                  >
                    Download Universal
                  </a>
                </div>
              </div>

              {/* Split APKs block */}
              <div className="p-4 rounded-xl bg-white/[0.02] border border-white/[0.03]">
                <h4 className="text-xs font-semibold text-white/80 mb-1.5">Split APKs (Architecture-Specific)</h4>
                <p className="text-[11px] text-white/35 mb-4 leading-normal">
                  Optimized builds with smaller download sizes. Requires an installer like ApKs to deploy splits.
                </p>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                  <a
                    href="https://github.com/RA-L-PH/ApKs/releases/download/latest/ApKs-v13.6.0.r1093.f635161c-arm64-v8a-release.apk"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="py-2 px-2 bg-white/[0.04] hover:bg-white/[0.08] text-[10px] font-bold text-white/70 hover:text-white rounded-lg transition-colors border border-white/[0.02] text-center"
                  >
                    arm64-v8a
                  </a>
                  <a
                    href="https://github.com/RA-L-PH/ApKs/releases/download/latest/ApKs-v13.6.0.r1093.f635161c-armeabi-v7a-release.apk"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="py-2 px-2 bg-white/[0.04] hover:bg-white/[0.08] text-[10px] font-bold text-white/70 hover:text-white rounded-lg transition-colors border border-white/[0.02] text-center"
                  >
                    armeabi-v7a
                  </a>
                  <a
                    href="https://github.com/RA-L-PH/ApKs/releases/download/latest/ApKs-v13.6.0.r1093.f635161c-x86-release.apk"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="py-2 px-2 bg-white/[0.04] hover:bg-white/[0.08] text-[10px] font-bold text-white/70 hover:text-white rounded-lg transition-colors border border-white/[0.02] text-center"
                  >
                    x86
                  </a>
                  <a
                    href="https://github.com/RA-L-PH/ApKs/releases/download/latest/ApKs-v13.6.0.r1093.f635161c-x86_64-release.apk"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="py-2 px-2 bg-white/[0.04] hover:bg-white/[0.08] text-[10px] font-bold text-white/70 hover:text-white rounded-lg transition-colors border border-white/[0.02] text-center"
                  >
                    x86_64
                  </a>
                </div>
              </div>
            </div>
          </div>

          {/* Card 2: F-Droid status */}
          <div className="group p-6 rounded-2xl bg-white/[0.01] border border-white/[0.03] flex flex-col justify-between">
            <div>
              <div className="w-10 h-10 rounded-xl bg-white/[0.03] flex items-center justify-center text-white/40 mb-4">
                <SiFdroid className="w-5.5 h-5.5" />
              </div>
              <div className="flex items-center gap-2 mb-2">
                <h3 className="text-sm font-semibold text-white/60">F-Droid</h3>
                <span className="px-1.5 py-0.5 rounded text-[8px] font-mono font-medium bg-amber-500/10 text-amber-400 border border-amber-500/10 uppercase">
                  Pending
                </span>
              </div>
              <p className="text-xs text-white/25 leading-relaxed">
                ApKs is not yet uploaded on F-Droid. We are currently preparing the submission files. In the meantime, please download stable packages from GitHub Releases.
              </p>
            </div>
            <button
              disabled
              className="w-full py-2.5 px-4 bg-white/[0.02] text-xs font-semibold text-white/30 rounded-xl border border-white/[0.02] cursor-not-allowed text-center"
            >
              Not yet available
            </button>
          </div>

        </div>
      </div>
    </section>
  )
}
