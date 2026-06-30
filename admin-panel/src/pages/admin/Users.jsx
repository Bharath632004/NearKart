import React, { useEffect, useState } from "react";
import API from "../../api/axios";

const mockUsers = [
  { _id: "1", name: "Ravi Kumar", email: "ravi@gmail.com", phone: "9876543210", status: "active" },
  { _id: "2", name: "Priya Sharma", email: "priya@gmail.com", phone: "9123456789", status: "active" },
  { _id: "3", name: "Anjali Reddy", email: "anjali@gmail.com", phone: "9988776655", status: "blocked" },
];

export default function Users() {
  const [users, setUsers] = useState(mockUsers);
  const [search, setSearch] = useState("");

  useEffect(() => {
    API.get("/admin/users").then(res => setUsers(res.data)).catch(() => {});
  }, []);

  const filtered = users.filter(u =>
    u.name.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  const toggleStatus = async (id, current) => {
    const next = current === "active" ? "blocked" : "active";
    try { await API.patch(`/admin/users/${id}/status`, { status: next }); } catch {}
    setUsers(prev => prev.map(u => u._id === id ? { ...u, status: next } : u));
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-6">User Management</h2>
      <div className="bg-white rounded-xl shadow p-6">
        <input
          className="border border-gray-300 rounded-lg px-4 py-2 mb-4 w-full sm:w-80 focus:ring-2 focus:ring-indigo-500"
          placeholder="Search users..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
              <tr>
                {["Name", "Email", "Phone", "Status", "Action"].map(h => (
                  <th key={h} className="px-4 py-3 text-left">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u._id} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{u.name}</td>
                  <td className="px-4 py-3">{u.email}</td>
                  <td className="px-4 py-3">{u.phone}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${u.status === "active" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                      {u.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => toggleStatus(u._id, u.status)}
                      className={`px-3 py-1 rounded text-xs text-white ${u.status === "active" ? "bg-red-500 hover:bg-red-600" : "bg-green-500 hover:bg-green-600"}`}
                    >
                      {u.status === "active" ? "Block" : "Unblock"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
