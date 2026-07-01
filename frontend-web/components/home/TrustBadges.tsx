const BADGES = [
  { icon: '⚡', label: '10-min delivery', sub: 'Express orders'      },
  { icon: '🛡️', label: '100% Safe',       sub: 'Secure payments'    },
  { icon: '🔄', label: 'Easy Returns',    sub: '24-hr return window' },
  { icon: '🌿', label: 'Fresh Products',  sub: 'Direct from farms'   },
];

export default function TrustBadges() {
  return (
    <section className="bg-white border-y border-gray-100 py-6 px-4">
      <div className="max-w-5xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-4">
        {BADGES.map(b => (
          <div key={b.label} className="flex items-center gap-3">
            <span className="text-2xl">{b.icon}</span>
            <div>
              <p className="text-sm font-bold text-gray-900">{b.label}</p>
              <p className="text-xs text-gray-400">{b.sub}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
