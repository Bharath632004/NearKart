import { CATEGORIES } from '@/lib/mockData';

interface Props {
  selected: string;
  onSelect: (cat: string) => void;
}

export default function ProductFilters({ selected, onSelect }: Props) {
  return (
    <div className="card p-4">
      <h3 className="font-bold text-gray-900 mb-3">Categories</h3>
      <ul className="space-y-1">
        {CATEGORIES.map(cat => (
          <li key={cat}>
            <button
              onClick={() => onSelect(cat)}
              className={`w-full text-left px-3 py-2 rounded-xl text-sm transition ${
                selected === cat
                  ? 'bg-primary-500 text-white font-semibold'
                  : 'text-gray-600 hover:bg-orange-50 hover:text-primary-600'
              }`}>
              {cat}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
