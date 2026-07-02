import React, { useState, useEffect } from "react";
import API from "../../api/axios";

const mockLogs = [
  { _id: "al1", action: "USER_BANNED", performedBy: "admin@nearkart.in", target: "user:ravi.kumar@gmail.com", ip: "192.168.1.10", timestamp: "2026-07-02T06:45:00Z", status: "success" },
  { _id: "al2", action: "MERCHANT_APPROVED", performedBy: "admin@nearkart.in", target: "merchant:FreshVeggies Store", ip: "192.168.1.10", timestamp: "2026-07-02T06:30:00Z", status: "success" },
  { _id: "al3", action: "COUPON_CREATED", performedBy: "admin@nearkart.in", target: "coupon:SAVE20", ip: "192.168.1.10", timestamp: "2026-07-02T05:15:00Z", status: "success" },
  { _id: "al4", action: "SETTINGS_UPDATED", performedBy: "admin@nearkart.in", target: "platform:deliveryFee", ip: "192.168.1.10", timestamp: "2026-07-01T22:00:00Z", status: "success" },
  { _id: "al5", action: "LOGIN_FAILED", performedBy: "unknown", target: "admin panel", ip: "103.45.67.89", timestamp: "2026-07-01T18:30:00Z", status: "failed" },
  { _id: "al6", action: "ORDER_STATUS_CHANGED", performedBy: "admin@nearkart.in", target: "order:#ORD-1045", ip: "192.168.1.10", timestamp: "2026-07-01T16:00:00Z", status: "success" },
  { _id: "al7", action: "ROLE_CREATED", performedBy: "admin@nearkart.in", target: "role:moderator", ip: "192.168.1.10", timestamp: "2026-07-01T14:00:00Z", status: "success" },
  { _id: "al8", action: "PAYMENT_REFUNDED", performedBy: "admin@nearkart.in", target: "payment:#p9823", ip: "192.168.1.10", timestamp: "2026-07-01T12:30:00Z", status: "success" },
];

const ACTION_COLORS = {
  USER_BANNED: "bg-red-100 text-red-700",
  MERCHANT_APPROVED: "bg-green-100 text-green-700",
  COUPON_CREATED: "bg-blue-100 text-blue-700",
  SETTINGS_UPDATED: "bg-purple-100 text-purple-700",
  LOGIN_FAILED: "bg-red-100 text-red-700",
  ORDER_STATUS_CHANGED: "bg-yellow-100 text-yellow-700",
  ROLE_CREATED: "bg-indigo-100 text-indigo-700",
  PAYMENT_REFUNDED: "bg-orange-100 text-orange-700",
};

export default function AuditLogs() {
  const [logs, setLogs] = useState(mockLogs);
  const [search, setSearch] = useState("");
  const [actionFilter, setActionFilter] = useState("all");
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const PER_PAGE = 6;

  useEffect(() => {
    setLoading(true);
    API.get("/admin/audit-logs", { params: { page, limit: 20 } })
      .then(res => setLogs(res.data.logs || res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page]);

  const allActions = ["all", ...new Set(mockLogs.map(l => l.action))];

  const filtered = logs.filter(l => {
    const matchSearch = [
      l.action, l.performedBy, l.target, l.ip
    ].some(v => v.toLowerCase().includes(search.toLowerCase()));
    const matchAction = actionFilter === "all" || l.action === actionFilter;
    return matchSearch && matchAction;
  });

  const paginated = filtered.slice((page - 1) * PER_PAGE, page * PER_PAGE);
  const totalPages = Math.ceil(filtered.length / PER_PAGE);

  const exportCSV = () => {
    const csv = [
      "Action,Performed By,Target,IP,Timestamp,Status",
      ...filtered.map(l => `${l.action},${l.performedBy},${l.target},${l.ip},${l.timestamp},${l.status}`)
    ].join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a"); a.href = url; a.download = "audit-logs.csv"; a.click();
    URL.revokeObjectURL(url);
  };

  const fmt = (iso) => new Date(iso).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "short" });

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Audit Logs</h2>
        <button onClick={exportCSV}
          className="flex items-center gap-2 bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 text-sm font-semibold">
          ⬇️ Export CSV
        </button>
      </div>

      <div className="grid grid-cols-3 sm:grid-cols-3 gap-4 mb-6">
        {[
          { label: "Total Logs", value: logs.length, color: "bg-blue-50 border-blue-200 text-blue-700" },
          { label: "Success Actions", value: logs.filter(l => l.status === "success").length, color: "bg-green-50 border-green-200 text-green-700" },
          { label: "Failed Actions", value: logs.filter(l => l.status === "failed").length, color: "bg-red-50 border-red-200 text-red-700" },
        ].map(c => (
          <div key={c.label} className={`rounded-xl border p-4 ${c.color}`}>
            <p className="text-xs font-medium mb-1">{c.label}</p>
            <p className="text-2xl font-bold">{c.value}</p>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow p-5">
        <div className="flex flex-wrap gap-3 mb-4">
          <input className="border border-gray-300 rounded-lg px-4 py-2 w-full sm:w-64 text-sm"
            placeholder="Search by action, user, target, IP..."
            value={search} onChange={e => { setSearch(e.target.value); setPage(1); }} />
          <select value={actionFilter} onChange={e => { setActionFilter(e.target.value); setPage(1); }}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm">
            {allActions.map(a => <option key={a} value={a}>{a === "all" ? "All Actions" : a.replace(/_/g, " ")}</option>)}
          </select>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
                <tr>
                  {["Action", "Performed By", "Target", "IP Address", "Timestamp", "Status"].map(h =>
                    <th key={h} className="px-4 py-3 text-left">{h}</th>
                  )}
                </tr>
              </thead>
              <tbody>
                {paginated.map(log => (
                  <tr key={log._id} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        ACTION_COLORS[log.action] || "bg-gray-100 text-gray-700"
                      }`}>{log.action.replace(/_/g, " ")}</span>
                    </td>
                    <td className="px-4 py-3 text-gray-700">{log.performedBy}</td>
                    <td className="px-4 py-3 text-gray-500 text-xs">{log.target}</td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-400">{log.ip}</td>
                    <td className="px-4 py-3 text-gray-500 text-xs">{fmt(log.timestamp)}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        log.status === "success" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"
                      }`}>{log.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {paginated.length === 0 && <p className="text-center text-gray-400 py-8">No logs found.</p>}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between mt-4">
            <p className="text-sm text-gray-500">Showing {(page-1)*PER_PAGE+1}–{Math.min(page*PER_PAGE, filtered.length)} of {filtered.length}</p>
            <div className="flex gap-2">
              <button disabled={page === 1} onClick={() => setPage(p => p - 1)}
                className="px-3 py-1 border rounded-lg text-sm disabled:opacity-40 hover:bg-gray-50">← Prev</button>
              <button disabled={page === totalPages} onClick={() => setPage(p => p + 1)}
                className="px-3 py-1 border rounded-lg text-sm disabled:opacity-40 hover:bg-gray-50">Next →</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
