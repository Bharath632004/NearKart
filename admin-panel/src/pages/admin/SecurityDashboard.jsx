import React, { useState, useEffect } from "react";
import API from "../../api/axios";

const mockSessions = [
  { _id: "s1", user: "admin@nearkart.in", ip: "192.168.1.10", browser: "Chrome 126 / Windows", location: "Vijayawada, IN", lastActive: "2026-07-02T07:30:00Z", current: true },
  { _id: "s2", user: "admin@nearkart.in", ip: "103.45.67.89", browser: "Firefox 127 / Android", location: "Hyderabad, IN", lastActive: "2026-07-02T04:00:00Z", current: false },
  { _id: "s3", user: "moderator@nearkart.in", ip: "49.205.32.18", browser: "Safari / iPhone", location: "Chennai, IN", lastActive: "2026-07-01T22:45:00Z", current: false },
];

const mockFailedLogins = [
  { _id: "fl1", email: "admin@nearkart.in", ip: "45.82.14.99", attempts: 5, timestamp: "2026-07-02T03:15:00Z", blocked: true },
  { _id: "fl2", email: "admin@nearkart.in", ip: "103.21.44.11", attempts: 3, timestamp: "2026-07-01T21:30:00Z", blocked: false },
  { _id: "fl3", email: "unknown@test.com", ip: "78.45.12.33", attempts: 10, timestamp: "2026-07-01T18:00:00Z", blocked: true },
];

const mockSecurityEvents = [
  { id: "e1", event: "Password Changed", user: "admin@nearkart.in", timestamp: "2026-06-30T10:00:00Z", severity: "low" },
  { id: "e2", event: "Multiple Failed Login Attempts", user: "Unknown", timestamp: "2026-07-02T03:15:00Z", severity: "high" },
  { id: "e3", event: "New Admin Session", user: "admin@nearkart.in", timestamp: "2026-07-02T07:30:00Z", severity: "info" },
  { id: "e4", event: "Role Permission Modified", user: "admin@nearkart.in", timestamp: "2026-07-01T14:00:00Z", severity: "medium" },
  { id: "e5", event: "Suspicious IP Access", user: "Unknown", timestamp: "2026-07-01T18:00:00Z", severity: "high" },
];

const SEV_COLORS = {
  high: "bg-red-100 text-red-700 border-red-200",
  medium: "bg-yellow-100 text-yellow-700 border-yellow-200",
  low: "bg-green-100 text-green-700 border-green-200",
  info: "bg-blue-100 text-blue-700 border-blue-200",
};

const SEV_ICONS = { high: "🔴", medium: "🟡", low: "🟢", info: "🔵" };

export default function SecurityDashboard() {
  const [sessions, setSessions] = useState(mockSessions);
  const [failedLogins] = useState(mockFailedLogins);
  const [events] = useState(mockSecurityEvents);
  const [toast, setToast] = useState("");

  useEffect(() => {
    API.get("/admin/security/sessions").then(res => setSessions(res.data)).catch(() => {});
  }, []);

  const revokeSession = async (id) => {
    try { await API.delete(`/admin/security/sessions/${id}`); } catch {}
    setSessions(prev => prev.filter(s => s._id !== id));
    setToast("✅ Session revoked.");
    setTimeout(() => setToast(""), 3000);
  };

  const fmt = (iso) => new Date(iso).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "short" });

  const highSeverity = events.filter(e => e.severity === "high").length;
  const activeSessions = sessions.length;
  const blockedIPs = failedLogins.filter(f => f.blocked).length;

  return (
    <div>
      {toast && <div className="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-xl shadow-lg z-50 text-sm">{toast}</div>}

      <h2 className="text-2xl font-bold text-gray-800 mb-6">Security Dashboard</h2>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        {[
          { label: "Active Sessions", value: activeSessions, icon: "🖥️", color: "bg-blue-50 border-blue-200 text-blue-700" },
          { label: "High Severity Events", value: highSeverity, icon: "🚨", color: "bg-red-50 border-red-200 text-red-700" },
          { label: "Blocked IPs", value: blockedIPs, icon: "🚫", color: "bg-orange-50 border-orange-200 text-orange-700" },
          { label: "Failed Logins (24h)", value: failedLogins.reduce((s, f) => s + f.attempts, 0), icon: "🔒", color: "bg-yellow-50 border-yellow-200 text-yellow-700" },
        ].map(c => (
          <div key={c.label} className={`rounded-xl border p-4 ${c.color}`}>
            <div className="flex items-center gap-2 mb-1">
              <span>{c.icon}</span>
              <p className="text-xs font-medium">{c.label}</p>
            </div>
            <p className="text-2xl font-bold">{c.value}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        {/* Active Sessions */}
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Active Sessions</h3>
          <div className="space-y-3">
            {sessions.map(s => (
              <div key={s._id} className={`rounded-xl border p-4 ${
                s.current ? "border-indigo-300 bg-indigo-50" : "border-gray-200"
              }`}>
                <div className="flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-gray-800 text-sm">{s.user}</span>
                      {s.current && <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">Current</span>}
                    </div>
                    <p className="text-xs text-gray-500 mt-0.5">{s.browser}</p>
                    <p className="text-xs text-gray-400">{s.ip} • {s.location}</p>
                    <p className="text-xs text-gray-400">Last active: {fmt(s.lastActive)}</p>
                  </div>
                  {!s.current && (
                    <button onClick={() => revokeSession(s._id)}
                      className="text-red-500 hover:text-red-700 text-sm font-medium border border-red-200 px-3 py-1 rounded-lg hover:bg-red-50">
                      Revoke
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Failed Login Attempts */}
        <div className="bg-white rounded-xl shadow p-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">Failed Login Attempts</h3>
          <div className="space-y-3">
            {failedLogins.map(f => (
              <div key={f._id} className={`rounded-xl border p-4 ${
                f.blocked ? "border-red-200 bg-red-50" : "border-yellow-200 bg-yellow-50"
              }`}>
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-gray-800 text-sm">{f.email}</p>
                    <p className="text-xs text-gray-500">{f.ip} • {f.attempts} attempts</p>
                    <p className="text-xs text-gray-400">{fmt(f.timestamp)}</p>
                  </div>
                  <span className={`text-xs px-2 py-1 rounded-full font-semibold ${
                    f.blocked ? "bg-red-100 text-red-700" : "bg-yellow-100 text-yellow-700"
                  }`}>{f.blocked ? "Blocked" : "Active"}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Security Events Timeline */}
      <div className="bg-white rounded-xl shadow p-6">
        <h3 className="text-lg font-semibold text-gray-700 mb-4">Security Events Timeline</h3>
        <div className="space-y-3">
          {events.map(e => (
            <div key={e.id} className={`rounded-xl border p-4 ${SEV_COLORS[e.severity]}`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span>{SEV_ICONS[e.severity]}</span>
                  <div>
                    <p className="font-medium text-sm">{e.event}</p>
                    <p className="text-xs opacity-70">{e.user} • {fmt(e.timestamp)}</p>
                  </div>
                </div>
                <span className="text-xs font-semibold uppercase">{e.severity}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
