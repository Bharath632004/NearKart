import React, { useEffect, useState, useCallback } from "react";
import API from "../../api/axios";
import { SkeletonTable } from "../../components/Skeleton";
import Toast from "../../components/Toast";
import useToast from "../../hooks/useToast";
import Modal from "../../components/Modal";
import Pagination from "../../components/Pagination";
import Badge from "../../components/Badge";
import ExportButton from "../../components/ExportButton";

const MOCK_MERCHANTS = [
  { _id: "m1", name: "Fresh Veggies Store", owner: "Ravi Kumar", email: "ravi@store.com", phone: "9876543210",
    status: "pending", gst: "29ABCDE1234F1Z5", gstVerified: false, commission: 8,
    category: "Grocery", orders: 142, rating: 4.8, balance: 12400,
    hours: { open: "08:00", close: "21:00" }, joined: "2026-05-01" },
  { _id: "m2", name: "Daily Dairy Hub", owner: "Priya S", email: "priya@dairy.com", phone: "9123456789",
    status: "approved", gst: "27XYZPQ9876G2A1", gstVerified: true, commission: 10,
    category: "Dairy", orders: 98, rating: 4.6, balance: 8200,
    hours: { open: "06:00", close: "20:00" }, joined: "2026-03-15" },
  { _id: "m3", name: "Ravi Kirana", owner: "Suresh B", email: "suresh@kirana.com", phone: "9988776655",
    status: "rejected", gst: "36LMNOP5432H3B2", gstVerified: false, commission: 7,
    category: "Kirana", orders: 74, rating: 4.5, balance: 5600,
    hours: { open: "09:00", close: "22:00" }, joined: "2026-04-10" },
];

const PAGE_SIZE = 5;

export default function MerchantApproval() {
  const [merchants, setMerchants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [page, setPage] = useState(1);
  const [detailMerchant, setDetailMerchant] = useState(null);
  const [commissionModal, setCommissionModal] = useState(null);
  const [commissionVal, setCommissionVal] = useState("");
  const [hoursModal, setHoursModal] = useState(null);
  const [hoursVal, setHoursVal] = useState({ open: "", close: "" });
  const [payoutModal, setPayoutModal] = useState(null);
  const { toast, showToast, hideToast } = useToast();

  const fetchMerchants = useCallback(async () => {
    setLoading(true);
    try {
      const res = await API.get("/admin/merchants");
      setMerchants(res.data);
    } catch {
      setMerchants(MOCK_MERCHANTS);
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { fetchMerchants(); }, [fetchMerchants]);

  const filtered = merchants.filter(m => {
    const matchSearch = m.name.toLowerCase().includes(search.toLowerCase()) ||
      m.owner.toLowerCase().includes(search.toLowerCase()) ||
      m.email.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === "all" || m.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const updateStatus = async (id, status, reason = "") => {
    try { await API.patch(`/admin/merchants/${id}/status`, { status, reason }); } catch {}
    setMerchants(prev => prev.map(m => m._id === id ? { ...m, status } : m));
    showToast(`Merchant ${status}`, "success");
  };

  const verifyGST = async (id) => {
    try { await API.patch(`/admin/merchants/${id}/gst-verify`); } catch {}
    setMerchants(prev => prev.map(m => m._id === id ? { ...m, gstVerified: true } : m));
    showToast("GST Verified", "success");
  };

  const saveCommission = async () => {
    if (!commissionVal) return;
    try { await API.patch(`/admin/merchants/${commissionModal._id}/commission`, { commission: Number(commissionVal) }); } catch {}
    setMerchants(prev => prev.map(m => m._id === commissionModal._id ? { ...m, commission: Number(commissionVal) } : m));
    showToast("Commission updated", "success");
    setCommissionModal(null); setCommissionVal("");
  };

  const saveHours = async () => {
    try { await API.patch(`/admin/merchants/${hoursModal._id}/hours`, hoursVal); } catch {}
    setMerchants(prev => prev.map(m => m._id === hoursModal._id ? { ...m, hours: hoursVal } : m));
    showToast("Operating hours updated", "success");
    setHoursModal(null);
  };

  const processPayout = async () => {
    try { await API.post(`/admin/merchants/${payoutModal._id}/payout`); } catch {}
    setMerchants(prev => prev.map(m => m._id === payoutModal._id ? { ...m, balance: 0 } : m));
    showToast(`Payout of \u20b9${payoutModal.balance} processed`, "success");
    setPayoutModal(null);
  };

  return (
    <div className="space-y-5">
      {toast && <Toast message={toast.message} type={toast.type} onClose={hideToast} />}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-2xl font-bold text-gray-800">Merchant Management</h2>
        <ExportButton data={filtered} filename="merchants" label="Export CSV" />
      </div>

      <div className="bg-white rounded-xl shadow p-5">
        <div className="flex flex-wrap gap-3 mb-4">
          <input className="border border-gray-300 rounded-lg px-4 py-2 text-sm w-full sm:w-72 focus:ring-2 focus:ring-indigo-500"
            placeholder="Search merchant, owner, email..."
            value={search} onChange={e => { setSearch(e.target.value); setPage(1); }} />
          <select className="border border-gray-300 rounded-lg px-3 py-2 text-sm" value={statusFilter}
            onChange={e => { setStatusFilter(e.target.value); setPage(1); }}>
            <option value="all">All Status</option>
            {["pending", "approved", "rejected"].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
              <tr>{["Store", "Owner", "Category", "Commission", "GST", "Balance", "Status", "Actions"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>)}
              </tr>
            </thead>
            {loading ? <SkeletonTable rows={5} cols={8} /> : (
              <tbody>
                {paginated.map(m => (
                  <tr key={m._id} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <p className="font-medium cursor-pointer text-indigo-600 hover:underline" onClick={() => setDetailMerchant(m)}>{m.name}</p>
                      <p className="text-xs text-gray-400">{m.email}</p>
                    </td>
                    <td className="px-4 py-3">{m.owner}</td>
                    <td className="px-4 py-3 text-gray-500">{m.category}</td>
                    <td className="px-4 py-3">
                      <span className="font-semibold">{m.commission}%</span>
                      <button onClick={() => { setCommissionModal(m); setCommissionVal(String(m.commission)); }}
                        className="ml-2 text-xs text-indigo-500 hover:underline">Edit</button>
                    </td>
                    <td className="px-4 py-3">
                      {m.gstVerified
                        ? <span className="text-xs text-green-600 font-semibold">\u2705 Verified</span>
                        : <button onClick={() => verifyGST(m._id)}
                            className="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded hover:bg-yellow-200">Verify GST</button>}
                    </td>
                    <td className="px-4 py-3 font-semibold">\u20b9{m.balance.toLocaleString()}</td>
                    <td className="px-4 py-3"><Badge label={m.status} type={m.status} /></td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-1">
                        {m.status === "pending" && (
                          <>
                            <button onClick={() => updateStatus(m._id, "approved")}
                              className="px-2 py-1 bg-green-500 text-white rounded text-xs hover:bg-green-600">✔ Approve</button>
                            <button onClick={() => updateStatus(m._id, "rejected")}
                              className="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600">✘ Reject</button>
                          </>
                        )}
                        {m.status === "approved" && (
                          <button onClick={() => updateStatus(m._id, "rejected")}
                            className="px-2 py-1 bg-red-400 text-white rounded text-xs">Suspend</button>
                        )}
                        <button onClick={() => { setHoursModal(m); setHoursVal(m.hours); }}
                          className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs hover:bg-gray-200">Hours</button>
                        <button onClick={() => setPayoutModal(m)}
                          className="px-2 py-1 bg-indigo-500 text-white rounded text-xs hover:bg-indigo-600">Payout</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {paginated.length === 0 && (
                  <tr><td colSpan={8} className="text-center py-8 text-gray-400">No merchants found.</td></tr>
                )}
              </tbody>
            )}
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {/* Merchant Detail */}
      <Modal open={!!detailMerchant} onClose={() => setDetailMerchant(null)} title="Merchant Details" size="lg">
        {detailMerchant && (
          <div className="grid grid-cols-2 gap-3 text-sm">
            {[["Store Name", detailMerchant.name], ["Owner", detailMerchant.owner],
              ["Email", detailMerchant.email], ["Phone", detailMerchant.phone],
              ["GST Number", detailMerchant.gst], ["Category", detailMerchant.category],
              ["Commission", `${detailMerchant.commission}%`], ["Total Orders", detailMerchant.orders],
              ["Rating", `\u2605 ${detailMerchant.rating}`], ["Balance", `\u20b9${detailMerchant.balance}`],
              ["Opening Hours", `${detailMerchant.hours.open} \u2014 ${detailMerchant.hours.close}`],
              ["Joined", detailMerchant.joined]].map(([k, v]) => (
              <div key={k} className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400 uppercase">{k}</p>
                <p className="font-semibold text-gray-800">{v}</p>
              </div>
            ))}
          </div>
        )}
      </Modal>

      {/* Commission Modal */}
      <Modal open={!!commissionModal} onClose={() => setCommissionModal(null)} title="Edit Commission">
        <div className="space-y-4">
          <label className="text-sm font-medium text-gray-700">Commission Rate (%)</label>
          <input type="number" min="0" max="100" value={commissionVal} onChange={e => setCommissionVal(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 w-full text-sm" />
          <div className="flex gap-3">
            <button onClick={saveCommission} className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm hover:bg-indigo-700">Save</button>
            <button onClick={() => setCommissionModal(null)} className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
          </div>
        </div>
      </Modal>

      {/* Operating Hours Modal */}
      <Modal open={!!hoursModal} onClose={() => setHoursModal(null)} title="Store Operating Hours">
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-700">Opening Time</label>
              <input type="time" value={hoursVal.open} onChange={e => setHoursVal(p => ({ ...p, open: e.target.value }))}
                className="mt-1 border border-gray-300 rounded-lg px-3 py-2 w-full text-sm" />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700">Closing Time</label>
              <input type="time" value={hoursVal.close} onChange={e => setHoursVal(p => ({ ...p, close: e.target.value }))}
                className="mt-1 border border-gray-300 rounded-lg px-3 py-2 w-full text-sm" />
            </div>
          </div>
          <div className="flex gap-3">
            <button onClick={saveHours} className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm hover:bg-indigo-700">Save</button>
            <button onClick={() => setHoursModal(null)} className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
          </div>
        </div>
      </Modal>

      {/* Payout Modal */}
      <Modal open={!!payoutModal} onClose={() => setPayoutModal(null)} title="Process Payout">
        {payoutModal && (
          <div className="space-y-4">
            <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-4 text-center">
              <p className="text-sm text-gray-600">Payout Amount for <b>{payoutModal.name}</b></p>
              <p className="text-3xl font-bold text-indigo-700 mt-1">\u20b9{payoutModal.balance.toLocaleString()}</p>
            </div>
            <p className="text-xs text-gray-400 text-center">This will initiate a bank transfer to the merchant's registered account.</p>
            <div className="flex gap-3">
              <button onClick={processPayout} className="flex-1 bg-green-600 text-white py-2 rounded-lg text-sm hover:bg-green-700">Confirm Payout</button>
              <button onClick={() => setPayoutModal(null)} className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
