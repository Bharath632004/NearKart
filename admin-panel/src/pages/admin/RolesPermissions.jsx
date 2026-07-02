import React, { useState, useEffect } from "react";
import API from "../../api/axios";

const ALL_PERMISSIONS = [
  { id: "view_dashboard", label: "View Dashboard", group: "Dashboard" },
  { id: "view_analytics", label: "View Analytics", group: "Dashboard" },
  { id: "manage_users", label: "Manage Users", group: "Users" },
  { id: "ban_users", label: "Ban Users", group: "Users" },
  { id: "manage_merchants", label: "Manage Merchants", group: "Merchants" },
  { id: "approve_merchants", label: "Approve/Reject Merchants", group: "Merchants" },
  { id: "manage_orders", label: "Manage Orders", group: "Orders" },
  { id: "view_payments", label: "View Payments", group: "Payments" },
  { id: "refund_payments", label: "Refund Payments", group: "Payments" },
  { id: "manage_coupons", label: "Manage Coupons", group: "Coupons" },
  { id: "view_reports", label: "View Reports", group: "Reports" },
  { id: "export_reports", label: "Export Reports", group: "Reports" },
  { id: "manage_notifications", label: "Send Notifications", group: "Notifications" },
  { id: "view_audit_logs", label: "View Audit Logs", group: "Security" },
  { id: "manage_roles", label: "Manage Roles", group: "Security" },
  { id: "manage_settings", label: "Manage Settings", group: "Settings" },
  { id: "view_security", label: "View Security Dashboard", group: "Security" },
];

const PERM_GROUPS = [...new Set(ALL_PERMISSIONS.map(p => p.group))];

const mockRoles = [
  { _id: "r1", name: "Super Admin", description: "Full access to all features", permissions: ALL_PERMISSIONS.map(p => p.id), userCount: 1, editable: false },
  { _id: "r2", name: "Moderator", description: "Can manage users, orders and view reports", permissions: ["view_dashboard", "manage_users", "manage_orders", "view_payments", "view_reports", "view_analytics"], userCount: 3, editable: true },
  { _id: "r3", name: "Support Agent", description: "Can view and respond to customer issues", permissions: ["view_dashboard", "manage_orders", "view_payments", "manage_notifications"], userCount: 5, editable: true },
  { _id: "r4", name: "Finance Manager", description: "Can manage payments and view financial reports", permissions: ["view_dashboard", "view_payments", "refund_payments", "view_reports", "export_reports", "view_analytics"], userCount: 2, editable: true },
];

export default function RolesPermissions() {
  const [roles, setRoles] = useState(mockRoles);
  const [selected, setSelected] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [editPerms, setEditPerms] = useState([]);
  const [newRole, setNewRole] = useState({ name: "", description: "", permissions: [] });
  const [toast, setToast] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    API.get("/admin/roles").then(res => setRoles(res.data)).catch(() => {});
  }, []);

  const openRole = (role) => {
    setSelected(role);
    setEditPerms([...role.permissions]);
    setShowCreate(false);
  };

  const togglePerm = (id, forNew = false) => {
    if (forNew) {
      setNewRole(r => ({ ...r, permissions: r.permissions.includes(id) ? r.permissions.filter(p => p !== id) : [...r.permissions, id] }));
    } else {
      setEditPerms(prev => prev.includes(id) ? prev.filter(p => p !== id) : [...prev, id]);
    }
  };

  const saveRole = async () => {
    if (!selected?.editable) return;
    setSaving(true);
    try { await API.put(`/admin/roles/${selected._id}`, { ...selected, permissions: editPerms }); } catch {}
    setRoles(prev => prev.map(r => r._id === selected._id ? { ...r, permissions: editPerms } : r));
    setSelected(null);
    setSaving(false);
    setToast("✅ Role permissions updated!");
    setTimeout(() => setToast(""), 3000);
  };

  const createRole = async (e) => {
    e.preventDefault();
    if (!newRole.name.trim()) return;
    setSaving(true);
    try { await API.post("/admin/roles", newRole); } catch {}
    const created = { _id: `r${Date.now()}`, ...newRole, userCount: 0, editable: true };
    setRoles(prev => [...prev, created]);
    setNewRole({ name: "", description: "", permissions: [] });
    setShowCreate(false);
    setSaving(false);
    setToast("✅ Role created successfully!");
    setTimeout(() => setToast(""), 3000);
  };

  const deleteRole = async (id) => {
    if (!window.confirm("Delete this role?")) return;
    try { await API.delete(`/admin/roles/${id}`); } catch {}
    setRoles(prev => prev.filter(r => r._id !== id));
    if (selected?._id === id) setSelected(null);
  };

  const PermissionMatrix = ({ perms, onToggle, editable = true }) => (
    <div className="space-y-4">
      {PERM_GROUPS.map(group => (
        <div key={group}>
          <p className="text-xs font-semibold text-gray-500 uppercase mb-2">{group}</p>
          <div className="flex flex-wrap gap-2">
            {ALL_PERMISSIONS.filter(p => p.group === group).map(perm => (
              <button key={perm.id}
                onClick={() => editable && onToggle(perm.id)}
                className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all border ${
                  perms.includes(perm.id)
                    ? "bg-indigo-600 text-white border-indigo-600"
                    : "bg-white text-gray-600 border-gray-300 hover:border-indigo-400"
                } ${!editable ? "cursor-default" : "cursor-pointer"}`}>
                {perms.includes(perm.id) ? "✓ " : ""}{perm.label}
              </button>
            ))}
          </div>
        </div>
      ))}
    </div>
  );

  return (
    <div>
      {toast && <div className="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-xl shadow-lg z-50 text-sm">{toast}</div>}

      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Roles & Permissions</h2>
        <button onClick={() => { setShowCreate(true); setSelected(null); }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 text-sm font-semibold">
          + Create Role
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Roles List */}
        <div className="lg:col-span-1 space-y-3">
          {roles.map(role => (
            <div key={role._id}
              onClick={() => openRole(role)}
              className={`bg-white rounded-xl border p-4 cursor-pointer hover:shadow-md transition-shadow ${
                selected?._id === role._id ? "border-indigo-400 shadow-md" : "border-gray-200"
              }`}>
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-semibold text-gray-800">{role.name}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{role.description}</p>
                </div>
                {!role.editable && <span className="text-xs bg-gray-100 text-gray-500 px-2 py-1 rounded-full">System</span>}
              </div>
              <div className="flex items-center justify-between mt-3">
                <span className="text-xs text-gray-500">{role.permissions.length} permissions</span>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-gray-500">{role.userCount} users</span>
                  {role.editable && (
                    <button onClick={e => { e.stopPropagation(); deleteRole(role._id); }}
                      className="text-red-400 hover:text-red-600 text-xs">Delete</button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Permission Editor */}
        <div className="lg:col-span-2">
          {showCreate ? (
            <div className="bg-white rounded-xl shadow p-6">
              <h3 className="text-lg font-bold text-gray-800 mb-4">Create New Role</h3>
              <form onSubmit={createRole} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Role Name *</label>
                  <input required type="text" value={newRole.name}
                    onChange={e => setNewRole({ ...newRole, name: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2" placeholder="e.g. Content Manager" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                  <input type="text" value={newRole.description}
                    onChange={e => setNewRole({ ...newRole, description: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2" placeholder="Role description" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Permissions</label>
                  <PermissionMatrix perms={newRole.permissions} onToggle={(id) => togglePerm(id, true)} />
                </div>
                <div className="flex gap-3 pt-2">
                  <button type="button" onClick={() => setShowCreate(false)}
                    className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg">Cancel</button>
                  <button type="submit" disabled={saving}
                    className="flex-1 bg-indigo-600 text-white py-2 rounded-lg hover:bg-indigo-700 font-semibold disabled:opacity-50">
                    {saving ? "Creating..." : "Create Role"}
                  </button>
                </div>
              </form>
            </div>
          ) : selected ? (
            <div className="bg-white rounded-xl shadow p-6">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h3 className="text-lg font-bold text-gray-800">{selected.name}</h3>
                  <p className="text-sm text-gray-500">{selected.description}</p>
                </div>
                {!selected.editable && <span className="text-xs bg-yellow-100 text-yellow-700 px-3 py-1 rounded-full">🔒 System Role (Read Only)</span>}
              </div>
              <PermissionMatrix perms={editPerms} onToggle={togglePerm} editable={selected.editable} />
              {selected.editable && (
                <div className="flex gap-3 mt-6">
                  <button onClick={() => setSelected(null)}
                    className="flex-1 border border-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-50">Cancel</button>
                  <button onClick={saveRole} disabled={saving}
                    className="flex-1 bg-indigo-600 text-white py-2 rounded-lg hover:bg-indigo-700 font-semibold disabled:opacity-50">
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="bg-white rounded-xl shadow p-12 flex flex-col items-center justify-center text-gray-400">
              <span className="text-5xl mb-3">🔐</span>
              <p className="text-lg font-medium">Select a role to view and edit permissions</p>
              <p className="text-sm mt-1">Or create a new role using the button above</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
