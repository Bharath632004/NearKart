import React, { useState, useEffect } from "react";
import API from "../../api/axios";

const mockNotifications = [
  { _id: "n1", title: "New Merchant Registration", message: "FreshVeggies Store has registered and awaits approval.", type: "merchant", read: false, createdAt: "2026-07-02T06:30:00Z" },
  { _id: "n2", title: "High Order Volume Alert", message: "Order volume exceeded 500 orders in the last hour.", type: "alert", read: false, createdAt: "2026-07-02T05:15:00Z" },
  { _id: "n3", title: "Payment Gateway Issue", message: "Stripe integration reported 3 failed transactions.", type: "warning", read: true, createdAt: "2026-07-01T22:00:00Z" },
  { _id: "n4", title: "New User Milestone", message: "NearKart has reached 10,000 registered users!", type: "success", read: true, createdAt: "2026-07-01T18:45:00Z" },
  { _id: "n5", title: "Delivery Partner Complaint", message: "User Amit Kumar filed a complaint against delivery partner #DP045.", type: "complaint", read: false, createdAt: "2026-07-01T14:20:00Z" },
  { _id: "n6", title: "System Backup Completed", message: "Daily database backup completed successfully.", type: "system", read: true, createdAt: "2026-07-01T02:00:00Z" },
];

const TYPE_STYLES = {
  merchant: { bg: "bg-blue-50", border: "border-blue-200", icon: "🏪", badge: "bg-blue-100 text-blue-700" },
  alert: { bg: "bg-red-50", border: "border-red-200", icon: "🚨", badge: "bg-red-100 text-red-700" },
  warning: { bg: "bg-yellow-50", border: "border-yellow-200", icon: "⚠️", badge: "bg-yellow-100 text-yellow-700" },
  success: { bg: "bg-green-50", border: "border-green-200", icon: "✅", badge: "bg-green-100 text-green-700" },
  complaint: { bg: "bg-orange-50", border: "border-orange-200", icon: "📋", badge: "bg-orange-100 text-orange-700" },
  system: { bg: "bg-gray-50", border: "border-gray-200", icon: "⚙️", badge: "bg-gray-100 text-gray-700" },
};

export default function Notifications() {
  const [notifications, setNotifications] = useState(mockNotifications);
  const [filter, setFilter] = useState("all");
  const [showSendForm, setShowSendForm] = useState(false);
  const [form, setForm] = useState({ title: "", message: "", target: "all", type: "system" });
  const [sending, setSending] = useState(false);
  const [toast, setToast] = useState("");

  useEffect(() => {
    API.get("/admin/notifications")
      .then(res => setNotifications(res.data))
      .catch(() => {});
  }, []);

  const markRead = (id) => {
    setNotifications(prev => prev.map(n => n._id === id ? { ...n, read: true } : n));
    API.put(`/admin/notifications/${id}/read`).catch(() => {});
  };

  const markAllRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
    API.put("/admin/notifications/read-all").catch(() => {});
  };

  const deleteNotification = (id) => {
    setNotifications(prev => prev.filter(n => n._id !== id));
  };

  const sendNotification = async (e) => {
    e.preventDefault();
    if (!form.title.trim() || !form.message.trim()) return;
    setSending(true);
    try {
      await API.post("/admin/notifications/send", form);
    } catch {}
    setToast("✅ Notification sent successfully!");
    setForm({ title: "", message: "", target: "all", type: "system" });
    setShowSendForm(false);
    setSending(false);
    setTimeout(() => setToast(""), 3000);
  };

  const filtered = notifications.filter(n => {
    if (filter === "unread") return !n.read;
    if (filter === "read") return n.read;
    if (filter !== "all") return n.type === filter;
    return true;
  });

  const unreadCount = notifications.filter(n => !n.read).length;

  const timeAgo = (iso) => {
    const diff = Date.now() - new Date(iso).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
  };

  return (
    <div>
      {toast && (
        <div className="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-xl shadow-lg z-50 text-sm font-medium">
          {toast}
        </div>
      )}

      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Notifications</h2>
          {unreadCount > 0 && <p className="text-sm text-gray-500">{unreadCount} unread notification{unreadCount > 1 ? "s" : ""}</p>}
        </div>
        <div className="flex gap-3">
          {unreadCount > 0 && (
            <button onClick={markAllRead}
              className="text-sm text-indigo-600 border border-indigo-600 px-4 py-2 rounded-lg hover:bg-indigo-50">
              Mark All Read
            </button>
          )}
          <button onClick={() => setShowSendForm(true)}
            className="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 text-sm font-semibold">
            + Send Notification
          </button>
        </div>
      </div>

      {/* Send Notification Modal */}
      {showSendForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-gray-800 mb-4">Send Notification</h3>
            <form onSubmit={sendNotification} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Title *</label>
                <input type="text" required value={form.title}
                  onChange={e => setForm({ ...form, title: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                  placeholder="Notification title" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Message *</label>
                <textarea required rows={3} value={form.message}
                  onChange={e => setForm({ ...form, message: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                  placeholder="Notification message" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Target</label>
                  <select value={form.target} onChange={e => setForm({ ...form, target: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2">
                    <option value="all">All Users</option>
                    <option value="merchants">Merchants</option>
                    <option value="delivery">Delivery Partners</option>
                    <option value="customers">Customers</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
                  <select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2">
                    {Object.keys(TYPE_STYLES).map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowSendForm(false)}
                  className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-50">
                  Cancel
                </button>
                <button type="submit" disabled={sending}
                  className="flex-1 bg-indigo-600 text-white py-2 rounded-lg hover:bg-indigo-700 font-semibold disabled:opacity-50">
                  {sending ? "Sending..." : "Send"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex flex-wrap gap-2 mb-6">
        {["all", "unread", "read", "alert", "warning", "merchant", "system"].map(f => (
          <button key={f}
            onClick={() => setFilter(f)}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              filter === f ? "bg-indigo-600 text-white" : "bg-white text-gray-600 border border-gray-200 hover:bg-indigo-50"
            }`}>
            {f.charAt(0).toUpperCase() + f.slice(1)}
            {f === "unread" && unreadCount > 0 && (
              <span className="ml-1.5 bg-red-500 text-white text-xs rounded-full px-1.5">{unreadCount}</span>
            )}
          </button>
        ))}
      </div>

      {/* Notifications List */}
      <div className="space-y-3">
        {filtered.length === 0 && (
          <div className="bg-white rounded-xl shadow p-12 text-center text-gray-400">
            <p className="text-4xl mb-3">🔔</p>
            <p>No notifications found.</p>
          </div>
        )}
        {filtered.map(n => {
          const style = TYPE_STYLES[n.type] || TYPE_STYLES.system;
          return (
            <div key={n._id}
              className={`rounded-xl border p-4 flex items-start gap-4 transition-all ${
                !n.read ? `${style.bg} ${style.border}` : "bg-white border-gray-200"
              }`}>
              <span className="text-2xl">{style.icon}</span>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-semibold text-gray-800 text-sm">{n.title}</span>
                  {!n.read && <span className="w-2 h-2 bg-blue-500 rounded-full"></span>}
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${style.badge}`}>{n.type}</span>
                </div>
                <p className="text-sm text-gray-600">{n.message}</p>
                <p className="text-xs text-gray-400 mt-1">{timeAgo(n.createdAt)}</p>
              </div>
              <div className="flex gap-2">
                {!n.read && (
                  <button onClick={() => markRead(n._id)}
                    className="text-xs text-indigo-600 hover:underline">Mark Read</button>
                )}
                <button onClick={() => deleteNotification(n._id)}
                  className="text-xs text-red-500 hover:underline">Delete</button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
