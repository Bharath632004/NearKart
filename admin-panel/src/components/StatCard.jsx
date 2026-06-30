export default function StatCard({ title, value, icon, color = "indigo" }) {
  const colors = {
    indigo: "bg-indigo-100 text-indigo-700",
    green: "bg-green-100 text-green-700",
    yellow: "bg-yellow-100 text-yellow-700",
    red: "bg-red-100 text-red-700",
  };
  return (
    <div className="bg-white rounded-xl shadow p-5 flex items-center gap-4">
      <div className={`text-3xl p-3 rounded-lg ${colors[color]}`}>{icon}</div>
      <div>
        <p className="text-gray-500 text-sm">{title}</p>
        <p className="text-2xl font-bold text-gray-800">{value}</p>
      </div>
    </div>
  );
}
