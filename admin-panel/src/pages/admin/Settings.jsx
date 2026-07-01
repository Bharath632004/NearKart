import React, { useState } from "react";
import API from "../../api/axios";

export default function Settings() {
  const [platform, setPlatform] = useState({ siteName: "NearKart", supportEmail: "support@nearkart.in", deliveryFee: 30, commissionRate: 10 });
  const [password, setPassword] = useState({ current: "", newPass: "", confirm: "" });
  const [msg, setMsg] = useState({ platform: "", password: "" });

  const savePlatform = async (e) => {
    e.preventDefault();
    try { await API.put("/admin/settings/platform", platform); } catch {}
    setMsg(m => ({ ...m, platform: "✅ Platform settings saved!" }));
    setTimeout(() => setMsg(m => ({ ...m, platform: "" })), 3000);
  };

  const changePassword = async (e) => {
    e.preventDefault();
    if (password.newPass !== password.confirm) {
      setMsg(m => ({ ...m, password: "❌ Passwords don't match!" }));
      return;
    }
    try { await API.put("/admin/settings/password", { current: password.current, newPassword: password.newPass }); } catch {}
    setMsg(m => ({ ...m, password: "✅ Password updated!" }));
    setPassword({ current: "", newPass: "", confirm: "" });
    setTimeout(() => setMsg(m => ({ ...m, password: "" })), 3000);
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Settings</h2>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Platform Settings</h3>
          {msg.platform && <p className="text-sm mb-3 text-green-600">{msg.platform}</p>}
          <form onSubmit={savePlatform} className="space-y-4">
            {[
              { label: "Site Name", key: "siteName", type: "text" },
              { label: "Support Email", key: "supportEmail", type: "email" },
              { label: "Delivery Fee (₹)", key: "deliveryFee", type: "number" },
              { label: "Commission Rate (%)", key: "commissionRate", type: "number" },
            ].map(({ label, key, type }) => (
              <div key={key}>
                <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
                <input type={type}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-indigo-500"
                  value={platform[key]} onChange={e => setPlatform({ ...platform, [key]: e.target.value })} />
              </div>
            ))}
            <button type="submit" className="w-full bg-indigo-600 text-white py-2 rounded-lg hover:bg-indigo-700 font-semibold">
              Save Platform Settings
            </button>
          </form>
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Change Password</h3>
          {msg.password && <p className={`text-sm mb-3 ${msg.password.includes("❌") ? "text-red-600" : "text-green-600"}`}>{msg.password}</p>}
          <form onSubmit={changePassword} className="space-y-4">
            {[
              { label: "Current Password", key: "current" },
              { label: "New Password", key: "newPass" },
              { label: "Confirm New Password", key: "confirm" },
            ].map(({ label, key }) => (
              <div key={key}>
                <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
                <input type="password" required
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-indigo-500"
                  value={password[key]} onChange={e => setPassword({ ...password, [key]: e.target.value })} />
              </div>
            ))}
            <button type="submit" className="w-full bg-red-500 text-white py-2 rounded-lg hover:bg-red-600 font-semibold">
              Update Password
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
