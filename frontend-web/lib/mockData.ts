export interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  mrp?: number;
  rating: number;
  reviews: number;
  emoji: string;
  description: string;
  inStock: boolean;
}

export const MOCK_PRODUCTS: Product[] = [
  { id: 1,  name: 'Fresh Milk 1L',        category: 'Dairy',      price: 58,  mrp: 65,  rating: 4.5, reviews: 320,  emoji: '🥛', description: 'Farm-fresh full-cream milk delivered daily.',          inStock: true  },
  { id: 2,  name: 'Whole Wheat Bread',     category: 'Bakery',     price: 45,  mrp: 50,  rating: 4.2, reviews: 210,  emoji: '🍞', description: 'Soft whole-wheat bread baked fresh every morning.',   inStock: true  },
  { id: 3,  name: 'Basmati Rice 5kg',      category: 'Grains',     price: 320, mrp: 360, rating: 4.7, reviews: 540,  emoji: '🍚', description: 'Long-grain aged basmati rice for perfect biryani.',  inStock: true  },
  { id: 4,  name: 'Sunflower Oil 1L',      category: 'Oils',       price: 130, mrp: 145, rating: 4.3, reviews: 180,  emoji: '🧴', description: 'Light refined sunflower oil, heart-healthy choice.', inStock: true  },
  { id: 5,  name: 'Tomatoes 500g',         category: 'Vegetables', price: 28,  mrp: 35,  rating: 4.1, reviews: 95,   emoji: '🍅', description: 'Farm-fresh red tomatoes sourced locally.',            inStock: true  },
  { id: 6,  name: 'Amul Butter 500g',      category: 'Dairy',      price: 245, mrp: 260, rating: 4.8, reviews: 620,  emoji: '🧈', description: 'Creamy pasteurised butter from Amul.',               inStock: true  },
  { id: 7,  name: 'Maggi Noodles 4pk',     category: 'Snacks',     price: 68,  mrp: 72,  rating: 4.6, reviews: 880,  emoji: '🍜', description: 'Classic masala noodles ready in 2 minutes.',         inStock: true  },
  { id: 8,  name: 'Tata Tea Gold 500g',    category: 'Beverages',  price: 215, mrp: 230, rating: 4.4, reviews: 440,  emoji: '🍵', description: 'Premium strong tea with natural Assam flavour.',     inStock: false },
  { id: 9,  name: 'Parle-G Biscuits 800g', category: 'Snacks',     price: 80,  mrp: 85,  rating: 4.9, reviews: 1200, emoji: '🍪', description: "India's favourite glucose biscuits.",               inStock: true  },
  { id: 10, name: 'Onions 1kg',            category: 'Vegetables', price: 35,  mrp: 42,  rating: 4.0, reviews: 70,   emoji: '🧅', description: 'Fresh red onions from Nashik.',                     inStock: true  },
  { id: 11, name: 'Colgate Toothpaste',    category: 'Personal',   price: 92,  mrp: 100, rating: 4.5, reviews: 310,  emoji: '🦷', description: 'Strong teeth protection with fluoride formula.',    inStock: true  },
  { id: 12, name: 'Surf Excel 1kg',        category: 'Household',  price: 170, mrp: 185, rating: 4.3, reviews: 260,  emoji: '🫧', description: 'Removes tough stains with less water.',             inStock: true  },
];

export const CATEGORIES = ['All', ...Array.from(new Set(MOCK_PRODUCTS.map(p => p.category)))];

export const MOCK_STORES = [
  { id: 1, name: 'Sharma Kirana Store', area: 'Ameerpet',   rating: 4.8, deliveryTime: '12 min', emoji: '🏪', categories: ['Dairy','Snacks','Grains'] },
  { id: 2, name: 'Fresh Mart',          area: 'Kukatpally', rating: 4.6, deliveryTime: '18 min', emoji: '🛒', categories: ['Vegetables','Fruits','Dairy'] },
  { id: 3, name: 'Daily Needs Express', area: 'HITEC City', rating: 4.7, deliveryTime: '10 min', emoji: '⚡', categories: ['All Categories'] },
  { id: 4, name: 'Green Basket',        area: 'Madhapur',   rating: 4.5, deliveryTime: '20 min', emoji: '🧺', categories: ['Vegetables','Organic'] },
];
