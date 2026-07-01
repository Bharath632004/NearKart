const STEPS = [
  { icon: '📍', title: 'Share your location', desc: 'We find the nearest stores to you instantly.' },
  { icon: '🛒', title: 'Pick your items',      desc: 'Browse and add items from multiple stores.'  },
  { icon: '💳', title: 'Pay securely',          desc: 'UPI, card or cash on delivery — your choice.' },
  { icon: '🛵', title: 'Fast delivery',         desc: 'Your order arrives in 10–30 minutes.'         },
];

export default function HowItWorks() {
  return (
    <section className="bg-orange-50 py-14 px-4">
      <div className="max-w-5xl mx-auto text-center">
        <h2 className="section-title">How It Works</h2>
        <p className="section-subtitle">Simple steps to get your groceries delivered</p>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mt-4">
          {STEPS.map((s, i) => (
            <div key={i} className="card p-6 text-center">
              <div className="text-4xl mb-3">{s.icon}</div>
              <div className="w-6 h-6 bg-primary-500 text-white text-xs font-bold rounded-full flex items-center justify-center mx-auto mb-2">{i + 1}</div>
              <h3 className="font-bold text-sm text-gray-900 mb-1">{s.title}</h3>
              <p className="text-xs text-gray-500">{s.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
