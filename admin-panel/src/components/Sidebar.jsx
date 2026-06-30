import React from "react";
import { NavLink } from "react-router-dom";

export default function Sidebar({ links, open, title }) {
  return (
    <aside className={`${open ? "w-64" : "w-16"} bg-indigo-700 text-white transition-all duration-300 flex flex-col`}>
      <div className="p-4 font-bold text-xl border-b border-indigo-600">
        {open ? title : "NK"}
      </div>
      <nav className="flex-1 py-4">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 hover:bg-indigo-600 transition-colors ${
                isActive ? "bg-indigo-900 font-semibold" : ""
              }`
            }
          >
            <span className="text-xl">{link.icon}</span>
            {open && <span className="text-sm">{link.label}</span>}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
