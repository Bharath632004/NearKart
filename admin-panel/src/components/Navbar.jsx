import React from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function Navbar({ onToggle }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="bg-white shadow-sm px-6 py-3 flex items-center justify-between">
      <button onClick={onToggle} className="text-gray-600 text-2xl hover:text-indigo-600">☰</button>
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-600 capitalize">
          {user?.name} ({user?.role})
        </span>
        <button
          onClick={handleLogout}
          className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
