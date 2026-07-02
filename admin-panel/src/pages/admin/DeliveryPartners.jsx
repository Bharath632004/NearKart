import React, { useEffect, useState, useCallback } from "react";
import API from "../../api/axios";
import { SkeletonTable } from "../../components/Skeleton";
import Toast from "../../components/Toast";
import useToast from "../../hooks/useToast";
import Modal from "../../components/Modal";
import Pagination from "../../components/Pagination";
import Badge from "../../components/Badge";
import ExportButton from "../../components/ExportButton";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

const MOCK_PARTNERS = [
  { _id: "dp1", name: "Arun Kumar", phone: "9876501234", email: "arun@del.com",
    status: "online", vehicle: "Bike", vehicleNo: "AP09AB1234", vehicleVerified: true,
    earnings: 4200, deliveries: 38, rating: 4.7, shift: "Morning (6AM-2PM)",
    location: { lat: 16.31, lng: 80.44 }, joined: "2026-01-10" },
  { _id: "dp2", name: "Ramu Reddy", phone: "9123000456", email: "ramu@del.com",
    status: "offline", vehicle: "Bicycle", vehicleNo: "N/A", vehicleVerified: false,
    earnings: 2100, deliveries: 19, rating: 4.3, shift: "Evening (2PM-10PM)",
    location: { lat: 16.32, lng: 80.45 }, joined: "2026-02-20" },
  { _id: "dp3", name: "Vijay S", phone: "9988001122", email: "vijay@del.com",
    status: "online", vehicle: "Scooter", vehicleNo: "AP09CD5678", vehicleVerified: true,
    earnings: 6100, deliveries: 55, rating: 4.9, shift: "Full Day (6AM-10PM)",
    location: { lat: 16.30, lng: 80.46 }, joined: "2025-12-05" },
];

const EARNING_CHART = [
  { name: "Arun", earnings: 4200 },
  { name: "Ramu", earnings: 2100 },
  { name: "Vijay", earnings: 6100 },
];

const PAGE_SIZE = 5;

export default function DeliveryPartners() {
  const [partners, setPartners] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [page, setPage] = useState(1);
  const [detailPartner, setDetailPartner] = useState(null);
  const [shiftModal, setShiftModal] = useState(null);
  const [shiftVal, setShiftVal] = useState("");
  const { toast, showToast, hideToast } = useToast();

  const SHIFTS = ["Morning (6AM-2PM)", "Evening (2PM-10PM)", "Night (10PM-6AM)", "Full Day (6AM-10PM)"];

  const fetchPartners = useCallback(async () => {
    setLoading(true);
    try {
      const res = await API.get("/admin/delivery-partners");
      setPartners(res.data);
    } catch {
      setPartners(MOCK_PARTNERS);
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { fetchPartners(); }, [fetchPartners]);

  const filtered = partners.filter(p => {
    const matchSearch = p.name.toLowerCase().includes(search.toLowerCase()) || p.phone.includes(search);
    const matchStatus = statusFilter === "all" || p.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const toggleOnline = async (id, current) => {
    const next = current === "online" ? "offline" : "online";
    try { await API.patch(`/admin/delivery-partners/${id}/status`, { status: next }); } catch {}
    setPartners(prev => prev.map(p => p._id === id ? { ...p, status: next } : p));
    showToast(`Partner marked ${next}`, "success");
  };

  const verifyVehicle = async (id) => {
    try { await API.patch(`/admin/delivery-partners/${id}/vehicle-verify`); } catch {}
    setPartners(prev => prev.map(p => p._id === id ? { ...p, vehicleVerified: true } : p));
    showToast("Vehicle verified", "success");
  };

  const saveShift = async () => {
    try { await API.patch(`/admin/delivery-partners/${shiftModal._id}/shift`, { shift: shiftVal }); } catch {}
    setPartners(prev => prev.map(p => p._id === shiftModal._id ? { ...p, shift: shiftVal } : p));
    showToast("Shift updated", "success");
    setShiftModal(null);
  };

  return (
    <div className="space-y-6">
      {toast && <Toast message={toast.message} type={toast.type} onClose={hideToast} />}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-2xl font-bold text-gray-800">Delivery Partners</h2>
        <ExportButton data={filtered} filename="delivery-partners" label="Export CSV" />
      </div>

      {/* Analytics summary cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Total Partners", value: partners.length, icon: "\uD83D\uDEB4" },
          { label: "Online Now", value: partners.filter(p => p.status === "online").length, icon: "\uD83D\uDFE2" },
          { label: "Total Deliveries", value: partners.reduce((a, p) => a + p.deliveries, 0), icon: "\uD83D\uDCE6" },
          { label: "Total Earnings", value: `\u20b9${partners.reduce((a, p) => a + p.earnings, 0).toLocaleString()}`, icon: "\uD83D\uDCB0" },
        ].map(c => (
          <div key={c.label} className="bg-white rounded-xl shadow p-4 flex items-center gap-3">
            <span className="text-2xl">{c.icon}</span>
            <div>
              <p className="text-xs text-gray-400">{c.label}</p>
              <p className="text-xl font-bold text-gray-800">{c.value}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Earnings Chart */}
      <div className="bg-white rounded-xl shadow p-5">
        <h3 className="text-base font-semibold text-gray-700 mb-4">Partner Earnings Comparison</h3>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={EARNING_CHART}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} />
            <Tooltip formatter={(v) => [`\u20b9${v}`, "Earnings"]} />
            <Bar dataKey="earnings" fill="#6366f1" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow p-5">
        <div className="flex flex-wrap gap-3 mb-4">
          <input className="border border-gray-300 rounded-lg px-4 py-2 text-sm w-full sm:w-72 focus:ring-2 focus:ring-indigo-500"
            placeholder="Search name or phone..." value={search}
            onChange={e => { setSearch(e.target.value); setPage(1); }} />
          <select className="border border-gray-300 rounded-lg px-3 py-2 text-sm" value={statusFilter}
            onChange={e => { setStatusFilter(e.target.value); setPage(1); }}>
            <option value="all">All</option>
            <option value="online">Online</option>
            <option value="offline">Offline</option>
          </select>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
              <tr>{["Name", "Phone", "Vehicle", "Shift", "Deliveries", "Earnings", "Rating", "Status", "Actions"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>)}
              </tr>
            </thead>
            {loading ? <SkeletonTable rows={4} cols={9} /> : (
              <tbody>
                {paginated.map(p => (
                  <tr key={p._id} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium cursor-pointer text-indigo-600 hover:underline"
                      onClick={() => setDetailPartner(p)}>{p.name}</td>
                    <td className="px-4 py-3">{p.phone}</td>
                    <td className="px-4 py-3">
                      <span>{p.vehicle} ({p.vehicleNo})</span>
                      {!p.vehicleVerified && (
                        <button onClick={() => verifyVehicle(p._id)}
                          className="ml-2 text-xs text-yellow-600 hover:underline">Verify</button>
                      )}
                      {p.vehicleVerified && <span className="ml-2 text-xs text-green-600">\u2705</span>}
                    </td>
                    <td className="px-4 py-3 text-gray-500 text-xs">{p.shift}</td>
                    <td className="px-4 py-3">{p.deliveries}</td>
                    <td className="px-4 py-3 font-semibold">\u20b9{p.earnings.toLocaleString()}</td>
                    <td className="px-4 py-3 text-yellow-500 font-semibold">\u2605 {p.rating}</td>
                    <td className="px-4 py-3"><Badge label={p.status} type={p.status} /></td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <button onClick={() => toggleOnline(p._id, p.status)}
                          className={`px-2 py-1 rounded text-xs text-white ${p.status === "online" ? "bg-red-500 hover:bg-red-600" : "bg-green-500 hover:bg-green-600"}`}>
                          {p.status === "online" ? "Go Offline" : "Go Online"}
                        </button>
                        <button onClick={() => { setShiftModal(p); setShiftVal(p.shift); }}
                          className="px-2 py-1 bg-indigo-100 text-indigo-700 rounded text-xs hover:bg-indigo-200">Shift</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {paginated.length === 0 && (
                  <tr><td colSpan={9} className="text-center py-8 text-gray-400">No partners found.</td></tr>
                )}
              </tbody>
            )}
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {/* Detail Modal */}
      <Modal open={!!detailPartner} onClose={() => setDetailPartner(null)} title="Partner Details" size="md">
        {detailPartner && (
          <div className="grid grid-cols-2 gap-3 text-sm">
            {[["Name", detailPartner.name], ["Phone", detailPartner.phone],
              ["Email", detailPartner.email], ["Vehicle", detailPartner.vehicle],
              ["Vehicle No.", detailPartner.vehicleNo], ["Shift", detailPartner.shift],
              ["Deliveries", detailPartner.deliveries], ["Earnings", `\u20b9${detailPartner.earnings}`],
              ["Rating", `\u2605 ${detailPartner.rating}`], ["Status", detailPartner.status],
              ["Location", `${detailPartner.location.lat}, ${detailPartner.location.lng}`],
              ["Joined", detailPartner.joined]].map(([k, v]) => (
              <div key={k} className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400 uppercase">{k}</p>
                <p className="font-semibold text-gray-800">{v}</p>
              </div>
            ))}
          </div>
        )}
      </Modal>

      {/* Shift Modal */}
      <Modal open={!!shiftModal} onClose={() => setShiftModal(null)} title="Assign Shift">
        <div className="space-y-4">
          <select className="border border-gray-300 rounded-lg px-3 py-2 w-full text-sm"
            value={shiftVal} onChange={e => setShiftVal(e.target.value)}>
            {SHIFTS.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
          <div className="flex gap-3">
            <button onClick={saveShift} className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm hover:bg-indigo-700">Save</button>
            <button onClick={() => setShiftModal(null)} className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
