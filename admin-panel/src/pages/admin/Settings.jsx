import React, { useState } from "react";

export default function Settings() {
  const [settings, setSettings] = useState({
    platformName: "NearKart",
    supportEmail: "support@nearkart.com",
    deliveryFee: 20,
    maintenanceMode: false,
  });

  const handleSave = (e) => {
    e.preventDefault();
    alert("Settings saved successfully!");
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Settings & Audit Logs</h2>
      <div className="bg-white rounded-xl shadow p-6 max-w-xl">
        <form onSubmit={handleSave} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Platform Name</label>
            <input className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-indigo-500" value={settings.platformName} onChange={e => setSettings({...settings, platformName: e.target.value})} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Support Email</label>
            <input type="email" className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-indigo-500" value={settings.supportEmail} onChange={e => setSettings({...settings, supportEmail: e.target.value})} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Default Delivery Fee (₹)</label>
            <input type="number" className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-indigo-500" value={settings.deliveryFee} onChange={e => setSettings({...settings, deliveryFee: +e.target.value})} />
          </div>
          <div className="flex items-center gap-3">
            <input type="checkbox" id="maintenance" checked={settings.maintenanceMode} onChange={e => setSettings({...settings, maintenanceMode: e.target.checked})} className="w-4 h-4 accent-indigo-600" />
            <label htmlFor="maintenance" className="text-sm text-gray-700">Enable Maintenance Mode</label>
          </div>
          <button type="submit" className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg font-semibold">Save Settings</button>
        </form>
      </div>
    </div>
  );
}
