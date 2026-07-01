import React, { useEffect, useState } from "react";
import StatCard from "../../components/StatCard";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import API from "../../api/axios";

const mockStats = { products: 24, ordersToday: 12, revenue: "₹8,450", pending: 3 };
const mockWeekly = [
  { day: "Mon", orders: 8 }, { day: "Tue", orders: 12 }, { day: "Wed", orders: 6 },
  { day: "Thu", orders: 15 }, { day: "Fri", orders: 20 }, { day: "Sat", orders: 25 }, { day: "Sun", orders: 18 },
];

export default function MerchantDashboard() {
  const [stats, setStats] = useState(mockStats);
  const [weekly] = useState(mockWeekly);
  const [recentOrders, setRecentOrders] = useState([
    { _id: "o1", customer: "Ravi Kumar", amount: 350, status: "pending", time: "10:30 AM" },
    { _id: "o2", customer: "Priya Sharma", amount: 120, status: "delivered", time: "09:15 AM" },
    { _id: "o3", customer: "Suresh Babu", amount: 540, status: "confirmed", time: "08:50 AM" },
  ]);

  useEffect(() => {
    API.get("/merchant/dashboard").then(res => {
      setStats(res.data.stats || mockStats);
      setRecentOrders(res.data.recentOrders || []);
    }).catch(() => {});
  }, []);

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Merchant Dashboard</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard title="My Products" value={stats.products} icon="🛒" color="indigo" />
        <StatCard title="Orders Today" value={stats.ordersToday} icon="📦" color="yellow" />
        <StatCard title="Today's Revenue" value={stats.revenue} icon="💰" color="green" />
        <StatCard title="Pending Orders" value={stats.pending} icon="⏳" color="red" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Weekly Orders</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={weekly}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="day" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="orders" fill="#4F46E5" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Recent Orders</h3>
          <div className="space-y-3">
            {recentOrders.map(o => (
              <div key={o._id} className="flex items-center justify-between border-b pb-2">
                <div>
                  <p className="font-medium text-sm">{o.customer}</p>
                  <p className="text-xs text-gray-400">{o.time}</p>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-sm">₹{o.amount}</p>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${
                    o.status === "delivered" ? "bg-green-100 text-green-700" :
                    o.status === "pending" ? "bg-yellow-100 text-yellow-700" :
                    "bg-blue-100 text-blue-700"
                  }`}>{o.status}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
