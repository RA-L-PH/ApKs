import { useState } from 'react'

const faqs = [
  {
    question: 'What is ApKs and how does it work?',
    answer: 'ApKs is a modern, open-source installer that scans your storage for APK and APKS files and installs them directly. It uses root access or wireless ADB (similar to Shizuku) to bypass restricted installer packages and standard Android sideloading limits.'
  },
  {
    question: 'Do I need root access to use ApKs?',
    answer: 'No, root is not required. You can use Wireless Debugging (wireless ADB) to pair and start the installer service. If you do have root access, ApKs can use it to install apps instantly without any manual pairing.'
  },
  {
    question: 'What makes ApKs different from other installers?',
    answer: 'ApKs is designed to be lightweight, secure, and future-proof. Unlike standard installers that might be blocked by future OS-level lockdowns, ApKs runs a local ADB service or utilizes Shizuku protocols to retain direct control over application package management.'
  },
  {
    question: 'How do I set up Wireless Debugging?',
    answer: 'Go to Settings -> Developer Options -> Enable Wireless Debugging. Under Wireless Debugging, select "Pair device with pairing code". Open the ApKs app, enter the pairing code and port displayed on your screen, and tap Pair to start the service.'
  },
  {
    question: 'Is ApKs free and open source?',
    answer: 'Yes, ApKs is 100% free, tracking-free, and open source (licensed under Apache 2.0). You can inspect the source code, build it yourself, or contribute directly on GitHub.'
  }
]

export function FaqSection() {
  const [openIndex, setOpenIndex] = useState<number | null>(null)

  return (
    <section id="faq" className="py-24 px-6 border-t border-white/[0.02]">
      <div className="max-w-3xl mx-auto">
        {/* Section header */}
        <div className="mb-12">
          <h2 className="text-xl font-medium text-white/90 mb-2">FAQ</h2>
          <div className="w-8 h-px bg-white/20" />
        </div>

        {/* Accordions */}
        <div className="space-y-3">
          {faqs.map((faq, index) => {
            const isOpen = openIndex === index
            return (
              <div
                key={index}
                className="rounded-2xl bg-white/[0.02] border border-white/[0.04] hover:bg-white/[0.03] hover:border-white/[0.06] transition-all duration-300 overflow-hidden"
              >
                <button
                  onClick={() => setOpenIndex(isOpen ? null : index)}
                  className="w-full flex items-center justify-between p-5 text-left text-sm font-medium text-white/80 hover:text-white transition-colors"
                >
                  <span>{faq.question}</span>
                  <svg
                    className={`w-4 h-4 text-white/40 transition-transform duration-300 ${isOpen ? 'rotate-180 text-white/70' : ''}`}
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth="2.5"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                <div
                  className={`grid transition-all duration-300 ease-out ${
                    isOpen ? 'grid-rows-[1fr] opacity-100 pb-5 px-5' : 'grid-rows-[0fr] opacity-0'
                  }`}
                >
                  <div className="overflow-hidden">
                    <p className="text-xs text-white/35 leading-relaxed pt-1 pr-6">
                      {faq.answer}
                    </p>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </section>
  )
}
