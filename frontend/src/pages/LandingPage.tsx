import { FeatureGrid } from '../components/FeatureGrid';
import { Footer } from '../components/Footer';
import { Hero } from '../components/Hero';
import { HowItWorks } from '../components/HowItWorks';

export function LandingPage() {
  return (
    <div>
      <Hero />
      <FeatureGrid />
      <HowItWorks />
      <Footer />
    </div>
  );
}
