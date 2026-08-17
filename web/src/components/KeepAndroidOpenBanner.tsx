import { useEffect, useRef } from 'react'

export function KeepAndroidOpenBanner() {
  const bannerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    // Load the Keep Android Open banner script
    const script = document.createElement('script')
    script.src = 'https://keepandroidopen.org/banner.js'
    script.async = true
    document.body.appendChild(script)

    return () => {
      document.body.removeChild(script)
    }
  }, [])

  return <div ref={bannerRef} id="keep-android-open-banner" />
}
