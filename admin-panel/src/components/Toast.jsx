import React, { useEffect } from "react";

export default function Toast({ message, type = "success", onClose }) {
  useEffect(() => {
    const t = setTimeout(onClose, 3500);
    return () => clearTimeout(t);
  }, [onClose]);

  const colors = {
    success: "bg-green-600",
    error: "bg-red-600",
    info: "bg-indigo-600",
    warning: "bg-yellow-500",
  };

  return (
    <div className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-5 py-3 rounded-xl shadow-lg text-white text-sm ${colors[type]}`}>
      <span>{message}</span>
      <button onClick={onClose} className="ml-2 font-bold text-white opacity-70 hover:opacity-100">✕</button>
    </div>
  );
}
