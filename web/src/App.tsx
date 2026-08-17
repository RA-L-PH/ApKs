import { Navbar } from './components/Navbar'
import { HeroSection } from './components/HeroSection'
import { FeaturesSection } from './components/FeaturesSection'
import { HowItWorksSection } from './components/HowItWorksSection'
import { WhySection } from './components/WhySection'
import { KeepAndroidOpenBanner } from './components/KeepAndroidOpenBanner'
import { Footer } from './components/Footer'

export default function App() {
  return (
    <div className="min-h-screen bg-[#0a0a0f] text-white overflow-hidden">
      <Navbar />
      <KeepAndroidOpenBanner />
      <HeroSection />
      <FeaturesSection />
      <HowItWorksSection />
      <WhySection />
      <Footer />
    </div>
  )
}
