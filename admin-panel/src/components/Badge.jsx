import React from "react";

const presets = {
  active: "bg-green-100 text-green-700",
  inactive: "bg-gray-100 text-gray-600",
  blocked: "bg-red-100 text-red-700",
  suspended: "bg-orange-100 text-orange-700",
  pending: "bg-yellow-100 text-yellow-700",
  approved: "bg-green-100 text-green-700",
  rejected: "bg-red-100 text-red-700",
  delivered: "bg-green-100 text-green-700",
  cancelled: "bg-red-100 text-red-700",
  confirmed: "bg-blue-100 text-blue-700",
  out_for_delivery: "bg-indigo-100 text-indigo-700",
  online: "bg-emerald-100 text-emerald-700",
  offline: "bg-gray-100 text-gray-500",
};

export default function Badge({ label, type }) {
  const cls = presets[type] || presets[label?.toLowerCase()] || "bg-gray-100 text-gray-600";
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${cls}`}>
      {label}
    </span>
  );
}
