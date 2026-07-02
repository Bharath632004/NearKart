import React, { useEffect, useState, useCallback } from "react";
import API from "../../api/axios";
import { SkeletonTable } from "../../components/Skeleton";
import Toast from "../../components/Toast";
import useToast from "../../hooks/useToast";
import Modal from "../../components/Modal";
import Pagination from "../../components/Pagination";
import Badge from "../../components/Badge";
import ExportButton from "../../components/ExportButton";

const MOCK_ORDERS = [
  { _id: "o1", customer: "Ravi Kumar", merchant: "Fresh Veggies", deliveryPartner: "Arun Kumar",
    amount: 350, status: "pending", date: "2026-07-01", refund: false, escalated: false,
    timeline: [{ event: "Order Placed", time: "10:00 AM" }, { event: "Confirmed", time: "10:05 AM" }],
    items: [{ name: "Tomatoes 1kg", qty: 2, price: 60 }, { name: "Onions 1kg", qty: 1, price: 40 }] },
  { _id: "o2", customer: "Priya Sharma", merchant: "Daily Dairy", deliveryPartner: "Vijay S",
    amount: 120, status: "delivered", date: "2026-06-30", refund: false, escalated: false,
    timeline: [{ event: "Order Placed", time: "09:00 AM" }, { event: "Delivered", time: "09:40 AM" }],
    items: [{ name: "Milk 1L", qty: 2, price: 60 }] },
  { _id: "o3", customer: "Anjali Reddy", merchant: "Ravi Kirana", deliveryPartner: null,
    amount: 890, status: "cancelled", date: "2026-06-29", refund: true, escalated: true,
    timeline: [{ event: "Order Placed", time: "08:00 AM" }, { event: "Cancelled", time: "08:15 AM" }],
    items: [{ name: "Rice 5kg", qty: 1, price: 300 }, { name: "Oil 1L", qty: 2, price: 200 }] },
  { _id: "o4", customer: "Suresh Babu", merchant: "Fresh Veggies", deliveryPartner: "Arun Kumar",
    amount: 540, status: "out_for_delivery", date: "2026-07-02", refund: false, escalated: false,
    timeline: [{ event: "Order Placed", time: "11:00 AM" }, { event: "Out for Delivery", time: "11:30 AM" }],
    items: [{ name: "Potatoes 2kg", qty: 1, price: 80 }] },
];

const MOCK_PARTNERS = ["Arun Kumar", "Ramu Reddy", "Vijay S"];
const STATUS_LIST = ["pending", "confirmed", "out_for_delivery", "delivered", "cancelled"];
const PAGE_SIZE = 5;

const STATUS_COLORS = {
  pending: "bg-yellow-100 text-yellow-700",
  confirmed: "bg-blue-100 text-blue-700",
  out_for_delivery: "bg-indigo-100 text-indigo-700",
  delivered: "bg-green-100 text-green-700",
  cancelled: "bg-red-100 text-red-700",
};

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("all");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [assignModal, setAssignModal] = useState(null);
  const [assignPartner, setAssignPartner] = useState("");
  const [refundModal, setRefundModal] = useState(null);
  const { toast, showToast, hideToast } = useToast();

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    try {
      const res = await API.get("/admin/orders");
      setOrders(res.data);
    } catch {
      setOrders(MOCK_ORDERS);
    } finally { setLoading(false); }
  }, []);

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 30000);
    return () => clearInterval(interval);
  }, [fetchOrders]);

  const filtered = orders.filter(o => {
    const matchFilter = filter === "all" || o.status === filter;
    const matchSearch = o.customer.toLowerCase().includes(search.toLowerCase()) ||
      o.merchant.toLowerCase().includes(search.toLowerCase());
    return matchFilter && matchSearch;
  });

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const updateStatus = async (id, status) => {
    try { await API.patch(`/admin/orders/${id}/status`, { status }); } catch {}
    setOrders(prev => prev.map(o => o._id === id ? { ...o, status } : o));
    showToast(`Order status updated to ${status}`, "success");
  };

  const assignDelivery = async () => {
    if (!assignPartner) return;
    try { await API.patch(`/admin/orders/${assignModal._id}/assign`, { partner: assignPartner }); } catch {}
    setOrders(prev => prev.map(o => o._id === assignModal._id ? { ...o, deliveryPartner: assignPartner } : o));
    showToast(`Assigned to ${assignPartner}`, "success");
    setAssignModal(null); setAssignPartner("");
  };

  const approveRefund = async (id) => {
    try { await API.post(`/admin/orders/${id}/refund`); } catch {}
    setOrders(prev => prev.map(o => o._id === id ? { ...o, refund: true } : o));
    showToast("Refund approved", "success");
    setRefundModal(null);
  };

  const escalate = async (id) => {
    try { await API.post(`/admin/orders/${id}/escalate`); } catch {}
    setOrders(prev => prev.map(o => o._id === id ? { ...o, escalated: true } : o));
    showToast("Order escalated", "warning");
  };

  const generateInvoice = (order) => {
    const lines = [
      `NEARKART INVOICE`,
      `Order ID: ${order._id}`,
      `Customer: ${order.customer}`,
      `Merchant: ${order.merchant}`,
      `Date: ${order.date}`,
      `---`,
      ...(order.items || []).map(i => `${i.name} x${i.qty} = \u20b9${i.price}`),
      `---`,
      `Total: \u20b9${order.amount}`,
    ].join("\n");
    const blob = new Blob([lines], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a"); a.href = url;
    a.download = `invoice_${order._id}.txt`; a.click();
    URL.revokeObjectURL(url);
    showToast("Invoice downloaded", "success");
  };

  return (
    <div className="space-y-5">
      {toast && <Toast message={toast.message} type={toast.type} onClose={hideToast} />}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <h2 className="text-2xl font-bold text-gray-800">Order Management</h2>
          <span className="text-xs text-green-600 bg-green-50 px-3 py-1 rounded-full border border-green-200 animate-pulse">\u25cf Live</span>
        </div>
        <ExportButton data={filtered} filename="orders" label="Export CSV" />
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-3">
        {["all", "pending", "confirmed", "out_for_delivery", "delivered"].map(s => (
          <button key={s} onClick={() => { setFilter(s); setPage(1); }}
            className={`rounded-xl p-3 text-sm font-semibold border transition ${filter === s ? "bg-indigo-600 text-white border-indigo-600" : "bg-white hover:bg-gray-50 text-gray-600"`}>
            {s === "all" ? "All Orders" : s.replace(/_/g, " ")}
            <span className="block text-lg font-bold">
              {s === "all" ? orders.length : orders.filter(o => o.status === s).length}
            </span>
          </button>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow p-5">
        <div className="flex flex-wrap gap-3 mb-4">
          <input className="border border-gray-300 rounded-lg px-4 py-2 text-sm w-full sm:w-72 focus:ring-2 focus:ring-indigo-500"
            placeholder="Search customer or merchant..."
            value={search} onChange={e => { setSearch(e.target.value); setPage(1); }} />
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
              <tr>{["Order ID", "Customer", "Merchant", "Partner", "Amount", "Date", "Status", "Actions"].map(h =>
                <th key={h} className="px-4 py-3 text-left">{h}</th>)}
              </tr>
            </thead>
            {loading ? <SkeletonTable rows={5} cols={8} /> : (
              <tbody>
                {paginated.map(o => (
                  <tr key={o._id} className={`border-t hover:bg-gray-50 ${o.escalated ? "bg-red-50" : ""}`}>
                    <td className="px-4 py-3 font-mono text-xs cursor-pointer text-indigo-600 hover:underline"
                      onClick={() => setSelectedOrder(o)}>#{o._id}</td>
                    <td className="px-4 py-3 font-medium">{o.customer}</td>
                    <td className="px-4 py-3">{o.merchant}</td>
                    <td className="px-4 py-3">{o.deliveryPartner || <span className="text-gray-400 italic">Unassigned</span>}</td>
                    <td className="px-4 py-3 font-semibold">\u20b9{o.amount}</td>
                    <td className="px-4 py-3 text-gray-500">{o.date}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-col gap-1">
                        <Badge label={o.status.replace(/_/g, " ")} type={o.status} />
                        {o.escalated && <span className="text-xs text-red-600 font-semibold">\uD83D\uDEA8 Escalated</span>}
                        {o.refund && <span className="text-xs text-green-600 font-semibold">\u21A9 Refunded</span>}
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-1">
                        <select className="border rounded px-1 py-1 text-xs"
                          value={o.status} onChange={e => updateStatus(o._id, e.target.value)}>
                          {STATUS_LIST.map(s => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                        </select>
                        <button onClick={() => { setAssignModal(o); setAssignPartner(o.deliveryPartner || ""); }}
                          className="px-2 py-1 bg-indigo-100 text-indigo-700 rounded text-xs">Assign</button>
                        <button onClick={() => setRefundModal(o)}
                          className="px-2 py-1 bg-yellow-100 text-yellow-700 rounded text-xs">Refund</button>
                        <button onClick={() => generateInvoice(o)}
                          className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs">Invoice</button>
                        {!o.escalated && (
                          <button onClick={() => escalate(o._id)}
                            className="px-2 py-1 bg-red-100 text-red-700 rounded text-xs">Escalate</button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {paginated.length === 0 && (
                  <tr><td colSpan={8} className="text-center py-8 text-gray-400">No orders found.</td></tr>
                )}
              </tbody>
            )}
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {/* Order Timeline Modal */}
      <Modal open={!!selectedOrder} onClose={() => setSelectedOrder(null)} title={`Order #${selectedOrder?._id} Timeline`} size="md">
        {selectedOrder && (
          <div className="space-y-4">
            <div className="space-y-2">
              {selectedOrder.timeline.map((t, i) => (
                <div key={i} className="flex items-start gap-3">
                  <div className="w-2 h-2 bg-indigo-500 rounded-full mt-1.5 flex-shrink-0" />
                  <div>
                    <p className="font-semibold text-sm">{t.event}</p>
                    <p className="text-xs text-gray-400">{t.time}</p>
                  </div>
                </div>
              ))}
            </div>
            <div className="border-t pt-4">
              <p className="text-sm font-semibold mb-2">Items</p>
              {(selectedOrder.items || []).map((item, i) => (
                <div key={i} className="flex justify-between text-sm py-1">
                  <span>{item.name} x{item.qty}</span>
                  <span className="font-semibold">\u20b9{item.price}</span>
                </div>
              ))}
              <div className="flex justify-between font-bold text-sm border-t mt-2 pt-2">
                <span>Total</span><span>\u20b9{selectedOrder.amount}</span>
              </div>
            </div>
          </div>
        )}
      </Modal>

      {/* Assign Modal */}
      <Modal open={!!assignModal} onClose={() => setAssignModal(null)} title="Assign Delivery Partner">
        <div className="space-y-4">
          <select className="border border-gray-300 rounded-lg px-3 py-2 w-full text-sm"
            value={assignPartner} onChange={e => setAssignPartner(e.target.value)}>
            <option value="">Select Partner</option>
            {MOCK_PARTNERS.map(p => <option key={p} value={p}>{p}</option>)}
          </select>
          <div className="flex gap-3">
            <button onClick={assignDelivery} className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm hover:bg-indigo-700">Assign</button>
            <button onClick={() => setAssignModal(null)} className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
          </div>
        </div>
      </Modal>

      {/* Refund Modal */}
      <Modal open={!!refundModal} onClose={() => setRefundModal(null)} title="Approve Refund">
        {refundModal && (
          <div className="space-y-4">
            <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4 text-center">
              <p className="text-sm text-gray-600">Refund Amount for Order <b>#{refundModal._id}</b></p>
              <p className="text-3xl font-bold text-yellow-700 mt-1">\u20b9{refundModal.amount}</p>
            </div>
            <div className="flex gap-3">
              <button onClick={() => approveRefund(refundModal._id)}
                className="flex-1 bg-green-600 text-white py-2 rounded-lg text-sm hover:bg-green-700">Approve Refund</button>
              <button onClick={() => setRefundModal(null)}
                className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
