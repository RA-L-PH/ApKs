import { useEffect, useRef, useState } from 'react'

const steps = [
  {
    num: '01',
    title: 'Install the app',
    what: 'Download the APK file and open it. Your phone will ask if you want to install — tap Install.',
    tip: 'You may need to allow "Install from unknown apps" in your browser settings first.',
    icon: (
      <svg className="w-7 h-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
        <polyline points="7 10 12 15 17 10" />
        <line x1="12" y1="15" x2="12" y2="3" />
      </svg>
    ),
  },
  {
    num: '02',
    title: 'Grant permissions',
    what: 'When ApKs opens for the first time, it will ask for two permissions. Tap Allow on both:',
    bullets: ['Notifications — so you know when installs finish', 'Storage — so it can find your APK files'],
    icon: (
      <svg className="w-7 h-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
      </svg>
    ),
  },
  {
    num: '03',
    title: 'Turn on wireless debugging',
    what: 'Before the server can work, you need to enable one setting on your phone:',
    path: ['Open Settings', 'Tap "About phone"', 'Tap "Build number" 7 times (unlocks Developer options)', 'Go back to Settings → System → Developer options', 'Turn on "Wireless debugging"'],
    icon: (
      <svg className="w-7 h-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z" />
      </svg>
    ),
  },
  {
    num: '04',
    title: 'Start the server',
    what: 'Go to the Server tab in ApKs and tap the Start button. Wait a few seconds — you\'ll see a checkmark when it\'s ready.',
    tip: 'The server runs in the background. You don\'t need to keep ApKs open.',
    icon: (
      <svg className="w-7 h-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="5 3 19 12 5 21 5 3" />
      </svg>
    ),
  },
  {
    num: '05',
    title: 'Install your apps',
    what: 'Switch to the APKs tab. Tap "Grant Access" if prompted, then browse to any .apk or .apks file on your phone. Tap it, then tap Install. Done.',
    tip: 'You can also uninstall apps from the same list — just long-press any app.',
    icon: (
      <svg className="w-7 h-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
        <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
        <line x1="12" y1="22.08" x2="12" y2="12" />
      </svg>
    ),
  },
]

export function HowItWorksSection() {
  const [visibleSteps, setVisibleSteps] = useState<Set<number>>(new Set())
  const refs = useRef<(HTMLDivElement | null)[]>([])

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const idx = Number(entry.target.getAttribute('data-step'))
            setVisibleSteps((prev) => new Set([...prev, idx]))
          }
        })
      },
      { threshold: 0.15 }
    )

    refs.current.forEach((el) => {
      if (el) observer.observe(el)
    })

    return () => observer.disconnect()
  }, [])

  return (
    <section id="how" className="py-28 px-6">
      <div className="max-w-4xl mx-auto">
        {/* Section header */}
        <div className="text-center mb-24">
          <p className="text-[11px] font-semibold tracking-[0.2em] uppercase text-white/30 mb-4">How it works</p>
          <h2 className="text-3xl md:text-4xl font-bold text-white/90 tracking-tight mb-4">
            Get started in <span className="italic font-normal text-white/50">5 steps</span>
          </h2>
          <p className="text-sm text-white/35 max-w-md mx-auto">
            Follow these in order. The whole process takes about 2 minutes.
          </p>
        </div>

        {/* Steps */}
        <div className="space-y-4">
          {steps.map((step, i) => (
            <div
              key={step.num}
              ref={(el) => { refs.current[i] = el }}
              data-step={i}
              className={`relative rounded-2xl border border-white/[0.06] bg-white/[0.02] overflow-hidden transition-all duration-600 ${
                visibleSteps.has(i)
                  ? 'opacity-100 translate-y-0'
                  : 'opacity-0 translate-y-6'
              }`}
              style={{ transitionDelay: `${i * 100}ms` }}
            >
              <div className="flex gap-5 p-6 md:p-7">
                {/* Step number - large */}
                <div className="flex-shrink-0 flex flex-col items-center">
                  <div className="w-12 h-12 rounded-2xl bg-white/[0.06] border border-white/[0.08] flex items-center justify-center mb-2">
                    <span className="text-lg font-bold text-white/40">{step.num}</span>
                  </div>
                  {i < steps.length - 1 && (
                    <div className="w-px flex-1 min-h-[20px] bg-gradient-to-b from-white/[0.08] to-transparent mt-2" />
                  )}
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0 pt-0.5">
                  <div className="flex items-center gap-3 mb-3">
                    <span className="text-white/30">{step.icon}</span>
                    <h3 className="text-lg font-bold text-white/85">{step.title}</h3>
                  </div>

                  <p className="text-[15px] text-white/50 leading-relaxed mb-3">
                    {step.what}
                  </p>

                  {/* Bullets */}
                  {step.bullets && (
                    <ul className="space-y-1.5 mb-3">
                      {step.bullets.map((b, j) => (
                        <li key={j} className="flex items-start gap-2 text-sm text-white/40">
                          <span className="text-white/20 mt-1.5 flex-shrink-0">
                            <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="20 6 9 17 4 12" />
                            </svg>
                          </span>
                          {b}
                        </li>
                      ))}
                    </ul>
                  )}

                  {/* Path (settings steps) */}
                  {step.path && (
                    <div className="bg-white/[0.03] border border-white/[0.06] rounded-xl p-4 mb-3">
                      <ol className="space-y-2">
                        {step.path.map((p, j) => (
                          <li key={j} className="flex items-start gap-3 text-sm">
                            <span className="flex-shrink-0 w-5 h-5 rounded-md bg-white/[0.06] flex items-center justify-center text-[10px] font-bold text-white/35 mt-0.5">
                              {j + 1}
                            </span>
                            <span className="text-white/50 leading-relaxed">
                              {j === step.path.length - 1 ? (
                                <span className="text-white/70 font-medium">{p}</span>
                              ) : (
                                p
                              )}
                            </span>
                          </li>
                        ))}
                      </ol>
                    </div>
                  )}

                  {/* Tip */}
                  {step.tip && (
                    <div className="flex items-start gap-2 text-xs text-white/30 mt-2">
                      <svg className="w-3.5 h-3.5 flex-shrink-0 mt-0.5 text-white/20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                        <line x1="12" y1="16" x2="12" y2="12" />
                        <line x1="12" y1="8" x2="12.01" y2="8" />
                      </svg>
                      <span>{step.tip}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Bottom */}
        <div className="mt-16 text-center">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/[0.04] border border-white/[0.06]">
            <svg className="w-4 h-4 text-white/30" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <span className="text-sm text-white/40 font-medium">That's it. No accounts, no sign-ups, no tracking.</span>
          </div>
        </div>
      </div>
    </section>
  )
}
