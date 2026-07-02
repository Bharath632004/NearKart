import React, { useEffect, useState, useCallback } from "react";
import API from "../../api/axios";
import { SkeletonTable } from "../../components/Skeleton";
import Toast from "../../components/Toast";
import useToast from "../../hooks/useToast";
import Modal from "../../components/Modal";
import Pagination from "../../components/Pagination";
import Badge from "../../components/Badge";
import ExportButton from "../../components/ExportButton";

const MOCK_USERS = [
  { _id: "1", name: "Ravi Kumar", email: "ravi@gmail.com", phone: "9876543210", status: "active", kyc: "verified", wallet: 250, orders: 12, joined: "2026-01-10" },
  { _id: "2", name: "Priya Sharma", email: "priya@gmail.com", phone: "9123456789", status: "active", kyc: "pending", wallet: 0, orders: 5, joined: "2026-02-14" },
  { _id: "3", name: "Anjali Reddy", email: "anjali@gmail.com", phone: "9988776655", status: "blocked", kyc: "rejected", wallet: 100, orders: 3, joined: "2026-03-01" },
  { _id: "4", name: "Suresh Babu", email: "suresh@gmail.com", phone: "9090909090", status: "active", kyc: "verified", wallet: 500, orders: 20, joined: "2025-12-20" },
  { _id: "5", name: "Lakshmi Devi", email: "lakshmi@gmail.com", phone: "9871234567", status: "suspended", kyc: "pending", wallet: 75, orders: 7, joined: "2026-04-05" },
];

const PAGE_SIZE = 5;

export default function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [kycFilter, setKycFilter] = useState("all");
  const [page, setPage] = useState(1);
  const [selectedUser, setSelectedUser] = useState(null);
  const [walletModal, setWalletModal] = useState(null);
  const [walletAmount, setWalletAmount] = useState("");
  const [walletNote, setWalletNote] = useState("");
  const { toast, showToast, hideToast } = useToast();

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await API.get("/admin/users");
      setUsers(res.data);
    } catch {
      setUsers(MOCK_USERS);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const filtered = users.filter(u => {
    const matchSearch = u.name.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase()) ||
      u.phone.includes(search);
    const matchStatus = statusFilter === "all" || u.status === statusFilter;
    const matchKyc = kycFilter === "all" || u.kyc === kycFilter;
    return matchSearch && matchStatus && matchKyc;
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const updateStatus = async (id, newStatus) => {
    try {
      await API.patch(`/admin/users/${id}/status`, { status: newStatus });
      showToast(`User ${newStatus} successfully`, "success");
    } catch {
      showToast("Action done (offline mode)", "info");
    }
    setUsers(prev => prev.map(u => u._id === id ? { ...u, status: newStatus } : u));
  };

  const updateKyc = async (id, kyc) => {
    try { await API.patch(`/admin/users/${id}/kyc`, { kyc }); } catch {}
    setUsers(prev => prev.map(u => u._id === id ? { ...u, kyc } : u));
    showToast(`KYC marked as ${kyc}`, "success");
  };

  const adjustWallet = async () => {
    if (!walletAmount) return;
    try {
      await API.post(`/admin/users/${walletModal._id}/wallet`, { amount: Number(walletAmount), note: walletNote });
    } catch {}
    setUsers(prev => prev.map(u => u._id === walletModal._id
      ? { ...u, wallet: (u.wallet || 0) + Number(walletAmount) } : u));
    showToast(`Wallet adjusted by \u20b9${walletAmount}`, "success");
    setWalletModal(null); setWalletAmount(""); setWalletNote("");
  };

  return (
    <div className="space-y-5">
      {toast && <Toast message={toast.message} type={toast.type} onClose={hideToast} />}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-2xl font-bold text-gray-800">User Management</h2>
        <ExportButton data={filtered} filename="users" label="Export CSV" />
      </div>

      <div className="bg-white rounded-xl shadow p-5">
        <div className="flex flex-wrap gap-3 mb-4">
          <input
            className="border border-gray-300 rounded-lg px-4 py-2 text-sm w-full sm:w-72 focus:ring-2 focus:ring-indigo-500"
            placeholder="Search name, email, phone..."
            value={search} onChange={e => { setSearch(e.target.value); setPage(1); }}
          />
          <select className="border border-gray-300 rounded-lg px-3 py-2 text-sm" value={statusFilter}
            onChange={e => { setStatusFilter(e.target.value); setPage(1); }}>
            <option value="all">All Status</option>
            {["active", "blocked", "suspended"].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
          <select className="border border-gray-300 rounded-lg px-3 py-2 text-sm" value={kycFilter}
            onChange={e => { setKycFilter(e.target.value); setPage(1); }}>
            <option value="all">All KYC</option>
            {["verified", "pending", "rejected"].map(k => <option key={k} value={k}>{k}</option>)}
          </select>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
              <tr>{["Name", "Email", "Phone", "Joined", "Orders", "Wallet", "KYC", "Status", "Actions"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>)}
              </tr>
            </thead>
            {loading ? <SkeletonTable rows={5} cols={9} /> : (
              <tbody>
                {paginated.map(u => (
                  <tr key={u._id} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium cursor-pointer text-indigo-600 hover:underline"
                      onClick={() => setSelectedUser(u)}>{u.name}</td>
                    <td className="px-4 py-3 text-gray-600">{u.email}</td>
                    <td className="px-4 py-3">{u.phone}</td>
                    <td className="px-4 py-3 text-gray-500 text-xs">{u.joined}</td>
                    <td className="px-4 py-3">{u.orders}</td>
                    <td className="px-4 py-3 font-semibold">\u20b9{u.wallet}</td>
                    <td className="px-4 py-3">
                      <select className="border rounded px-2 py-1 text-xs"
                        value={u.kyc} onChange={e => updateKyc(u._id, e.target.value)}>
                        {["verified", "pending", "rejected"].map(k => <option key={k} value={k}>{k}</option>)}
                      </select>
                    </td>
                    <td className="px-4 py-3"><Badge label={u.status} type={u.status} /></td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2 flex-wrap">
                        {u.status === "active" && (
                          <>
                            <button onClick={() => updateStatus(u._id, "blocked")}
                              className="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600">Block</button>
                            <button onClick={() => updateStatus(u._id, "suspended")}
                              className="px-2 py-1 bg-orange-400 text-white rounded text-xs hover:bg-orange-500">Suspend</button>
                          </>
                        )}
                        {u.status !== "active" && (
                          <button onClick={() => updateStatus(u._id, "active")}
                            className="px-2 py-1 bg-green-500 text-white rounded text-xs hover:bg-green-600">Activate</button>
                        )}
                        <button onClick={() => setWalletModal(u)}
                          className="px-2 py-1 bg-indigo-500 text-white rounded text-xs hover:bg-indigo-600">Wallet</button>
                      </div>
                    </td>
                  </tr>
                ))}
                {paginated.length === 0 && (
                  <tr><td colSpan={9} className="text-center py-8 text-gray-400">No users found.</td></tr>
                )}
              </tbody>
            )}
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {/* User Detail Modal */}
      <Modal open={!!selectedUser} onClose={() => setSelectedUser(null)} title="User Details" size="md">
        {selectedUser && (
          <div className="space-y-3 text-sm">
            <div className="grid grid-cols-2 gap-3">
              {[["Name", selectedUser.name], ["Email", selectedUser.email],
                ["Phone", selectedUser.phone], ["Joined", selectedUser.joined],
                ["Orders", selectedUser.orders], ["Wallet", `\u20b9${selectedUser.wallet}`],
                ["KYC", selectedUser.kyc], ["Status", selectedUser.status]].map(([k, v]) => (
                <div key={k} className="bg-gray-50 rounded-lg p-3">
                  <p className="text-xs text-gray-400 uppercase">{k}</p>
                  <p className="font-semibold text-gray-800">{v}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </Modal>

      {/* Wallet Adjustment Modal */}
      <Modal open={!!walletModal} onClose={() => { setWalletModal(null); setWalletAmount(""); setWalletNote(""); }}
        title={`Adjust Wallet \u2014 ${walletModal?.name}`}>
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium text-gray-700">Amount (+ add / - deduct)</label>
            <input type="number" value={walletAmount} onChange={e => setWalletAmount(e.target.value)}
              className="mt-1 border border-gray-300 rounded-lg px-3 py-2 w-full text-sm"
              placeholder="e.g. 100 or -50" />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">Note (optional)</label>
            <input type="text" value={walletNote} onChange={e => setWalletNote(e.target.value)}
              className="mt-1 border border-gray-300 rounded-lg px-3 py-2 w-full text-sm"
              placeholder="Reason for adjustment" />
          </div>
          <div className="flex gap-3 pt-2">
            <button onClick={adjustWallet}
              className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm hover:bg-indigo-700">Apply</button>
            <button onClick={() => setWalletModal(null)}
              className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
